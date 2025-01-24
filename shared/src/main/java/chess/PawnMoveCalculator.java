package chess;

import java.util.Collection;
import java.util.List;

public class PawnMoveCalculator extends ChessPieceCalculator {


    @Override
    Collection<ChessMove> possibleMoves(ChessBoard board, ChessPosition myPosition, ChessGame.TeamColor color) {
        // check to see if moving forward one is ok
            // check to see if moving one is valid and it is the last row -> add 4 moves( queen, rook, bishop, k)
            // check to see if in the first row (starting) can move forward two if so see if it is ok to move two
//        int direction = (color == ChessGame.TeamColor.WHITE) ? 1 : -1;
        int direction = 1;
        if (color == ChessGame.TeamColor.BLACK) {
            direction = -1;
        }


        // check both directions diagonally for opponent
            // if so and on board, add




        // if in starting position, can move forward 1 or 2 spaces

        // if front is clear, move forward one
        // if there is an enemy diagonal, move diagonal 1
        // if it reaches the end of the board add promotion
        return List.of();
    }

    private boolean isInStartingRow(int row, ChessGame.TeamColor color) {
        return (color == ChessGame.TeamColor.WHITE && row == 1) || (color == ChessGame.TeamColor.BLACK && row == 6);
    }

}
