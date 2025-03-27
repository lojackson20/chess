//package ui;
//
//public class ChessClient {
//}

package ui;

import java.util.Arrays;
import com.google.gson.Gson;
import server.ServerFacade;
import dataaccess.DataAccessException;


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
            case "list" -> listGames();
            case "create game" -> createGame(parameters);
            case "join" -> joinGame(parameters);
            case "signout" -> signOut();
            case "quit" -> "quit";
            default -> help();
        };
    }

    public String registerUser(String ... parameters) throws DataAccessException {
        if (parameters.length >= 1) {
            playerName = String.join("-", parameters);
            return String.format("You signed in as %s.", playerName);
        }
        throw new DataAccessException("Expected: <yourname>", 400);
    }

    public String signIn(String... params) throws DataAccessException {
        if (params.length >= 1) {
            playerName = String.join("-", params);
            return String.format("You signed in as %s.", playerName);
        }
        throw new DataAccessException("Expected: <yourname>", 400);
    }

    public String listGames() throws ResponseException {
        assertSignedIn();
        var games = server.listGames();
        var result = new StringBuilder();
        var gson = new Gson();
        for (var game : games) {
            result.append(gson.toJson(game)).append('\n');
        }
        return result.toString();
    }

    public String createGame(String... params) throws ResponseException {
        assertSignedIn();
        if (params.length >= 1) {
            var gameName = String.join(" ", params);
            var gameID = server.createGame(gameName);
            return String.format("Game '%s' created with ID: %d", gameName, gameID);
        }
        throw new ResponseException(400, "Expected: <game name>");
    }

    public String joinGame(String... params) throws ResponseException {
        assertSignedIn();
        if (params.length == 2) {
            var gameID = Integer.parseInt(params[0]);
            var color = params[1].toUpperCase();
            server.joinGame(gameID, color);
            return String.format("You joined game %d as %s", gameID, color);
        }
        throw new ResponseException(400, "Expected: <game id> <WHITE|BLACK>");
    }

    public String signOut() throws ResponseException {
        assertSignedIn();
        state = State.SIGNEDOUT;
        return String.format("%s signed out", playerName);
    }

    public String help() {
        if (state == State.SIGNEDOUT) {
            return """
                    - signIn <yourname>
                    - quit
                    """;
        }
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
