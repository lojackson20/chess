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
        assertThrows(DataAccessException.class, () -> {
            RegisterResult registerResult = userService.registerUser(new RegisterRequest("username", "", "lukeoj@byu.edu"));
        });
    }

    @Test
    void loginUser() throws DataAccessException {
        userService.registerUser(new RegisterRequest("username", "password", "lukeoj@byu.edu"));

        LoginResult loginResult = userService.loginUser(new LoginRequest("username", "password"));
        assertEquals("username", loginResult.username());
    }

    @Test
    void loginUserBad() throws DataAccessException {
        assertThrows(DataAccessException.class, () -> {
            userService.loginUser(new LoginRequest("wrongUsername", "password"));
        });
    }

    @Test
    void logoutUser() throws DataAccessException {
        RegisterResult registerResult = userService.registerUser(new RegisterRequest("username", "password", "lukeoj@byu.edu"));

        LogoutResult logoutResult = userService.logoutUser(registerResult.authToken());
        assertNotNull(logoutResult);
    }

    @Test
    void logoutUserBad() throws DataAccessException {
        assertThrows(DataAccessException.class, () -> {
            userService.logoutUser("invalidAuthToken");
        });
    }

    @Test
    void createGame() throws DataAccessException {
        RegisterResult registerResult = userService.registerUser(new RegisterRequest("username", "password", "lukeoj@byu.edu"));

        CreateGameResult gameID = gameService.createGame(registerResult.authToken(), "game1");
        assertNotNull(gameID);
    }

    @Test
    void createGameBad() throws DataAccessException {
        assertThrows(DataAccessException.class, () -> {
            gameService.createGame("invalidAuthToken", "game1");
        });
    }

    @Test
    void listGames() throws DataAccessException {
        RegisterResult registerResult = userService.registerUser(new RegisterRequest("username", "password", "lukeoj@byu.edu"));

        gameService.createGame(registerResult.authToken(), "game1");
//        gameService.listGames(registerResult.authToken());
        ListGamesResult listGamesResult = gameService.listGames(registerResult.authToken());
        assertNotNull(listGamesResult);
        assertFalse(listGamesResult.games().isEmpty());
    }

    @Test
    void listGamesBad() throws DataAccessException {
        assertThrows(DataAccessException.class, () -> {
            gameService.listGames("invalidAuthToken");
        });
    }

    @Test
    void joinGame() throws DataAccessException {
        RegisterResult registerResult = userService.registerUser(new RegisterRequest("username", "password", "lukeoj@byu.edu"));

        CreateGameResult gameID = gameService.createGame(registerResult.authToken(), "game2");
        JoinGameResult joinGameResult = gameService.joinGame(registerResult.authToken(), gameID.gameID(), "Black");
        assertNull(joinGameResult);
    }

    @Test
    void joinGameBad() throws DataAccessException {
        assertThrows(DataAccessException.class, () -> {
            gameService.joinGame("invalidAuthToken", 99, "Black");
        });
    }

}
