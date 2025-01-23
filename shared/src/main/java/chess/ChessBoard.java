package chess;

/**
 * A chessboard that can hold and rearrange chess pieces.
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessBoard {
    private ChessPiece[][] myChessBoard;
    public ChessBoard() {
        myChessBoard = new ChessPiece[8][8];
    }

    /**
     * Adds a chess piece to the chessboard
     *
     * @param position where to add the piece to
     * @param piece    the piece to add
     */
    public void addPiece(ChessPosition position, ChessPiece piece) {
        myChessBoard[position.getRow() - 1][position.getColumn() - 1] = piece;
    }

    /**
     * Gets a chess piece on the chessboard
     *
     * @param position The position to get the piece from
     * @return Either the piece at the position, or null if no piece is at that
     * position
     */
    public ChessPiece getPiece(ChessPosition position) {
        return myChessBoard[position.getRow() - 1][position.getColumn() - 1];
    }

    /**
     * Sets the board to the default starting board
     * (How the game of chess normally starts)
     */
    public void resetBoard() {
        // make whole board null with 2 for loops
        // use add piece or what im doing

        //bottom white row
        myChessBoard[0][0] = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.ROOK);
        myChessBoard[0][1] = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.KNIGHT);
        myChessBoard[0][2] = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.BISHOP);
        myChessBoard[0][3] = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.QUEEN);
        myChessBoard[0][4] = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.KING);
        myChessBoard[0][5] = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.BISHOP);
        myChessBoard[0][6] = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.KNIGHT);
        myChessBoard[0][7] = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.ROOK);

        // white pawns
        myChessBoard[1][0] = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN);
        myChessBoard[1][1] = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN);
        myChessBoard[1][2] = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN);
        myChessBoard[1][3] = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN);
        myChessBoard[1][4] = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN);
        myChessBoard[1][5] = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN);
        myChessBoard[1][6] = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN);
        myChessBoard[1][7] = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN);

        // blank space
        myChessBoard[2][0] = new ChessPiece(null, null);
        myChessBoard[2][1] = new ChessPiece(null, null);
        myChessBoard[2][2] = new ChessPiece(null, null);
        myChessBoard[2][3] = new ChessPiece(null, null);
        myChessBoard[2][4] = new ChessPiece(null, null);
        myChessBoard[2][5] = new ChessPiece(null, null);
        myChessBoard[2][6] = new ChessPiece(null, null);
        myChessBoard[2][7] = new ChessPiece(null, null);

        // Blank space
        myChessBoard[3][0] = new ChessPiece(null, null);
        myChessBoard[3][1] = new ChessPiece(null, null);
        myChessBoard[3][2] = new ChessPiece(null, null);
        myChessBoard[3][3] = new ChessPiece(null, null);
        myChessBoard[3][4] = new ChessPiece(null, null);
        myChessBoard[3][5] = new ChessPiece(null, null);
        myChessBoard[3][6] = new ChessPiece(null, null);
        myChessBoard[3][7] = new ChessPiece(null, null);

        // More blank space
        myChessBoard[4][0] = new ChessPiece(null, null);
        myChessBoard[4][1] = new ChessPiece(null, null);
        myChessBoard[4][2] = new ChessPiece(null, null);
        myChessBoard[4][3] = new ChessPiece(null, null);
        myChessBoard[4][4] = new ChessPiece(null, null);
        myChessBoard[4][5] = new ChessPiece(null, null);
        myChessBoard[4][6] = new ChessPiece(null, null);
        myChessBoard[4][7] = new ChessPiece(null, null);

        // last blank space
        myChessBoard[5][0] = new ChessPiece(null, null);
        myChessBoard[5][1] = new ChessPiece(null, null);
        myChessBoard[5][2] = new ChessPiece(null, null);
        myChessBoard[5][3] = new ChessPiece(null, null);
        myChessBoard[5][4] = new ChessPiece(null, null);
        myChessBoard[5][5] = new ChessPiece(null, null);
        myChessBoard[5][6] = new ChessPiece(null, null);
        myChessBoard[5][7] = new ChessPiece(null, null);

        //black pawns
        myChessBoard[6][0] = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN);
        myChessBoard[6][1] = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN);
        myChessBoard[6][2] = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN);
        myChessBoard[6][3] = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN);
        myChessBoard[6][4] = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN);
        myChessBoard[6][5] = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN);
        myChessBoard[6][6] = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN);
        myChessBoard[6][7] = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN);

        // black pieces
        myChessBoard[7][0] = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.ROOK);
        myChessBoard[7][1] = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.KNIGHT);
        myChessBoard[7][2] = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.BISHOP);
        myChessBoard[7][3] = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.QUEEN);
        myChessBoard[7][4] = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.KING);
        myChessBoard[7][5] = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.BISHOP);
        myChessBoard[7][6] = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.KNIGHT);
        myChessBoard[7][7] = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.ROOK);

    }
}
