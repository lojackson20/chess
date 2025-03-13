package dataaccess;

import dataaccess.DataAccessException;
import dataaccess.MemoryDataAccess;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import service.*;

import static org.junit.jupiter.api.Assertions.*;

public class DataAccessTests {

    private UserService userService;
    private GameService gameService;

    @BeforeEach
    void setUp() throws DataAccessException {
        MySQLDataAccess dataAccess = new MySQLDataAccess();
        userService = new UserService(dataAccess);
        gameService = new GameService(dataAccess);
        userService.clearData();
    }

    @Test
    void clear() throws DataAccessException {
        RegisterResult registerResult = userService.registerUser(new RegisterRequest("username", "password", "lukeoj@byu.edu"));
        CreateGameResult gameID = gameService.createGame(registerResult.authToken(), "game1");
        ListGamesResult listGamesResult = gameService.listGames(registerResult.authToken());
        userService.clearData();
        assertNotNull(listGamesResult);
    }

    @Test
    void registerUser() throws DataAccessException {
        RegisterResult registerResult = userService.registerUser(new RegisterRequest("username", "password", "lukeoj@byu.edu"));
        assertEquals("username", registerResult.username());
    }

    @Test
    void registerUserBad() throws DataAccessException {
        assertThrows(DataAccessException.class, () -> {
            RegisterResult registerResult = userService.registerUser(new RegisterRequest("username", null, "lukeoj@byu.edu"));
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
        assertNotNull(joinGameResult);
    }

    @Test
    void joinGameBad() throws DataAccessException {
        assertThrows(DataAccessException.class, () -> {
            gameService.joinGame("invalidAuthToken", 99, "Black");
        });
    }

}