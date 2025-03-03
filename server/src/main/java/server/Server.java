package server;

import com.google.gson.Gson;
import dataaccess.DataAccessException;
import service.RegisterRequest;
import service.RegisterResult;
import service.UserService;
import spark.*;

import java.lang.module.ResolutionException;

public class Server {
    private final UserService userService;

    public Server(UserService userService) {
        this.userService = userService;
    }

    public int run(int desiredPort) {

        Spark.port(desiredPort);

        Spark.staticFiles.location("web");

        // Register your endpoints and handle exceptions here.
        Spark.post("/user", this::registerUser);
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

    private Object registerUser(Request request, Response response) throws DataAccessException {
        RegisterRequest registerRequest = new Gson().fromJson(request.body(), RegisterRequest.class);
        RegisterResult registerResult = userService.registerUser(registerRequest);
        return new Gson().toJson(registerResult);
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }
}
