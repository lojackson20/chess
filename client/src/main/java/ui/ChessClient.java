package ui;

import java.util.Arrays;

import chess.ChessBoard;
import chess.ChessGame;
import chess.ChessPiece;
import chess.ChessPosition;
import server.ServerFacade;
import dataaccess.DataAccessException;
import model.GameData;
import requestandresult.ListGamesResult;
import requestandresult.LoginRequest;
import requestandresult.LoginResult;
import requestandresult.RegisterRequest;
import requestandresult.RegisterResult;
import requestandresult.CreateGameRequest;
import requestandresult.JoinGameRequest;
import static ui.EscapeSequences.*;

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
            case "observe" -> observeGame(parameters);
            case "join" -> joinGame(parameters);
            case "signout" -> signOut();
            case "quit" -> "Goodbye!";
            default -> help();
        };
    }

    private String observeGame(String ... params) throws DataAccessException {
        assertSignedIn();
        if (params.length == 1) {
            int gameID;
            try {
                gameID = Integer.parseInt(params[0]);
            } catch (NumberFormatException e) {
                return "Invalid game ID. Please enter a number.";
            }

            // Check if the game exists before attempting to observe
            ListGamesResult games = server.listGames(authToken);
            boolean gameExists = games.games().stream().anyMatch(game -> game.gameID() == gameID);

            if (!gameExists) {
                return "That game doesn't exist!";
            }

            try {
                GameData gameData = server.observeGame(authToken, gameID);
                drawBoard(true, gameData);
                return "You are now observing game " + gameID;
            } catch (Exception e) {
                return "Failed to observe game. Please try again.";
            }
        }
        return "Expected: observe <game id>";
    }

    public String registerUser(String... parameters) throws DataAccessException {
        try {
            if (parameters.length == 3) {
                RegisterResult result = server.registerUser(new RegisterRequest(parameters[0], parameters[1], parameters[2]));
                authToken = result.authToken();
                playerName = parameters[0];
                state = State.SIGNEDIN;
                return "Successfully registered and signed in as " + playerName;
            }
        } catch (DataAccessException e) {
            return "user is already taken";
        }
        return "Expected: register <username> <password> <email>";
//        throw new DataAccessException("Expected: register <username> <password> <email>", 400);
    }

    public String signIn(String... params) throws DataAccessException {
        try {
            if (params.length == 2) {
                LoginResult result = server.loginUser(new LoginRequest(params[0], params[1]));
                authToken = result.authToken();
                playerName = params[0];
                state = State.SIGNEDIN;
                return "Signed in successfully as " + playerName;
            }
        } catch (DataAccessException e) {
            return "User doesn't exist or wrong password, try registering";
        }
        return "Expected: signin <username> <password>";
//        throw new DataAccessException("Expected: signin <username> <password>", 400);
    }

    public String listGames() throws DataAccessException {
        assertSignedIn();
        String listedGame = "";
        ListGamesResult games = server.listGames(authToken);
        for (int i = 1; i < games.games().size(); ++i) {
            String gameName = games.games().get(i).gameName();
//            String gameID = String.valueOf(games.games().get(i).gameID());
            String black = games.games().get(i).blackUsername();
            String white = games.games().get(i).whiteUsername();
            listedGame += "Game name:" + gameName + " Game ID:" + i + " Black user:" + black + " White user:" + white + "\n";

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
            int gameID;
            try {
                gameID = Integer.parseInt(params[0]);
            } catch (NumberFormatException e) {
                return "Invalid game ID. Please enter a number.";
            }

            String color = params[1].toUpperCase();

            try {
                GameData gameData = server.joinGame(authToken, new JoinGameRequest(authToken, color, gameID));
                drawBoard(!color.equals("BLACK"), gameData);
                return "You joined game " + gameID + " as " + color;
            } catch (DataAccessException e) {
                if (!color.equals("BLACK") && !color.equals("WHITE")) {
                    return "That is not a valid color, try BLACK or WHITE";
                }
                ListGamesResult games = server.listGames(authToken);
                boolean gameExists = games.games().stream().anyMatch(game -> game.gameID() == gameID);

                if (!gameExists) {
                    return "That game doesn't exist!";
                }
                return "Failed to join game: Game is full";
            }
        }
        return "Expected: join <game id> <WHITE|BLACK>";
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


    public void drawBoard(boolean isWhitePerspective, GameData gameData) {
        ChessBoard board = gameData.game().getBoard();

        if (isWhitePerspective) {
            for (int i = 8; i >= 1; i--) {
                System.out.print(i);
                for (int j = 1; j <= 8; j++) {
                    ChessPiece piece = board.getPiece(new ChessPosition(i, j));
                    printSquare(piece, new ChessPosition(i, j));
                }
                System.out.print(RESET_BG_COLOR);
                System.out.print("\n");
            }
            System.out.print("  A ");
            System.out.print("  B ");
            System.out.print(" C ");
            System.out.print("  D ");
            System.out.print("  E ");
            System.out.print(" F ");
            System.out.print("  G ");
            System.out.print("  H ");
            System.out.print("\n");

        } else {
            for (int i = 1; i <= 8; i++) {
                System.out.print(i);
                for (int j = 8; j >= 1; j--) {
                    ChessPiece piece = board.getPiece(new ChessPosition(i, j));
                    printSquare(piece, new ChessPosition(i, j));
                }
                System.out.print(RESET_BG_COLOR);
                System.out.print("\n");
            }
            System.out.print("  H ");
            System.out.print("  G ");
            System.out.print("  F ");
            System.out.print(" E ");
            System.out.print("  D ");
            System.out.print(" C ");
            System.out.print("  B ");
            System.out.print("  A ");
            System.out.print("\n");
        }
    }


    public void printSquare(ChessPiece piece, ChessPosition position) {
        if ((position.getRow() + position.getColumn()) % 2 == 0) {
            System.out.print(SET_BG_COLOR_DARK_GREEN);
            System.out.print(whatPiece(piece));
        } else {
            System.out.print(SET_BG_COLOR_WHITE);
            System.out.print(whatPiece(piece));
        }
    }

    public String whatPiece(ChessPiece piece) {
        if (piece == null) {
            return EMPTY;
        }
        if (piece.getPieceType() == ChessPiece.PieceType.KING && piece.getTeamColor() == ChessGame.TeamColor.BLACK) {
            return BLACK_KING;
        }
        if (piece.getPieceType() == ChessPiece.PieceType.KING && piece.getTeamColor() == ChessGame.TeamColor.WHITE) {
            return WHITE_KING;
        }
        if (piece.getPieceType() == ChessPiece.PieceType.QUEEN && piece.getTeamColor() == ChessGame.TeamColor.BLACK) {
            return BLACK_QUEEN;
        }
        if (piece.getPieceType() == ChessPiece.PieceType.QUEEN && piece.getTeamColor() == ChessGame.TeamColor.WHITE) {
            return WHITE_QUEEN;
        }
        if (piece.getPieceType() == ChessPiece.PieceType.ROOK && piece.getTeamColor() == ChessGame.TeamColor.BLACK) {
            return BLACK_ROOK;
        }
        if (piece.getPieceType() == ChessPiece.PieceType.ROOK && piece.getTeamColor() == ChessGame.TeamColor.WHITE) {
            return WHITE_ROOK;
        }
        if (piece.getPieceType() == ChessPiece.PieceType.KNIGHT && piece.getTeamColor() == ChessGame.TeamColor.BLACK) {
            return BLACK_KNIGHT;
        }
        if (piece.getPieceType() == ChessPiece.PieceType.KNIGHT && piece.getTeamColor() == ChessGame.TeamColor.WHITE) {
            return WHITE_KNIGHT;
        }
        if (piece.getPieceType() == ChessPiece.PieceType.BISHOP && piece.getTeamColor() == ChessGame.TeamColor.BLACK) {
            return BLACK_BISHOP;
        }
        if (piece.getPieceType() == ChessPiece.PieceType.BISHOP && piece.getTeamColor() == ChessGame.TeamColor.WHITE) {
            return WHITE_BISHOP;
        }
        if (piece.getPieceType() == ChessPiece.PieceType.PAWN && piece.getTeamColor() == ChessGame.TeamColor.BLACK) {
            return BLACK_PAWN;
        }
        if (piece.getPieceType() == ChessPiece.PieceType.PAWN && piece.getTeamColor() == ChessGame.TeamColor.WHITE) {
            return WHITE_PAWN;
        }
        return "hello";
    }
}

// better error message for signin that doesnt exist----***
// error for reregistering ----***
// better error message for when game doesnt exist
// joining as an invalid color----***
