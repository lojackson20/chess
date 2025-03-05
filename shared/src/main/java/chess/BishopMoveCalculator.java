package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class BishopMoveCalculator extends ChessPieceCalculator {

    private static final int[][] BISHOP_MOVES = {
            {1, 1}, {1, -1}, {-1, 1}, {-1, -1}
    };

    @Override
    Collection<ChessMove> possibleMoves(ChessBoard board, ChessPosition myPosition, ChessGame.TeamColor color) {
        List<ChessMove> moves = new ArrayList<>();

        for (int[] direction : BISHOP_MOVES) {
            int row = myPosition.getRow();
            int col = myPosition.getColumn();

            while (true) {
                row += direction[0];
                col += direction[1];
                ChessPosition newPosition = new ChessPosition(row, col);

                if (!isInBounds(newPosition)) {
                    break;
                }

                ChessPiece finishPiece = board.getPiece(newPosition);
                if (finishPiece == null) {
                    moves.add(new ChessMove(myPosition, newPosition, null));
                } else {
                    if (finishPiece.getTeamColor() != color) {
                        moves.add(new ChessMove(myPosition, newPosition, null));
                    }
                    break;
                }
            }


        }

        return moves;
    }

    private boolean isInBounds(ChessPosition position) {
        int row = position.getRow();
        int col = position.getColumn();
        return row >= 1 && row <= 8 && col >= 1 && col <= 8;
    }
}