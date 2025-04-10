//package server.websocket;
//
//public class WebSocketHandler {
//    //
//}
package server.websocket;

import com.google.gson.Gson;
import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import dataaccess.MySQLDataAccess;
import model.AuthData;
import model.GameData;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import websocket.commands.UserGameCommand;
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
        UserGameCommand command = new Gson().fromJson(message, UserGameCommand.class);
        switch (command.getCommandType()) {
            case CONNECT -> connect(command.getGameID(), command.getAuthToken(), session);
            case MAKE_MOVE -> makeMove();
//            case LEAVE -> leave();
//            case RESIGN -> resign();
        }
    }

    private void connect(Integer gameID, String authToken, Session session) throws IOException, DataAccessException {
        GameData game = dataAccess.getGame(gameID);
        if (game == null) {
            // Send error to client
            DataAccessException error = new DataAccessException(ServerMessage.ServerMessageType.ERROR, "Invalid game ID");
            session.getRemote().sendString(new Gson().toJson(error));
            return;
        }
        LoadGameMessage message = new LoadGameMessage(ServerMessage.ServerMessageType.LOAD_GAME, game);
        String sendMessage =  new Gson().toJson(message);
        session.getRemote().sendString(sendMessage);
        connections.add(authToken, session);
        String userColor;
        AuthData authData = dataAccess.getAuth(authToken);
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

    private void makeMove() throws IOException {
//        connections.remove(visitorName);
//        var message = String.format("%s left the shop", visitorName);
//        var notification = new Notification(Notification.Type.DEPARTURE, message);
//        connections.broadcast(visitorName, notification);
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
