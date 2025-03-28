package ui;

import java.util.Arrays;
import com.google.gson.Gson;
import server.ServerFacade;
import dataaccess.DataAccessException;
import model.GameData;
import service.ListGamesResult;
import service.LoginRequest;
import service.LoginResult;
import service.RegisterRequest;
import service.RegisterResult;
import service.CreateGameRequest;
import service.JoinGameRequest;

public class ChessClient {
    private String playerName = null;
    private String authToken = null;
    private final ServerFacade server;
    private State state = State.SIGNEDOUT;

    public ChessClient(String serverUrl) {
        server = new ServerFacade(serverUrl);
    }

    public String evalPreLogin(String input) throws DataAccessException {
        var tokens = input.trim().split(" ");
        var cmd = (tokens.length > 0) ? tokens[0] : "help";
        var parameters = Arrays.copyOfRange(tokens, 1, tokens.length);
        return switch (cmd) {
            case "register" -> registerUser(parameters);
            case "signin" -> signIn(parameters);
            case "list" -> listGames();
            case "create" -> createGame(parameters);
            case "observe" -> observeGame();
            case "join" -> joinGame(parameters);
            case "signout" -> signOut();
            case "quit" -> "Goodbye!";
            default -> help();
        };
    }

    private String observeGame(String ... params) throws DataAccessException {
        assertSignedIn();
        if (params.length == 1) {
            int gameID = Integer.parseInt(params[0]);
            server.observeGame(authToken, gameID);
            drawBoard(true);
            return "You are observing game number " + gameID;
        }
        throw new DataAccessException("Expected: join <game id> <WHITE|BLACK>", 400);
    }

    public String registerUser(String... parameters) throws DataAccessException {
        if (parameters.length == 3) {
            RegisterResult result = server.registerUser(new RegisterRequest(parameters[0], parameters[1], parameters[2]));
            authToken = result.authToken();  // Store auth token after registration
            playerName = parameters[0];
            state = State.SIGNEDIN;
            return "Successfully registered and signed in as " + playerName;
        }
        throw new DataAccessException("Expected: register <username> <password> <email>", 400);
    }

    public String signIn(String... params) throws DataAccessException {
        if (params.length == 2) {
            LoginResult result = server.loginUser(new LoginRequest(params[0], params[1]));
            authToken = result.authToken();  // Store auth token after login
            playerName = params[0];
            state = State.SIGNEDIN;
            return "Signed in successfully as " + playerName;
        }
        throw new DataAccessException("Expected: signin <username> <password>", 400);
    }

    public String listGames() throws DataAccessException {
        assertSignedIn();
        String listedGame = "";
        ListGamesResult games = server.listGames(authToken);
        for (int i = 0; i < games.games().size(); ++i) {
            String gameName = games.games().get(i).gameName();
            String gameID = String.valueOf(games.games().get(i).gameID());
            String black = games.games().get(i).blackUsername();
            String white = games.games().get(i).whiteUsername();
            listedGame += "Game name:" + gameName + " Game ID:" + gameID + " Black user:" + black + " White user:" + white + "\n";

        }
        return listedGame;
    }

    public String createGame(String... params) throws DataAccessException {
        assertSignedIn();
        if (params.length >= 1) {
            var gameName = String.join(" ", params);
            CreateGameRequest request = new CreateGameRequest(authToken, gameName);
            GameData game = server.createGame(authToken, request);
            return "Game '" + gameName + "' created with ID: " + game.gameID();
        }
        throw new DataAccessException("Expected: create <game name>", 400);
    }

    public String joinGame(String... params) throws DataAccessException {
        assertSignedIn();
        if (params.length == 2) {
            int gameID = Integer.parseInt(params[0]);
            String color = params[1].toUpperCase();
            server.joinGame(authToken, new JoinGameRequest(authToken, color, gameID));
            drawBoard(true);
            return "You joined game " + gameID + " as " + color;
        }
        throw new DataAccessException("Expected: join <game id> <WHITE|BLACK>", 400);
    }

    public String signOut() throws DataAccessException {
        assertSignedIn();
        server.logoutUser(authToken);
        playerName = null;
        authToken = null;
        return "Signed out successfully.";
    }

    public String help() {
        if (state == State.SIGNEDOUT) {
            return """
                    Commands:
                    - register <username> <password> <email>
                    - signin <username> <password>
                    - quit
                    - help
                    """;
        }
        return """
                - list (lists available games)
                - create <game name>
                - join <game id> <WHITE|BLACK>
                - observe
                - signout
                - quit
                - help
                """;

    }

    private void assertSignedIn() throws DataAccessException {
        if (state == State.SIGNEDOUT) {
            throw new DataAccessException("You must sign in", 400);
        }
    }

    private static final String[] WHITE_PIECES = {"\u2656", "\u2658", "\u2657", "\u2655", "\u2654", "\u2657", "\u2658", "\u2656"};
    private static final String[] BLACK_PIECES = {"\u265C", "\u265E", "\u265D", "\u265B", "\u265A", "\u265D", "\u265E", "\u265C"};
    private static final String WHITE_PAWN = "\u2659";
    private static final String BLACK_PAWN = "\u265F";
    private static final String LIGHT_SQUARE = "\u25A1";
    private static final String DARK_SQUARE = "\u25A0";

    public void drawBoard(boolean isWhitePerspective) {
        String[][] board = new String[8][8];

        for (int i = 0; i < 8; i++) {
            board[0][i] = BLACK_PIECES[i];
            board[1][i] = BLACK_PAWN;
            board[6][i] = WHITE_PAWN;
            board[7][i] = WHITE_PIECES[i];
        }

        for (int row = 2; row < 6; row++) {
            for (int col = 0; col < 8; col++) {
                board[row][col] = ((row + col) % 2 == 0) ? LIGHT_SQUARE : DARK_SQUARE;
            }
        }

        System.out.println("  a b c d e f g h");

        // Print board
        for (int i = 0; i < 8; i++) {
            int row = isWhitePerspective ? (7 - i) : i;
            System.out.print((row + 1) + " ");
            for (int j = 0; j < 8; j++) {
                int col = isWhitePerspective ? j : (7 - j);
                System.out.print(board[row][col] + " ");
            }
            System.out.println(" " + (row + 1));
        }

        System.out.println("  a b c d e f g h");
    }

}

