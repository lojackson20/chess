package server;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import dataaccess.DataAccessException;
import dataaccess.DataAccess;
import dataaccess.MySQLDataAccess;
import model.AuthData;
import requestandresult.*;
import service.*;
import spark.*;

public class Server {
    private final UserService userService;
    private final GameService gameService;

    public Server() {
        DataAccess dataAccess = null;
        try {
            dataAccess = new MySQLDataAccess();
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }
        this.userService = new UserService(dataAccess);
        this.gameService = new GameService(dataAccess);
    }

    public Server(UserService userService, GameService gameService) {
        this.userService = userService;
        this.gameService = gameService;
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
        response.status(e.statusCode());
        response.body(e.toJson());
    }



    private Object clearApp(Request request, Response response) {
        userService.clearData();
        response.status(200);
        return new JsonObject();
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

    private Object listGames(Request request, Response response) throws DataAccessException {
        String authToken = request.headers("Authorization");
        if (authToken == null || authToken.isEmpty()) {
            response.status(401);
        }
        ListGamesResult games = gameService.listGames(request.headers("Authorization"));
        response.status(200);
        return new Gson().toJson(games);
    }


    private Object createGame(Request request, Response response) throws DataAccessException {
        String authToken = request.headers("Authorization");
        AuthData authData = userService.dataAccess.getAuth(authToken);
        if (authData == null) {
            throw new DataAccessException("Error: unauthorized", 401);
        }

        CreateGameRequest gameRequest = new Gson().fromJson(request.body(), CreateGameRequest.class);
        CreateGameResult gameID = gameService.createGame(authToken, gameRequest.gameName());
//        GameData newGame = new GameData(gameID, null, null, gameRequest.gameName(), new ChessGame());

        return new Gson().toJson(gameID);
    }

    private Object joinGame(Request request, Response response) throws DataAccessException {
        String authToken = request.headers("Authorization");
        if (authToken == null || authToken.isEmpty()) {
            throw new DataAccessException("Error: unauthorized", 401);
        }

        JoinGameRequest joinRequest = new Gson().fromJson(request.body(), JoinGameRequest.class);
        if (joinRequest == null || joinRequest.gameID() == null || joinRequest.playerColor() == null) {
            response.status(400);
            throw new DataAccessException("Error: bad request", 400);
        }

        JoinGameResult joinResult = gameService.joinGame(authToken, joinRequest.gameID(), joinRequest.playerColor());
        return new JsonObject();

    }




    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }
}
