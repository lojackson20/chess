package ui;

import java.sql.Array;
import java.util.*;

import chess.*;
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
import ui.websocket.NotificationHandler;
import ui.websocket.WebSocketFacade;

import static ui.EscapeSequences.*;

public class ChessClient {
    private String playerName = null;
    private String authToken = null;
    private final ServerFacade server;
    private State state = State.SIGNEDOUT;
    private Map<Integer, Integer> gameIndexMap = new HashMap<>();
    private String serverUrl;
    private NotificationHandler notificationHandler;
    private Boolean inGame = false;
    private GameData currentGameData;
    private WebSocketFacade ws;

    public ChessClient(String serverUrl, NotificationHandler notificationHandler) throws DataAccessException {
        server = new ServerFacade(serverUrl);
        this.serverUrl = serverUrl;
        this.notificationHandler = notificationHandler;
        this.ws = new WebSocketFacade(serverUrl, notificationHandler);
    }

    public String getPlayerName() {
        return playerName;
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
            case "redraw" -> redraw();
            case "leave" -> leave();
            case "move" -> makeMove();
            case "resign" -> resign();
            case "highlight" -> highlight();
            case "quit" -> "Goodbye!";
            default -> help();
        };
    }

    private String redraw() {
        if (currentGameData == null) {
            return "No game currently loaded to redraw.";
        }

        boolean isWhite = currentGameData.whiteUsername().equals(playerName);
        drawBoard(isWhite, currentGameData, new ArrayList<>());
        return "Board redrawn";
    }

    private String leave() throws DataAccessException {

        ws.leave(authToken, currentGameData.gameID());
//        ws = null;

        currentGameData = null;
        inGame = false;
        return "You have left the game. Returning to main menu.";
    }

    private String makeMove() {
        if (currentGameData == null) {
            return "You are not in a game.";
        }

        try {
            System.out.println("Enter move in format: startRow startColLetter endRow endColLetter");
            Scanner scanner = new Scanner(System.in);
            String input = scanner.nextLine();
            String[] tokens = input.split(" ");
            if (tokens.length != 4) {
                return "Invalid format. Try again.";
            }

            int startRow = Integer.parseInt(tokens[0]);
            int startCol = columnLetterToNumber(tokens[1]);
            int endRow = Integer.parseInt(tokens[2]);
            int endCol = columnLetterToNumber(tokens[3]);

            ChessPosition start = new ChessPosition(startRow, startCol);
            ChessPosition end = new ChessPosition(endRow, endCol);
            ChessPiece movingPiece = currentGameData.game().getBoard().getPiece(start);

            ChessPiece.PieceType promotionPiece = null;

            if (movingPiece != null && movingPiece.getPieceType() == ChessPiece.PieceType.PAWN) {
                boolean isWhite = movingPiece.getTeamColor() == ChessGame.TeamColor.WHITE;
                if ((isWhite && endRow == 8) || (!isWhite && endRow == 1)) {
                    System.out.println("Choose a piece to promote to: QUEEN, ROOK, BISHOP, KNIGHT");
                    String choice = scanner.nextLine().trim().toUpperCase();
                    switch (choice) {
                        case "QUEEN" -> promotionPiece = ChessPiece.PieceType.QUEEN;
                        case "ROOK" -> promotionPiece = ChessPiece.PieceType.ROOK;
                        case "BISHOP" -> promotionPiece = ChessPiece.PieceType.BISHOP;
                        case "KNIGHT" -> promotionPiece = ChessPiece.PieceType.KNIGHT;
                        default -> {
                            return "Invalid promotion piece type.";
                        }
                    }
                }
            }

            ChessMove move = new ChessMove(start, end, promotionPiece);
            ws.makeMove(authToken, currentGameData.gameID(), move);
            return "Move sent.";
        } catch (Exception e) {
            return "Error processing move: " + e.getMessage();
        }
    }

    private String resign() {
        if (currentGameData == null) {
            return "You are not in a game.";
        }

        System.out.println("Are you sure you want to resign? (yes/no)");
        Scanner scanner = new Scanner(System.in);
        String input = scanner.nextLine();
        if ("yes".equalsIgnoreCase(input)) {
            ws.resign(authToken, currentGameData.gameID());
            return "You have resigned.";
        } else {
            return "Resignation canceled.";
        }
    }

    private String highlight() {
        System.out.println("Enter position to highlight legal moves for (row col):\n");
        Scanner scanner = new Scanner(System.in);
        String input = scanner.nextLine();
        String[] tokens = input.split(" ");
        if (tokens.length != 2) {
            return "Invalid input format.";
        }

        try {
            int row = Integer.parseInt(tokens[0]);
            int col = columnLetterToNumber(tokens[1]);
            ChessPosition pos = new ChessPosition(row, col);
            ChessPiece piece = currentGameData.game().getBoard().getPiece(pos);


            ArrayList<ChessMove> legalMoves = (ArrayList<ChessMove>) currentGameData.game().validMoves(pos);
            ArrayList<ChessPosition> highlightedPos = new ArrayList<>();
            for (ChessMove move : legalMoves) {
                highlightedPos.add(move.getEndPosition());
            }

            boolean isWhite = playerName.equals(currentGameData.whiteUsername());
            drawBoard(isWhite, currentGameData, highlightedPos);
            return "Legal moves highlighted.";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }


    private String observeGame(String ... params) throws DataAccessException {
        assertSignedIn();
        if (params.length == 1) {
            int gameIndex;
            try {
                gameIndex = Integer.parseInt(params[0]);
            } catch (NumberFormatException e) {
                return "Invalid game number. Please enter a valid number from the list.";
            }

            Integer gameID = gameIndexMap.get(gameIndex);
            if (gameID == null) {
                return "That game doesn't exist! Please list games again.";
            }

            try {
                GameData gameData = server.observeGame(authToken, gameID);
                this.currentGameData = gameData;
                this.inGame = true;

                this.ws.connect(authToken, gameID);

//                drawBoard(true, gameData, new ArrayList<>());
                return "You are now observing game " + gameIndex;
            } catch (Exception e) {
                return "Failed to observe game. Please try again.";
            }
        }
        return "Expected: observe <game number>";
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
    gameIndexMap.clear();

    StringBuilder listedGame = new StringBuilder();
    ListGamesResult games = server.listGames(authToken);

    for (int i = 0; i < games.games().size(); i++) {
        int realGameID = games.games().get(i).gameID();
        gameIndexMap.put(i + 1, realGameID);

        String gameName = games.games().get(i).gameName();
        String black = games.games().get(i).blackUsername();
        String white = games.games().get(i).whiteUsername();

        listedGame.append("[").append(i + 1).append("] ")
                .append("Game name: ").append(gameName)
                .append(" Black: ").append(black)
                .append(" White: ").append(white).append("\n");
    }
    return listedGame.toString();
}

    public String createGame(String... params) throws DataAccessException {
        assertSignedIn();
        if (params.length >= 1) {
            var gameName = String.join(" ", params);
            CreateGameRequest request = new CreateGameRequest(authToken, gameName);
            GameData game = server.createGame(authToken, request);
            return "Game '" + gameName + "' created";
        }
        throw new DataAccessException("Expected: create <game name>", 400);
    }

    public String joinGame(String... params) throws DataAccessException {
        assertSignedIn();
        if (params.length == 2) {
            int gameIndex;
            try {
                gameIndex = Integer.parseInt(params[0]);
            } catch (NumberFormatException e) {
                return "Invalid game number. Please enter a valid number from the list.";
            }

            Integer gameID = gameIndexMap.get(gameIndex);
            if (gameID == null) {
                return "That game doesn't exist! Please list games again.";
            }

            String color = params[1].toUpperCase();
            if (!color.equals("BLACK") && !color.equals("WHITE")) {
                return "Invalid color. Please enter 'WHITE' or 'BLACK'.";
            }

            try {
                // Join game on server and get fresh GameData
                GameData gameData = server.joinGame(authToken, new JoinGameRequest(authToken, color, gameID));

                // Set game state
                currentGameData = gameData;
                inGame = true;

                // Reconnect WebSocket and assign to field
                ws = new WebSocketFacade(serverUrl, notificationHandler);
                ws.connect(authToken, gameID);

                return "You joined game " + gameIndex + " as " + color;
            } catch (DataAccessException e) {
                return "Failed to join game: Game is full or invalid request.";
            }
        }
        return "Expected: join <game number> <WHITE|BLACK>";
    }

    public String signOut() throws DataAccessException {
        assertSignedIn();
        server.logoutUser(authToken);
        playerName = null;
        authToken = null;
        state = State.SIGNEDOUT;
        return "Signed out successfully.";
    }

    public String help() {
        if (inGame) {
            return """
                    - redraw
                    - leave
                    - move
                    - resign
                    - highlight
                    - help
                    """;
        }
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


    public void drawBoard(boolean isWhitePerspective, GameData gameData, ArrayList<ChessPosition> highlighted) {
        ChessBoard board = gameData.game().getBoard();

        if (isWhitePerspective) {
            for (int i = 8; i >= 1; i--) {
                System.out.print(i);
                for (int j = 1; j <= 8; j++) {
                    ChessPiece piece = board.getPiece(new ChessPosition(i, j));
                    printSquare(piece, new ChessPosition(i, j), highlighted);
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
                    printSquare(piece, new ChessPosition(i, j), highlighted);
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


    public void printSquare(ChessPiece piece, ChessPosition position, ArrayList<ChessPosition> highlighted) {
        if ((position.getRow() + position.getColumn()) % 2 == 0) {
            if (highlighted.contains(position)) {
                System.out.print(SET_BG_COLOR_RED);
                System.out.print(whatPiece(piece));
            } else {
                System.out.print(SET_BG_COLOR_DARK_GREEN);
                System.out.print(whatPiece(piece));
            }
        } else {
            if (highlighted.contains(position)) {
                System.out.print(SET_BG_COLOR_LIGHT_GREY);
                System.out.print(whatPiece(piece));
            } else {
                System.out.print(SET_BG_COLOR_WHITE);
                System.out.print(whatPiece(piece));
            }
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

    public void updateGameData (GameData gameData){
        currentGameData = gameData;
    }

    private int columnLetterToNumber(String col) {
        col = col.toLowerCase();
        if (col.length() != 1 || col.charAt(0) < 'a' || col.charAt(0) > 'h') {
            throw new IllegalArgumentException("Invalid column letter: " + col);
        }
        return col.charAt(0) - 'a' + 1;
    }

}

