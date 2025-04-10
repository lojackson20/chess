//package server.websocket;
//
//public class WebSocketHandler {
//    //
//}
package server.websocket;

import chess.ChessGame;
import chess.ChessMove;
import chess.InvalidMoveException;
import com.google.gson.Gson;
import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import dataaccess.MySQLDataAccess;
import model.AuthData;
import model.GameData;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import websocket.commands.MakeMoveCommand;
import websocket.commands.UserGameCommand;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.Notification;
import websocket.messages.ServerMessage;

import java.io.IOException;
import java.util.Objects;
import java.util.Timer;


@WebSocket
public class WebSocketHandler {

    private final ConnectionManager connections = new ConnectionManager();
    private final MySQLDataAccess dataAccess = new MySQLDataAccess();

    public WebSocketHandler() throws DataAccessException {
    }

    @OnWebSocketMessage
    // when i recieve a message what do i do? from (UserGameCommands)
    public void onMessage(Session session, String message) throws IOException, DataAccessException {
//        UserGameCommand command = new Gson().fromJson(message, UserGameCommand.class);
        Gson gson = new Gson();
        UserGameCommand baseCommand = gson.fromJson(message, UserGameCommand.class);

        switch (baseCommand.getCommandType()) {
            case CONNECT -> connect(baseCommand.getGameID(), baseCommand.getAuthToken(), session);
            case MAKE_MOVE -> {
                MakeMoveCommand moveCommand = gson.fromJson(message, MakeMoveCommand.class);
                makeMove(moveCommand.getGameID(), moveCommand.getAuthToken(), moveCommand.getMove(), session);
            }
//            case LEAVE -> leave();
//            case RESIGN -> resign();
        }
    }

    private void connect(Integer gameID, String authToken, Session session) throws IOException, DataAccessException {
        GameData game = dataAccess.getGame(gameID);
        if (game == null) {
            ErrorMessage error = new ErrorMessage(ServerMessage.ServerMessageType.ERROR, "Invalid game ID");
            session.getRemote().sendString(new Gson().toJson(error));
            return;
        }

        AuthData authData = dataAccess.getAuth(authToken);
        if (authData == null) {
            ErrorMessage error = new ErrorMessage(ServerMessage.ServerMessageType.ERROR, "Invalid authtoken");
            session.getRemote().sendString(new Gson().toJson(error));
            return;
        }

        LoadGameMessage message = new LoadGameMessage(ServerMessage.ServerMessageType.LOAD_GAME, game);
        String sendMessage =  new Gson().toJson(message);
        session.getRemote().sendString(sendMessage);
        connections.add(authToken, session);

        String userColor;

        if (Objects.equals(game.whiteUsername(), authData.username())) {
            userColor = "White";
        } else if (Objects.equals(game.blackUsername(), authData.username())) {
            userColor = "Black";
        } else {
            userColor = "observer";
        }
        var allMessage = dataAccess.getAuth(authToken).username() + " has joined the game as " + userColor;
        var notification = new Notification(ServerMessage.ServerMessageType.NOTIFICATION, allMessage);
        connections.broadcast(authData.authToken(), notification);
    }

    private void makeMove(Integer gameID, String authToken, ChessMove move, Session session) throws IOException, DataAccessException {
        GameData game = dataAccess.getGame(gameID);
        if (game == null) {
            if (session != null) {
                ErrorMessage error = new ErrorMessage(ServerMessage.ServerMessageType.ERROR, "Invalid game ID");
                session.getRemote().sendString(new Gson().toJson(error));
            }
            return;
        }

        AuthData authData = dataAccess.getAuth(authToken);
        if (authData == null) {
            if (session != null) {
                ErrorMessage error = new ErrorMessage(ServerMessage.ServerMessageType.ERROR, "Invalid authtoken");
                session.getRemote().sendString(new Gson().toJson(error));
            }
            return;
        }

        ChessGame chessGame = game.game();
        String username = authData.username();
        boolean isWhite = Objects.equals(username, game.whiteUsername());
        boolean isBlack = Objects.equals(username, game.blackUsername());

        if ((chessGame.getTeamTurn() == ChessGame.TeamColor.WHITE && !isWhite) ||
                (chessGame.getTeamTurn() == ChessGame.TeamColor.BLACK && !isBlack)) {
            if (session != null) {
                ErrorMessage error = new ErrorMessage(ServerMessage.ServerMessageType.ERROR, "Not your turn");
                session.getRemote().sendString(new Gson().toJson(error));
            }
            return;
        }

        try {
            chessGame.makeMove(move);
        } catch (InvalidMoveException e) {
            if (session != null) {
                ErrorMessage error = new ErrorMessage(ServerMessage.ServerMessageType.ERROR, "Illegal move: " + e.getMessage());
                session.getRemote().sendString(new Gson().toJson(error));
            }
            return;
        }

        dataAccess.updateGame(game);

        LoadGameMessage loadMessage = new LoadGameMessage(ServerMessage.ServerMessageType.LOAD_GAME, game);
        connections.broadcast(authToken, loadMessage);

        String moveMessage = username + " moved from " + move.getStartPosition() + " to " + move.getEndPosition();
        Notification moveNotification = new Notification(ServerMessage.ServerMessageType.NOTIFICATION, moveMessage);
        connections.broadcast(authToken, moveNotification);

        if (chessGame.isInCheckmate(chessGame.getTeamTurn())) {
            Notification checkmate = new Notification(ServerMessage.ServerMessageType.NOTIFICATION, "Checkmate!");
            connections.broadcast(authToken, checkmate);
        } else if (chessGame.isInStalemate(chessGame.getTeamTurn())) {
            Notification stalemate = new Notification(ServerMessage.ServerMessageType.NOTIFICATION, "Stalemate.");
            connections.broadcast(authToken, stalemate);
        } else if (chessGame.isInCheck(chessGame.getTeamTurn())) {
            Notification check = new Notification(ServerMessage.ServerMessageType.NOTIFICATION, "Check!");
            connections.broadcast(authToken, check);
        }
    }
//
//    public void leave(String petName, String sound) throws ResponseException {
//        try {
//            var message = String.format("%s says %s", petName, sound);
//            var notification = new Notification(Notification.Type.NOISE, message);
//            connections.broadcast("", notification);
//        } catch (Exception ex) {
//            throw new ResponseException(500, ex.getMessage());
//        }
//    }
//
//    public void resign() {
//
//    }
}
