package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class PawnMoveCalculator extends ChessPieceCalculator {


    @Override
    Collection<ChessMove> possibleMoves(ChessBoard board, ChessPosition myPosition, ChessGame.TeamColor color) {
        List<ChessMove> moves = new ArrayList<>();
        // check to see if moving forward one is ok
            // check to see if moving one is valid and it is the last row -> add 4 moves( queen, rook, bishop, k)
            // check to see if in the first row (starting) can move forward two if so see if it is ok to move two
        //        int direction = 1;
//        if (color == ChessGame.TeamColor.BLACK) {
//            direction = -1;
//        }
        int direction = (color == ChessGame.TeamColor.WHITE) ? 1 : -1;

        ChessPosition oneForward = new ChessPosition(myPosition.getRow() + direction, myPosition.getColumn());
        // check if next forward is in bounds and empty
        if (isInBounds(oneForward) && board.getPiece(oneForward) == null) {
            if (oneForward.getRow() == 8 || oneForward.getRow() == 1) {
                moves.add(new ChessMove(myPosition, oneForward, ChessPiece.PieceType.BISHOP));
                moves.add(new ChessMove(myPosition, oneForward, ChessPiece.PieceType.QUEEN));
                moves.add(new ChessMove(myPosition, oneForward, ChessPiece.PieceType.ROOK));
                moves.add(new ChessMove(myPosition, oneForward, ChessPiece.PieceType.KNIGHT));
            } else {
                moves.add(new ChessMove(myPosition, oneForward, null));
            }
            // can go forward 2
            if (isInStartingRow(myPosition.getRow(), color)) {
                ChessPosition twoForward = new ChessPosition(myPosition.getRow() + (direction * 2), myPosition.getColumn());
                if (board.getPiece(twoForward) == null) {
                    moves.add(new ChessMove(myPosition, twoForward, null));
                }
            }
        }

        // diagonal stuff
        ChessPosition minusDiagonal = new ChessPosition(myPosition.getRow() + direction, myPosition.getColumn() - 1);
        ChessPosition plusDiagonal = new ChessPosition(myPosition.getRow() + direction, myPosition.getColumn() + 1);
        ChessPiece minusPiece = board.getPiece(minusDiagonal);
        ChessPiece plusPiece = board.getPiece(plusDiagonal);

        if (isInBounds(minusDiagonal) && minusPiece != null && minusPiece.getTeamColor() != color) {
            if (minusDiagonal.getRow() == 8 || minusDiagonal.getRow() == 1) {
                moves.add(new ChessMove(myPosition, minusDiagonal, ChessPiece.PieceType.BISHOP));
                moves.add(new ChessMove(myPosition, minusDiagonal, ChessPiece.PieceType.QUEEN));
                moves.add(new ChessMove(myPosition, minusDiagonal, ChessPiece.PieceType.ROOK));
                moves.add(new ChessMove(myPosition, minusDiagonal, ChessPiece.PieceType.KNIGHT));
            } else {
                moves.add(new ChessMove(myPosition, minusDiagonal, null));
            }
        }

        if (isInBounds(plusDiagonal) && plusPiece != null && plusPiece.getTeamColor() != color) {
            if (plusDiagonal.getRow() == 8 || plusDiagonal.getRow() == 1) {
                moves.add(new ChessMove(myPosition, plusDiagonal, ChessPiece.PieceType.BISHOP));
                moves.add(new ChessMove(myPosition, plusDiagonal, ChessPiece.PieceType.QUEEN));
                moves.add(new ChessMove(myPosition, plusDiagonal, ChessPiece.PieceType.ROOK));
                moves.add(new ChessMove(myPosition, plusDiagonal, ChessPiece.PieceType.KNIGHT));
            } else {
                moves.add(new ChessMove(myPosition, plusDiagonal, null));
            }
        }

        return moves;

    }

    private boolean isInStartingRow(int row, ChessGame.TeamColor color) {
        return (color == ChessGame.TeamColor.WHITE && row == 2) || (color == ChessGame.TeamColor.BLACK && row == 7);
    }

    private boolean isInBounds(ChessPosition position) {
        int row = position.getRow();
        int col = position.getColumn();
        return row >= 1 && row <= 8 && col >= 1 && col <= 8;
    }

}
