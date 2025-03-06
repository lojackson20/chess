package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {

    private ChessBoard board;
    private TeamColor teamTurn;

    public ChessGame() {
        this.board = new ChessBoard();
        this.board.resetBoard();
        this.teamTurn = TeamColor.WHITE;

    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return teamTurn;
    }

    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        this.teamTurn = team;
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    /**
     * Gets a valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        ChessPiece piece = board.getPiece(startPosition);
        if (piece == null) {
            return List.of();
        }

        Collection<ChessMove> allMoves = piece.pieceMoves(board, startPosition);
        Collection<ChessMove> validMoves = new ArrayList<>();

        for (ChessMove move : allMoves) {
            ChessBoard copiedBoard = board.copyBoard();
            copiedBoard.addPiece(move.getEndPosition(), piece);
            copiedBoard.addPiece(move.getStartPosition(), null);

            ChessGame tempGame = new ChessGame();
            tempGame.setBoard(copiedBoard);
            tempGame.setTeamTurn(teamTurn);

            if (!tempGame.isInCheck(piece.getTeamColor())) {
                validMoves.add(move);
            }
        }

        return validMoves;
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to preform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        ChessPiece piece = board.getPiece(move.getStartPosition());
        if (piece == null || piece.getTeamColor() != teamTurn) {
            throw new InvalidMoveException("You can't do that, there isn't a piece there");
        }
        if (!validMoves(move.getStartPosition()).contains(move)) {
            throw new InvalidMoveException("You can't do that, that piece doesn't do that.");
        }

        ChessBoard copiedBoard = board.copyBoard();
        copiedBoard.addPiece(move.getEndPosition(), piece);
        copiedBoard.addPiece(move.getStartPosition(), null);

        if (isInCheck(teamTurn)) {
            throw new InvalidMoveException("You can't do that, your king is still in check!");
        }

        ChessPiece newPiece = piece;
        if (piece.getPieceType() == ChessPiece.PieceType.PAWN) {
            int lastRow = (piece.getTeamColor() == TeamColor.WHITE) ? 8 : 1;
            if (move.getEndPosition().getRow() == lastRow) {
                ChessPiece.PieceType promotionType = move.getPromotionPiece();

                if (promotionType == null || promotionType == ChessPiece.PieceType.PAWN || promotionType == ChessPiece.PieceType.KING) {
                    promotionType = ChessPiece.PieceType.QUEEN;
                }
                newPiece = new ChessPiece(piece.getTeamColor(), promotionType);
            }
        }

        board.addPiece(move.getEndPosition(), newPiece);
        board.addPiece(move.getStartPosition(), null);

        teamTurn = (teamTurn == TeamColor.WHITE) ? TeamColor.BLACK : TeamColor.WHITE;
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        ChessPosition kingPosition = findKingPosition(teamColor);
        if (kingPosition == null) {
            return false;
        }

        TeamColor opponentColor = getOpponentColor(teamColor);
        return isPositionUnderAttack(kingPosition, opponentColor);
    }

    private ChessPosition findKingPosition(TeamColor teamColor) {
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition position = new ChessPosition(row, col);
                ChessPiece piece = board.getPiece(position);
                if (isKingOfColor(piece, teamColor)) {
                    return position;
                }
            }
        }
        return null;
    }

    private boolean isKingOfColor(ChessPiece piece, TeamColor teamColor) {
        return piece != null && piece.getTeamColor() == teamColor && piece.getPieceType() == ChessPiece.PieceType.KING;
    }

    private TeamColor getOpponentColor(TeamColor teamColor) {
        return (teamColor == TeamColor.WHITE) ? TeamColor.BLACK : TeamColor.WHITE;
    }

    private boolean isPositionUnderAttack(ChessPosition position, TeamColor opponentColor) {
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition attackerPosition = new ChessPosition(row, col);
                ChessPiece attacker = board.getPiece(attackerPosition);

                if (isOpponentPiece(attacker, opponentColor) && canAttackPosition(attacker, attackerPosition, position)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isOpponentPiece(ChessPiece piece, TeamColor opponentColor) {
        return piece != null && piece.getTeamColor() == opponentColor;
    }

    private boolean canAttackPosition(ChessPiece piece, ChessPosition from, ChessPosition target) {
        for (ChessMove move : piece.pieceMoves(board, from)) {
            if (move.getEndPosition().equals(target)) {
                return true;
            }
        }
        return false;
    }


    /**
     * Determines if the given team is in checkmate
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        if (!isInCheck(teamColor)) {
            return false;
        }
        return checkForCheck(teamColor);
    }

    private boolean checkForCheck(TeamColor teamColor) {
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition position = new ChessPosition(row, col);
                ChessPiece piece = board.getPiece(position);

                if (piece == null || piece.getTeamColor() != teamColor) {
                    continue;
                }

                for (ChessMove move : piece.pieceMoves(board, position)) {
                    ChessBoard copiedBoard = board.copyBoard();
                    copiedBoard.addPiece(move.getEndPosition(), piece);
                    copiedBoard.addPiece(move.getStartPosition(), null);

                    ChessGame tempGame = new ChessGame();
                    tempGame.setBoard(copiedBoard);

                    if (!tempGame.isInCheck(teamColor)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }


    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        if (isInCheck(teamColor)) {
            return false;
        }

        return checkForCheck(teamColor);
    }

    /**
     * Sets this game's chessboard with a given board
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        this.board =board;
    }

    /**
     * Gets the current chessboard
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return board;
    }
}
