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
        return possibleMovesHelper(board, myPosition, color, BISHOP_MOVES);
    }
}