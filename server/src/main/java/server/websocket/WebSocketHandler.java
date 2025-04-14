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
        Gson gson = new Gson();
        UserGameCommand baseCommand = gson.fromJson(message, UserGameCommand.class);

        switch (baseCommand.getCommandType()) {
            case CONNECT -> connect(baseCommand.getGameID(), baseCommand.getAuthToken(), session);
            case MAKE_MOVE -> {
                MakeMoveCommand moveCommand = gson.fromJson(message, MakeMoveCommand.class);
                makeMove(moveCommand.getGameID(), moveCommand.getAuthToken(), moveCommand.getMove(), session);
            }
            case LEAVE -> leaveGame(baseCommand.getGameID(), baseCommand.getAuthToken(), session);
            case RESIGN -> resign(baseCommand.getGameID(), baseCommand.getAuthToken(), session);
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
        connections.add(authToken, session, gameID);

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
        connections.broadcastExcept(authData.authToken(), gameID, notification);
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
        
        if (!game.stillPlaying()) {
            ErrorMessage error = new ErrorMessage(ServerMessage.ServerMessageType.ERROR, "Game over, you can't move");
            session.getRemote().sendString(new Gson().toJson(error));
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
                ErrorMessage error = new ErrorMessage(ServerMessage.ServerMessageType.ERROR, "Can't make move (not your turn or game has ended)");
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
        connections.broadcastToGame(gameID, loadMessage);

        String moveMessage = username + " moved from " + move.getStartPosition().toString() + " to " + move.getEndPosition().toString();
        Notification moveNotification = new Notification(ServerMessage.ServerMessageType.NOTIFICATION, moveMessage);
        connections.broadcastExcept(authData.authToken(), gameID, moveNotification);

        GameData gameData = new GameData(
                game.gameID(),
                game.whiteUsername(),
                game.blackUsername(),
                game.gameName(),
                game.game(),
                false
        );
        if (chessGame.isInCheckmate(chessGame.getTeamTurn())) {
            Notification checkmate = new Notification(ServerMessage.ServerMessageType.NOTIFICATION, "Checkmate!");
            connections.broadcastToGame(gameID, checkmate);
            GameData newGameData = gameData;
            dataAccess.updateGame(newGameData);
        } else if (chessGame.isInStalemate(chessGame.getTeamTurn())) {
            Notification stalemate = new Notification(ServerMessage.ServerMessageType.NOTIFICATION, "Stalemate.");
            connections.broadcastToGame(gameID, stalemate);
            GameData newGameData = gameData;
            dataAccess.updateGame(newGameData);
        } else if (chessGame.isInCheck(chessGame.getTeamTurn())) {
            Notification check = new Notification(ServerMessage.ServerMessageType.NOTIFICATION, "Check!");
            connections.broadcastToGame(gameID, check);
        }
    }

    private void leaveGame(Integer gameID, String authToken, Session session) throws IOException, DataAccessException {
        GameData game = dataAccess.getGame(gameID);
        if (game == null) {
            ErrorMessage error = new ErrorMessage(ServerMessage.ServerMessageType.ERROR, "Invalid game ID");
            session.getRemote().sendString(new Gson().toJson(error));
            return;
        }

        AuthData authData = dataAccess.getAuth(authToken);
        if (authData == null) {
            ErrorMessage error = new ErrorMessage(ServerMessage.ServerMessageType.ERROR, "Invalid auth token");
            session.getRemote().sendString(new Gson().toJson(error));
            return;
        }

        connections.remove(authToken);
        if (authData.username().equals(game.blackUsername())) {
            GameData updatedGame = new GameData(gameID, game.whiteUsername(), null, game.gameName(), game.game());
            dataAccess.updateGame(updatedGame);
        }
        if (authData.username().equals(game.whiteUsername())) {
            GameData updatedGame = new GameData(gameID, null, game.blackUsername(), game.gameName(), game.game());
            dataAccess.updateGame(updatedGame);
        }


        String username = authData.username();
        String role;
        String whiteUsername = game.whiteUsername();
        String blackUsername = game.blackUsername();

        if (Objects.equals(username, whiteUsername)) {
            role = "White";
            game = new GameData(game.gameID(), null, blackUsername, game.gameName(), game.game());
            dataAccess.updateGame(game);
        } else if (Objects.equals(username, blackUsername)) {
            role = "Black";
            game = new GameData(game.gameID(), whiteUsername, null, game.gameName(), game.game());
            dataAccess.updateGame(game);
        } else {
            role = "observer";
        }

        String leaveMessage = username + " has left the game as " + role + ".";
        Notification notification = new Notification(ServerMessage.ServerMessageType.NOTIFICATION, leaveMessage);
        connections.broadcastExcept(authToken, gameID, notification);

    }

    private void resign(Integer gameID, String authToken, Session session) throws IOException, DataAccessException {
        GameData game = dataAccess.getGame(gameID);
        if (game == null) {
            ErrorMessage error = new ErrorMessage(ServerMessage.ServerMessageType.ERROR, "Invalid game ID");
            session.getRemote().sendString(new Gson().toJson(error));
            return;
        }

        AuthData authData = dataAccess.getAuth(authToken);
        if (authData == null) {
            ErrorMessage error = new ErrorMessage(ServerMessage.ServerMessageType.ERROR, "Invalid auth token");
            session.getRemote().sendString(new Gson().toJson(error));
            return;
        }

        String username = authData.username();
        boolean isWhite = username.equals(game.whiteUsername());
        boolean isBlack = username.equals(game.blackUsername());

        if (!isWhite && !isBlack) {
            ErrorMessage error = new ErrorMessage(ServerMessage.ServerMessageType.ERROR, "Only players can resign");
            session.getRemote().sendString(new Gson().toJson(error));
            return;
        }

        GameData updatedGame = new GameData(
                game.gameID(),
                null,
                null,
                game.gameName(),
                game.game(),
                false
        );

        dataAccess.updateGame(updatedGame);

        String resignMessage = username + " has resigned. Game over.";
        Notification notification = new Notification(ServerMessage.ServerMessageType.NOTIFICATION, resignMessage);
        connections.broadcastToGame(gameID, notification);
    }

}
