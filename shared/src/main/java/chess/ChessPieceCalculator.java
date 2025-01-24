package chess;

import java.util.Collection;

abstract class ChessPieceCalculator {
    abstract Collection<ChessMove> possibleMoves(ChessBoard board, ChessPosition myPosition);
}
