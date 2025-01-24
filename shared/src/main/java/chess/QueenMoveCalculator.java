package chess;

import java.util.Collection;
import java.util.List;

public class QueenMoveCalculator extends ChessPieceCalculator {
    @Override
    Collection<ChessMove> possibleMoves(ChessBoard board, ChessPosition myPosition, ChessGame.TeamColor color) {
        return List.of();
    }

}
