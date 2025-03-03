package service;

import dataaccess.AuthDAO;
import dataaccess.UserDAO;

import java.util.UUID;

public class UserService {
    public RegisterResult registerUser(RegisterRequest registerRequest) {
        // look at diagram, create authtoken, send to auth dao, that creates it, then returns it
        final UserDAO userDao = new UserDAO();
        private final AuthDAO authTokenDao = new AuthDAO();

        public RegisterResult registerUser(RegisterRequest registerRequest) {
            // Check if user already exists
            if (userDao.getUser(registerRequest.getUsername()) != null) {
                return new RegisterResult(false, "Username already taken.", null);
            }

            // Create new user and store in database
            User newUser = new User(registerRequest.getUsername(), registerRequest.getPassword(), registerRequest.getEmail());
            userDao.createUser(newUser);

            // Generate auth token
            String token = UUID.randomUUID().toString();
            AuthToken authToken = new AuthToken(registerRequest.getUsername(), token);
            authTokenDao.createAuthToken(authToken);

            return new RegisterResult(true, "Registration successful.", token);
        }

    }
}
