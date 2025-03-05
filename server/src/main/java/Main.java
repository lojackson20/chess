import chess.*;
import dataaccess.DataAccess;
import dataaccess.MemoryDataAccess;
import server.Server;
import service.GameService;
import service.UserService;

public class Main {
    public static void main(String[] args) {
        var piece = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN);
        System.out.println("♕ 240 Chess Server: " + piece);

        DataAccess dataAccess = new MemoryDataAccess();

        UserService userService = new UserService(dataAccess);
        GameService gameService = new GameService(dataAccess);

        Server newServer = new Server(userService, gameService);
        newServer.run(8080);
    }
}