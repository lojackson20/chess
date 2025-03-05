package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class QueenMoveCalculator extends ChessPieceCalculator {

    private static final int[][] QUEEN_MOVES = {
            {0,1}, {0, -1}, {1, 0}, {-1, 0}, {1, 1}, {1, -1}, {-1, 1}, {-1, -1}
    };

    @Override
    Collection<ChessMove> possibleMoves(ChessBoard board, ChessPosition myPosition, ChessGame.TeamColor color) {
        return possibleMovesHelper(board, myPosition, color, QUEEN_MOVES);
    }
}

