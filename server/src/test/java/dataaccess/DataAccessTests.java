package dataaccess;

import chess.ChessGame;
import dataaccess.DataAccessException;
import dataaccess.MemoryDataAccess;
import model.AuthData;
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
        UserData user = new UserData(null, "password", "email@example.com");
        assertThrows(DataAccessException.class, () -> mySQLDataAccess.createUser(user));
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
        GameData game = new GameData(1, "", "", "", null);
        assertNotNull(game);
    }

    @Test
    void getGame() throws DataAccessException {
        GameData game = new GameData(1, "white", "black", "Game1", new ChessGame());
        Integer gameId = mySQLDataAccess.createGame(game);
        GameData retrieved = mySQLDataAccess.getGame(gameId);
        assertNotNull(retrieved);
        assertEquals("Game1", retrieved.gameName());

    }

    @Test
    void getGameBad() throws DataAccessException {
        assertNull(mySQLDataAccess.getGame(9999));
    }

    @Test
    void listGame() throws DataAccessException {
        mySQLDataAccess.createGame(new GameData(1, "white1", "black1", "Game1", new ChessGame()));
        mySQLDataAccess.createGame(new GameData(2, "white2", "black2", "Game2", new ChessGame()));
        assertEquals(2, mySQLDataAccess.listGames().size());
    }

    @Test
    void listGameBad() throws DataAccessException {
        assertEquals(0, mySQLDataAccess.listGames().size());
    }

    @Test
    void updateGame() throws DataAccessException {
        GameData game = new GameData(1, "white", "black", "Game1", new ChessGame());
        Integer gameId = mySQLDataAccess.createGame(game);
        GameData updatedGame = new GameData(gameId, "white", "black", "UpdatedGame", new ChessGame());
        mySQLDataAccess.updateGame(updatedGame);
        assertEquals("UpdatedGame", mySQLDataAccess.getGame(gameId).gameName());
    }

    @Test
    void updateGameBad() throws DataAccessException {
        GameData invalidGame = new GameData(9999, "white", "black", "Nonexistent", new ChessGame());
        assertThrows(DataAccessException.class, () -> mySQLDataAccess.updateGame(invalidGame));
    }

    @Test
    void createAuth() throws DataAccessException {
        AuthData auth = new AuthData("luke", "token123");
        assertTrue(mySQLDataAccess.createAuth(auth));
        assertNotNull(mySQLDataAccess.getAuth("token123"));
    }

    @Test
    void createAuthBad() throws DataAccessException {
        AuthData auth = new AuthData(null, "username");
        assertThrows(DataAccessException.class, () -> mySQLDataAccess.createAuth(auth));
    }

    @Test
    void getAuth() throws DataAccessException {
        AuthData auth = new AuthData("user1", "authToken");
        mySQLDataAccess.createAuth(auth);
        AuthData retrieved = mySQLDataAccess.getAuth("authToken");
        assertEquals("user1", retrieved.username());
    }

    @Test
    void getAuthBad() throws DataAccessException {
        assertNull(mySQLDataAccess.getAuth("nonexistentToken"));
    }

    @Test
    void deleteAuth() throws DataAccessException {
        AuthData auth = new AuthData("user1", "tokenToDelete");
        mySQLDataAccess.createAuth(auth);
        assertTrue(mySQLDataAccess.deleteAuth("tokenToDelete"));
        assertNull(mySQLDataAccess.getAuth("tokenToDelete"));
    }

    @Test
    void deleteAuthBad() throws DataAccessException {
        assertFalse(mySQLDataAccess.deleteAuth("invalidToken"));
    }

}
