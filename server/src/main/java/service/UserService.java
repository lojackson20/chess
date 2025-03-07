package service;

import dataaccess.DataAccess;

import java.util.ArrayList;
import java.util.Objects;
import java.util.UUID;

import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import model.UserData;
import model.AuthData;
import model.GameData;


public class UserService {
    public final DataAccess dataAccess;

    public void clearData() {
        dataAccess.clear();
    }

    public UserService(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    public RegisterResult registerUser(RegisterRequest registerRequest) throws DataAccessException {
        // look at diagram, create authtoken, send to auth dao, that creates it, then returns it
        if (registerRequest == null || registerRequest.username() == null || Objects.equals(registerRequest.password(), null)) {
            throw new DataAccessException("{message: Error: bad request}", 400);
        }

        UserData newUser = new UserData(registerRequest.username(), registerRequest.password(), registerRequest.email());
        boolean userCreated = dataAccess.createUser(newUser);

        if (!userCreated) {
            return null;
        }

        String authToken = UUID.randomUUID().toString();
        AuthData authData = new AuthData(registerRequest.username(), authToken);
        dataAccess.createAuth(authData);

        return new RegisterResult(registerRequest.username(), authToken);
    }

    public LoginResult loginUser(LoginRequest loginRequest) throws DataAccessException {
        if (loginRequest.username() == null || loginRequest.username().isEmpty()
                || loginRequest.password() == null || loginRequest.password().isEmpty()) {
            throw new DataAccessException("message: Error: bad request", 400);
        }

        UserData user = dataAccess.getUser(loginRequest.username());
        if (user == null || !user.password().equals(loginRequest.password())) {
            throw new DataAccessException("message: Error: unauthorized", 401);
        }

        String authToken = UUID.randomUUID().toString();
        AuthData authData = new AuthData(loginRequest.username(), authToken);
        dataAccess.createAuth(authData);

        return new LoginResult(authData.username(), authData.authToken());
    }


    public LogoutResult logoutUser(String authToken) throws DataAccessException {
        if (authToken == null || authToken.isEmpty()) {
            throw new DataAccessException("{message: Error: bad request}", 400);
        }

        AuthData authData = dataAccess.getAuth(authToken);
        if (authData == null) {
            throw new DataAccessException("{message: Error: unauthorized}", 401);
        }

        boolean notRemoved = dataAccess.deleteAuth(authToken);
        if (notRemoved) {
            throw new DataAccessException("{message: Error: internal server error}", 500);
        }

        return new LogoutResult();
    }

}
