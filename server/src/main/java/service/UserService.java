package service;

import dataaccess.DataAccess;

import java.util.Objects;
import java.util.UUID;

import dataaccess.DataAccessException;
import model.UserData;
import model.AuthData;
import org.mindrot.jbcrypt.BCrypt;
import requestandresult.*;


public class UserService {
    public final DataAccess dataAccess;

    public void clearData() {
        try {
            dataAccess.clear();
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public UserService(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    public RegisterResult registerUser(RegisterRequest registerRequest) throws DataAccessException {
        // look at diagram, create authtoken, send to auth dao, that creates it, then returns it
        if (registerRequest == null || registerRequest.username() == null || Objects.equals(registerRequest.password(), null)) {
            throw new DataAccessException("{message: Error: bad request}", 400);
        }

        String hashedPassword = BCrypt.hashpw(registerRequest.password(), BCrypt.gensalt());
        UserData newUser = new UserData(registerRequest.username(), hashedPassword, registerRequest.email());
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
        String hashedPassword = BCrypt.hashpw(loginRequest.password(), BCrypt.gensalt());
        if (loginRequest.username() == null || loginRequest.username().isEmpty()
                || hashedPassword == null || hashedPassword.isEmpty()) {
            throw new DataAccessException("message: Error: bad request", 400);
        }

        UserData user = dataAccess.getUser(loginRequest.username());
        if (user == null || !BCrypt.checkpw(loginRequest.password(), user.password())) {
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
        if (!notRemoved) {
            throw new DataAccessException("{message: Error: internal server error}", 500);
        }

        return new LogoutResult();
    }

}
