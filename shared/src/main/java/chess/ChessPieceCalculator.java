package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

abstract class ChessPieceCalculator {
    abstract Collection<ChessMove> possibleMoves(ChessBoard board, ChessPosition myPosition, ChessGame.TeamColor color);

    Collection<ChessMove> possibleMovesHelper(ChessBoard board, ChessPosition myPosition, ChessGame.TeamColor color, int [][] possibleMoves) {
        List<ChessMove> moves = new ArrayList<>();

        for (int[] direction : possibleMoves) {
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
