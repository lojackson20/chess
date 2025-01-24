package chess;

import java.util.Collection;
import java.util.List;

public class BishopMoveCalculator extends ChessPieceCalculator {
    @Override
    Collection<ChessMove> possibleMoves(ChessBoard board, ChessPosition myPosition) {
        return List.of();
    }
}
