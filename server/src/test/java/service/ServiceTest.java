package service;

import dataaccess.DataAccessException;
import dataaccess.MemoryDataAccess;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ServiceTest {

    private UserService userService;
    private GameService gameService;

    @BeforeEach
    void setUp() {
        MemoryDataAccess memoryDataAccess = new MemoryDataAccess();
        userService = new UserService(memoryDataAccess);
        gameService = new GameService(memoryDataAccess);
    }

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
    void createGame() throws DataAccessException {
        RegisterResult registerResult = userService.registerUser(new RegisterRequest("username", "password", "lukeoj@byu.edu"));

        Integer createGameResult = gameService.createGame(registerResult.authToken());
        assertEquals(0, createGameResult);
    }

    @Test
    void createGameBad() throws DataAccessException {
        assertThrows(DataAccessException.class, () -> {
            Integer createGameResult = gameService.createGame("authToken");
        });
    }

    @Test
    void listGames() throws DataAccessException {
        RegisterResult registerResult = userService.registerUser(new RegisterRequest("username", "password", "lukeoj@byu.edu"));

        ListGamesResult listGamesResult = gameService.listGames(registerResult.authToken());
        Integer createGameResult = gameService.createGame(registerResult.authToken());
//        assertEquals(listGamesResult);
    }

    @Test
    void listGamesBad() throws DataAccessException {
        assertThrows(DataAccessException.class, () -> {
            LogoutResult logoutResult1 = userService.logoutUser("authToken");
        });
    }

    @Test
    void joinGame() throws DataAccessException {
        RegisterResult registerResult = userService.registerUser(new RegisterRequest("username", "password", "lukeoj@byu.edu"));

        ListGamesResult listGamesResult = gameService.listGames(registerResult.authToken());
        Integer createGameResult = gameService.createGame(registerResult.authToken());
//        assertEquals(listGamesResult);
    }

    @Test
    void joinGameBad() throws DataAccessException {
        assertThrows(DataAccessException.class, () -> {
            LogoutResult logoutResult1 = userService.logoutUser("authToken");
        });
    }

}
