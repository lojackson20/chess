package server;

import com.google.gson.Gson;
import service.RegisterRequest;
import service.RegisterResult;
import service.UserService;
import spark.*;

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

        //This line initializes the server and can be removed once you have a functioning endpoint 
        Spark.init();

        Spark.awaitInitialization();
        return Spark.port();
    }

    private Object registerUser(Request request, Response response) {
        RegisterRequest registerRequest = new Gson().fromJson(request.body(), RegisterRequest.class);
        RegisterResult registerResult = userService.registerUser(registerRequest);
        return new Gson().toJson(registerResult);
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }
}
