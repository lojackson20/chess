package service;

import dataaccess.DataAccessException;
import dataaccess.MemoryDataAccess;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ServiceTest {

    UserService userService = new UserService(new MemoryDataAccess());
    GameService gameService = new GameService(new MemoryDataAccess());

    @Test
    void registerUser() throws DataAccessException {
        RegisterResult registerResult = userService.registerUser(new RegisterRequest("username", "password", "lukeoj@byu.edu"));
        assertEquals("username", registerResult.username());
    }

    @Test
    void registerUserBad() throws DataAccessException {
        Exception exception = assertThrows(DataAccessException.class, () -> {
            RegisterResult registerResult = userService.registerUser(new RegisterRequest("username", "", "lukeoj@byu.edu"));
        });
    }

    @Test
    void loginUser() throws DataAccessException {
        RegisterResult registerResult = userService.registerUser(new RegisterRequest("username", "password", "lukeoj@byu.edu"));

        LoginResult loginResult = userService.loginUser(new LoginRequest("username", "password"));
        assertEquals("username", loginResult.username());
    }

    @Test
    void loginUserBad() throws DataAccessException {
        assertThrows(DataAccessException.class, () -> {
            LoginResult loginResult1 = userService.loginUser(new LoginRequest("username", "password"));
        });
    }

    @Test
    void logoutUser() throws DataAccessException {
        RegisterResult registerResult = userService.registerUser(new RegisterRequest("username", "password", "lukeoj@byu.edu"));

        LogoutResult logoutResult = userService.logoutUser(registerResult.authToken());
        assertEquals(new LogoutResult(), logoutResult);
    }

    @Test
    void logoutUserBad() throws DataAccessException {
        assertThrows(DataAccessException.class, () -> {
            LogoutResult logoutResult1 = userService.logoutUser("authToken");
        });
    }

    @Test
    void listGames() throws DataAccessException {
        RegisterResult registerResult = userService.registerUser(new RegisterRequest("username", "password", "lukeoj@byu.edu"));

        ListGamesResult listGamesResult = gameService.listGames(registerResult.authToken());
        assertEquals(listGamesResult);
    }

    @Test
    void listGamesBad() throws DataAccessException {
        assertThrows(DataAccessException.class, () -> {
            LogoutResult logoutResult1 = userService.logoutUser("authToken");
        });
    }

}
