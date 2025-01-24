package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class RookMoveCalculator extends ChessPieceCalculator {

    private static final int[][] ROOK_MOVES = {
            {0,1}, {0, -1}, {1, 0}, {-1, 0}
    };

    @Override
    Collection<ChessMove> possibleMoves(ChessBoard board, ChessPosition myPosition, ChessGame.TeamColor color) {
        List<ChessMove> moves = new ArrayList<>();

        for (int[] move : ROOK_MOVES) {
            int newRow = myPosition.getRow() + move[0];
            int newCol = myPosition.getColumn() + move[1];
            ChessPosition newPosition = new ChessPosition(newRow, newCol);

            if (isValidMove(board, newPosition, color)) {
                ChessPiece capturedPiece = board.getPiece(newPosition);
                moves.add(new ChessMove(myPosition, newPosition, null));
            }
        }

        return moves;
    }
    private boolean isValidMove(ChessBoard board, ChessPosition position, ChessGame.TeamColor color) {
        if (position.getRow() < 1 || position.getRow() > 8 || position.getColumn() < 1 || position.getColumn() > 8) {
            return false;
        }

        ChessPiece pieceAtDestination = board.getPiece(position);

        // Check if the destination is empty or occupied by an opponent's piece
        return pieceAtDestination == null || pieceAtDestination.getTeamColor() != color;
    }
}
