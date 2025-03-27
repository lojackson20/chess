//package ui;
//
//public class ChessClient {
//}

package ui;

import java.util.Arrays;
import com.google.gson.Gson;
import server.Server;
import server.ServerFacade;
import dataaccess.DataAccessException;
import service.LoginRequest;
import service.RegisterRequest;


public class ChessClient {
    private String playerName = null;
    private final ServerFacade server;
    private final String serverUrl;

    public ChessClient(String serverUrl, Repl repl) {
        server = new ServerFacade(serverUrl);
        this.serverUrl = serverUrl;
    }

    public String eval(String input) throws DataAccessException {
        var tokens = input.toLowerCase().split(" ");
        var cmd = (tokens.length > 0) ? tokens[0] : "help";
        var parameters = Arrays.copyOfRange(tokens, 1, tokens.length);
        return switch (cmd) {
            case "register" -> registerUser(parameters);
            case "signin" -> signIn(parameters);
//            case "list" -> listGames();
//            case "create game" -> createGame(parameters);
            case "join" -> joinGame(parameters);
            case "signout" -> signOut();
            case "quit" -> "quit";
            default -> help();
        };
    }

    public String registerUser(String ... parameters) throws DataAccessException {
        if (parameters.length == 3) {
            playerName = String.join("-", parameters);
//            return String.format("You signed in as %s.", playerName);
            server.registerUser(new RegisterRequest(parameters[0], parameters[1], parameters[2]));
            return "successfully registered!";
        }
        throw new DataAccessException("Expected: register 'username', 'password', 'email'", 400);
    }

    public String signIn(String... params) throws DataAccessException {
        if (params.length == 2) {
            playerName = String.join("-", params);
//            return String.format("You signed in as %s.", playerName);
            server.loginUser(new LoginRequest(params[0], params[1]));
            return "signed in successfully!";
        }
        throw new DataAccessException("Expected: signin 'username' 'password'", 400);
    }

    public String listGames() throws DataAccessException {
        assertSignedIn();
        var games = server.listGames();
        var result = new StringBuilder();
        var gson = new Gson();
        for (var game : games) {
            result.append(gson.toJson(game)).append('\n');
        }
        return result.toString();
    }

    public String createGame(String... params) throws DataAccessException {
        assertSignedIn();
        if (params.length >= 1) {
            var gameName = String.join(" ", params);
            var gameID = server.createGame(gameName);
            return String.format("Game '%s' created with ID: %d", gameName, gameID);
        }
        throw new DataAccessException("Expected: <game name>", 400);
    }

    public String joinGame(String... params) throws DataAccessException {
        if (params.length == 2) {
            var gameID = Integer.parseInt(params[0]);
            var color = params[1].toUpperCase();
            server.joinGame(color, gameID);
            return String.format("You joined game %d as %s", gameID, color);
        }
        throw new DataAccessException("Expected: <game id> <WHITE|BLACK>", 400);
    }

    public String signOut() throws DataAccessException {
        assertSignedIn();
        return String.format("%s signed out", playerName);
    }

    public String help() {
        return """
                - list
                - create <game name>
                - join <game id> <WHITE|BLACK>
                - signOut
                - quit
                """;
    }

    private void assertSignedIn() throws ResponseException {
        if (state == State.SIGNEDOUT) {
            throw new ResponseException(400, "You must sign in");
        }
    }
}
