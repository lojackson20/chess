//package ui.websocket;
//
//public class WebSocketFacade {
//    public void connect() {
//
//    }
//}

package ui.websocket;

import chess.ChessBoard;
import chess.ChessMove;
import com.google.gson.Gson;
import dataaccess.DataAccessException;
import websocket.commands.MakeMoveCommand;
import websocket.commands.UserGameCommand;
import websocket.messages.*;

import javax.websocket.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

//need to extend Endpoint for websocket to work properly
public class WebSocketFacade extends Endpoint {

    Session session;
    NotificationHandler notificationHandler;


    public WebSocketFacade(String url, NotificationHandler notificationHandler) throws DataAccessException {
        try {
            url = url.replace("http", "ws");
            URI socketURI = new URI(url + "/ws");
            this.notificationHandler = notificationHandler;

            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            this.session = container.connectToServer(this, socketURI);

            //set message handler
            this.session.addMessageHandler(new MessageHandler.Whole<String>() {
                @Override
                public void onMessage(String message) {
                    notificationHandler.notify(message);
                }
            });
        } catch (DeploymentException | IOException | URISyntaxException ex) {
            throw new DataAccessException(ex.getMessage(), 500);
        }
    }

    //Endpoint requires this method, but you don't have to do anything
    @Override
    public void onOpen(Session session, EndpointConfig endpointConfig) {
    }

    public void connect(String authToken, Integer gameID) throws DataAccessException {
        try {
            var action = new UserGameCommand(UserGameCommand.CommandType.CONNECT, authToken, gameID);
            this.session.getBasicRemote().sendText(new Gson().toJson(action));
        } catch (IOException ex) {
            throw new DataAccessException(ex.getMessage(), 500);
        }
    }

    public void leave(String authToken, Integer gameID) throws DataAccessException {
        try {
            var action = new UserGameCommand(UserGameCommand.CommandType.LEAVE, authToken, gameID);
            this.session.getBasicRemote().sendText(new Gson().toJson(action));
//            this.session.close();
        } catch (IOException ex) {
            throw new DataAccessException(ex.getMessage(), 500);
        }
    }

    public void makeMove(String authToken, Integer gameID, ChessMove move) {
        if (authToken == null || gameID == null || session == null || !session.isOpen()) {
            System.out.println("Cannot send move: not connected.");
            return;
        }

        var command = new MakeMoveCommand(UserGameCommand.CommandType.MAKE_MOVE, authToken, gameID, move);
        try {
            session.getBasicRemote().sendText(new Gson().toJson(command));
        } catch (IOException e) {
            System.out.println("Failed to send move: " + e.getMessage());
        }
    }

    public void resign(String authToken, Integer gameID) {
        if (authToken == null || gameID == null || session == null || !session.isOpen()) {
            System.out.println("Cannot resign: not connected.");
            return;
        }

        var command = new UserGameCommand(UserGameCommand.CommandType.RESIGN, authToken, gameID);
        try {
            session.getBasicRemote().sendText(new Gson().toJson(command));
        } catch (IOException e) {
            System.out.println("Failed to send resign command: " + e.getMessage());
        }
    }
}
