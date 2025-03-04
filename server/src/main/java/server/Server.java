package server;

import com.google.gson.Gson;
import dataaccess.DataAccessException;
import dataaccess.DataAccess;
import model.AuthData;
import model.GameData;
import service.*;
import spark.*;

import java.util.ArrayList;

public class Server {
    private final UserService userService;

    public Server(UserService userService) {
        this.userService = userService;
    }

    public int run(int desiredPort) {

        Spark.port(desiredPort);

        Spark.staticFiles.location("web");

        // Register your endpoints and handle exceptions here.
        Spark.delete("/db", this::clearApp);
        Spark.post("/user", this::registerUser);
        Spark.post("/session", this::loginUser);
        Spark.delete("/session", this::logoutUser);
        Spark.get("/game", this::listGames);
        Spark.post("/game", this::createGame);
        Spark.put("/game", this::joinGame);

        Spark.exception(DataAccessException.class, this::exceptionHandler);

        //This line initializes the server and can be removed once you have a functioning endpoint 
        Spark.init();

        Spark.awaitInitialization();
        return Spark.port();
    }


    private void exceptionHandler(DataAccessException e, Request request, Response response) {
        response.status(e.StatusCode());
        response.body(e.toJson());
    }



    private Object clearApp(Request request, Response response) {
        dataAccess.clear();
        response.status(200);
        return new Gson().toJson("{message: \"Database cleared successfully\"}");
    }

    private Object registerUser(Request request, Response response) throws DataAccessException {
        RegisterRequest registerRequest = new Gson().fromJson(request.body(), RegisterRequest.class);
        RegisterResult registerResult = userService.registerUser(registerRequest);
        return new Gson().toJson(registerResult);
    }

    private Object loginUser(Request request, Response response) throws DataAccessException {
        LoginRequest loginRequest = new Gson().fromJson(request.body(), LoginRequest.class);
        LoginResult loginResult = userService.loginUser(loginRequest);
        return new Gson().toJson(loginResult);
    }

    private Object logoutUser(Request request, Response response) throws DataAccessException {
        String authToken = request.headers("Authorization");
        if (authToken == null || authToken.isEmpty()) {
            response.status(401);
            return new Gson().toJson(new LogoutResult());
        }
        LogoutResult logoutResult = userService.logoutUser(authToken);
        return new Gson().toJson(logoutResult);
    }

    private Object listGames(Request request, Response response) {
        ArrayList<GameData> games = dataAccess.listGames(null);
        response.status(200);
        return new Gson().toJson(games);
    }


    private Object createGame(Request request, Response response) {
        String authToken = request.headers("Authorization");
        AuthData authData = dataAccess.getAuth(authToken);
        if (authData == null) {
            response.status(401);
            return new Gson().toJson("{message: \"Error: unauthorized\"}");
        }

        CreateGameRequest gameRequest = new Gson().fromJson(request.body(), CreateGameRequest.class);
        int gameID = generateUniqueGameID();
        GameData newGame = new GameData(gameID, null, null, gameRequest.gameName(), new ChessGame());

        if (!dataAccess.createGame(newGame)) {
            response.status(400);
            return new Gson().toJson("{message: \"Error: game creation failed\"}");
        }

        response.status(200);
        return new Gson().toJson(new GameCreationResult(gameID));
    }


    private Object joinGame(Request request, Response response) {
        String authToken = request.headers("Authorization");
        AuthData authData = dataAccess.getAuth(authToken);
        if (authData == null) {
            response.status(401);
            return new Gson().toJson("{message: \"Error: unauthorized\"}");
        }

        JoinGameRequest joinRequest = new Gson().fromJson(request.body(), JoinGameRequest.class);
        GameData game = dataAccess.getGame(joinRequest.gameID());
        if (game == null) {
            response.status(400);
            return new Gson().toJson("{message: \"Error: game not found\"}");
        }

        GameData updatedGame;
        if (joinRequest.playerColor().equals("WHITE") && game.whiteUsername() == null) {
            updatedGame = new GameData(game.gameID(), authData.username(), game.blackUsername(), game.gameName(), game.game());
        } else if (joinRequest.playerColor().equals("BLACK") && game.blackUsername() == null) {
            updatedGame = new GameData(game.gameID(), game.whiteUsername(), authData.username(), game.gameName(), game.game());
        } else {
            response.status(403);
            return new Gson().toJson("{message: \"Error: seat already taken\"}");
        }

        dataAccess.updateGame(updatedGame);
        response.status(200);
        return new Gson().toJson("{message: \"Joined game successfully\"}");
    }

    private int generateUniqueGameID() {
        return (int) (Math.random() * 1000000);
    }


    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }
}
