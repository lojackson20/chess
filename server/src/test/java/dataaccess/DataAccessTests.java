package dataaccess;

import chess.ChessGame;
import dataaccess.DataAccessException;
import dataaccess.MemoryDataAccess;
import model.GameData;
import model.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import service.*;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

public class DataAccessTests {

    private MySQLDataAccess mySQLDataAccess;

    @BeforeEach
    void setUp() throws DataAccessException {
        MySQLDataAccess dataAccess = new MySQLDataAccess();
        mySQLDataAccess = new MySQLDataAccess();
        mySQLDataAccess.clear();
    }

    @Test
    void clear() throws DataAccessException {
        UserData user = new UserData("luke", "jackson", "lj@byu");
        mySQLDataAccess.createUser(user);
        GameData game = new GameData(1, "joe", "biden", "something", new ChessGame());
        mySQLDataAccess.clear();
        var result = new ArrayList<GameData>();
        assertNull(mySQLDataAccess.getUser("luke"));
        assertEquals(result, mySQLDataAccess.listGames());
    }

    @Test
    void createUser() throws DataAccessException {
        UserData user = new UserData("luke", "jackson", "lj@byu");
        assertTrue(mySQLDataAccess.createUser(user));
        assertNotNull(mySQLDataAccess.getUser("luke"));
    }

    @Test
    void createUserBad() throws DataAccessException {
        UserData user = new UserData("luke", "jackson", "lj@byu");
        mySQLDataAccess.createUser(user);
        assertFalse(mySQLDataAccess.createUser(user));
    }

    @Test
    void getUser() throws DataAccessException {
        UserData user = new UserData("cosmo", "password123", "cosmo@byu");
        mySQLDataAccess.createUser(user);
        UserData retrieved = mySQLDataAccess.getUser("cosmo");
        assertEquals("cosmo", retrieved.username());
    }

    @Test
    void getUserBad() throws DataAccessException {
        assertNull(mySQLDataAccess.getUser("nonexistent"));
    }

    @Test
    void createGame() throws DataAccessException {
        GameData game = new GameData(1, "whitePlayer", "blackPlayer", "TestGame", new ChessGame());
        Integer gameId = mySQLDataAccess.createGame(game);
        assertNotNull(gameId);
        assertNotNull(mySQLDataAccess.getGame(gameId));
    }

    @Test
    void createGameBad() throws DataAccessException {
        assertThrows(DataAccessException.class, () -> mySQLDataAccess.createGame(null));
    }

    @Test
    void getGame() throws DataAccessException {

    }

    @Test
    void getGameBad() throws DataAccessException {

    }

    @Test
    void listGame() throws DataAccessException {

    }

    @Test
    void listGameBad() throws DataAccessException {

    }

    @Test
    void updateGame() throws DataAccessException {

    }

    @Test
    void updateGameBad() throws DataAccessException {

    }

    @Test
    void createAuth() throws DataAccessException {

    }

    @Test
    void createAuthBad() throws DataAccessException {

    }

    @Test
    void getAuth() throws DataAccessException {

    }

    @Test
    void getAuthBad() throws DataAccessException {

    }

    @Test
    void deleteAuth() throws DataAccessException {

    }

    @Test
    void deleteAuthBad() throws DataAccessException {

    }

}

//    @Test
//    void registerUser() throws DataAccessException {
//        RegisterResult registerResult = userService.registerUser(new RegisterRequest("username", "password", "lukeoj@byu.edu"));
//        assertEquals("username", registerResult.username());
//    }
//
//    @Test
//    void registerUserBad() throws DataAccessException {
//        assertThrows(DataAccessException.class, () -> {
//            RegisterResult registerResult = userService.registerUser(new RegisterRequest("username", null, "lukeoj@byu.edu"));
//        });
//    }
//
//    @Test
//    void loginUser() throws DataAccessException {
//        userService.registerUser(new RegisterRequest("username", "password", "lukeoj@byu.edu"));
//
//        LoginResult loginResult = userService.loginUser(new LoginRequest("username", "password"));
//        assertEquals("username", loginResult.username());
//    }
//
//    @Test
//    void loginUserBad() throws DataAccessException {
//        assertThrows(DataAccessException.class, () -> {
//            userService.loginUser(new LoginRequest("wrongUsername", "password"));
//        });
//    }
//
//    @Test
//    void logoutUser() throws DataAccessException {
//        RegisterResult registerResult = userService.registerUser(new RegisterRequest("username", "password", "lukeoj@byu.edu"));
//
//        LogoutResult logoutResult = userService.logoutUser(registerResult.authToken());
//        assertNotNull(logoutResult);
//    }
//
//    @Test
//    void logoutUserBad() throws DataAccessException {
//        assertThrows(DataAccessException.class, () -> {
//            userService.logoutUser("invalidAuthToken");
//        });
//    }
//
//    @Test
//    void createGame() throws DataAccessException {
//        RegisterResult registerResult = userService.registerUser(new RegisterRequest("username", "password", "lukeoj@byu.edu"));
//
//        CreateGameResult gameID = gameService.createGame(registerResult.authToken(), "game1");
//        assertNotNull(gameID);
//    }
//
//    @Test
//    void createGameBad() throws DataAccessException {
//        assertThrows(DataAccessException.class, () -> {
//            gameService.createGame("invalidAuthToken", "game1");
//        });
//    }
//
//    @Test
//    void listGames() throws DataAccessException {
//        RegisterResult registerResult = userService.registerUser(new RegisterRequest("username", "password", "lukeoj@byu.edu"));
//
//        gameService.createGame(registerResult.authToken(), "game1");
////        gameService.listGames(registerResult.authToken());
//        ListGamesResult listGamesResult = gameService.listGames(registerResult.authToken());
//        assertNotNull(listGamesResult);
//        assertFalse(listGamesResult.games().isEmpty());
//    }
//
//    @Test
//    void listGamesBad() throws DataAccessException {
//        assertThrows(DataAccessException.class, () -> {
//            gameService.listGames("invalidAuthToken");
//        });
//    }
//
//    @Test
//    void joinGame() throws DataAccessException {
//        RegisterResult registerResult = userService.registerUser(new RegisterRequest("username", "password", "lukeoj@byu.edu"));
//
//        CreateGameResult gameID = gameService.createGame(registerResult.authToken(), "game2");
//        JoinGameResult joinGameResult = gameService.joinGame(registerResult.authToken(), gameID.gameID(), "Black");
//        assertNotNull(joinGameResult);
//    }
//
//    @Test
//    void joinGameBad() throws DataAccessException {
//        assertThrows(DataAccessException.class, () -> {
//            gameService.joinGame("invalidAuthToken", 99, "Black");
//        });
//    }
//
//}