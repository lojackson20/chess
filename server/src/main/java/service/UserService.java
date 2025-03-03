package service;

import dataaccess.DataAccess;
import java.util.UUID;

import dataaccess.DataAccessException;
import service.RegisterRequest;
import service.RegisterResult;
import model.UserData;
import model.AuthData;


public class UserService {
    private final DataAccess dataAccess;

    public UserService(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    public RegisterResult registerUser(RegisterRequest registerRequest) throws DataAccessException {
        // look at diagram, create authtoken, send to auth dao, that creates it, then returns it
        if (registerRequest == null || registerRequest.username().isEmpty() || registerRequest.password().isEmpty()) {
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


}
