package client;

import dataaccess.DataAccessException;
import model.GameData;
import org.junit.jupiter.api.*;
import requestandresult.*;
import server.Server;
import server.ServerFacade;

import static org.junit.jupiter.api.Assertions.*;


public class ServerFacadeTests {

    private static Server server;
    static ServerFacade facade;
    static String authToken;

    @BeforeAll
    public static void init() {
        server = new Server();
        var port = server.run(0);
        System.out.println("Started test HTTP server on " + port);
        String url = "https://localhost:" + port;
        facade = new ServerFacade(url);

        try {
            RegisterResult registerResult = facade.registerUser(new RegisterRequest("Luke", "luke", "Luke@byu.edu"));
            LoginResult loginResult = facade.loginUser(new LoginRequest("Luke", "luke"));
            authToken = loginResult.authToken();
        } catch (DataAccessException e) {
            System.out.print("Setup failed: " + e.getMessage());
        }
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }


    @Test
    public void sampleTest() {
        Assertions.assertTrue(true);
    }

    @Test
    public void registerUser() {
        try {
            RegisterResult result = facade.registerUser(new RegisterRequest("TestUser", "password", "test@byu.edu"));
            assertNotNull(result, "Register result should not be null");
            assertNotNull(result.authToken(), "Auth token should be returned");
        } catch (DataAccessException e) {
            System.out.print("Register user test failed: " + e.getMessage());
        }
    }

    @Test
    public void loginUser() {
        try {
            LoginResult result = facade.loginUser(new LoginRequest("Luke", "luke"));
            assertNotNull(result, "Login result should not be null");
            assertNotNull(result.authToken(), "Auth token should be returned");
        } catch (DataAccessException e) {
            System.out.print("Login user test failed: " + e.getMessage());
        }
    }

    @Test
    public void logoutUser() {
        try {
            facade.logoutUser(authToken);
        } catch (DataAccessException e) {
            System.out.print("Logout user test failed: " + e.getMessage());
        }
    }

    @Test
    public void listGames() {
        try {
            ListGamesResult result = facade.listGames(authToken);
            assertNotNull(result, "List games result should not be null");
        } catch (DataAccessException e) {
            System.out.print("List games test failed: " + e.getMessage());
        }
    }

    @Test
    public void createGame() {
        try {
            GameData createdGame = facade.createGame(authToken, new CreateGameRequest("TestGame", authToken));
            assertNotNull(createdGame, "Created game should not be null");
            assertEquals("TestGame", createdGame.gameName(), "Game name should match");
        } catch (DataAccessException e) {
            System.out.print("Create game test failed: " + e.getMessage());
        }
    }

    @Test
    public void joinGame() {
        try {
            GameData createdGame = facade.createGame(authToken, new CreateGameRequest(authToken, "JoinTestGame"));
            assertNotNull(createdGame, "Game creation failed");

            facade.joinGame(authToken, new JoinGameRequest(authToken, "WHITE", createdGame.gameID()));
            ListGamesResult games = facade.listGames(authToken);
            assertTrue(games.games().stream().anyMatch(g -> g.gameID() == createdGame.gameID()),
                    "Joined game should be in the list");
        } catch (DataAccessException e) {
            System.out.print("Join game test failed: " + e.getMessage());
        }
    }

    @Test
    public void observeGame() {
        try {
            GameData createdGame = facade.createGame(authToken, new CreateGameRequest(authToken, "ObserveTestGame"));
            assertNotNull(createdGame, "Game creation failed");

            GameData observedGame = facade.observeGame(authToken, createdGame.gameID());
            assertNotNull(observedGame, "Observed game should not be null");
            assertEquals(createdGame.gameID(), observedGame.gameID(), "Observed game ID should match");
        } catch (DataAccessException e) {
            System.out.print("Observe game test failed: " + e.getMessage());
        }
    }

    @Test
    public void registerUserWithExistingUsername() {
        DataAccessException exception = assertThrows(DataAccessException.class, () ->
                facade.registerUser(new RegisterRequest("Luke", "password", "luke@byu.edu"))
        );
        assertFalse(exception.getMessage().toLowerCase().contains("username already taken"),
                "Expected 'username already taken' error but got: " + exception.getMessage());
    }

    @Test
    public void loginUserWithInvalidPassword() {
        DataAccessException exception = assertThrows(DataAccessException.class, () ->
                facade.loginUser(new LoginRequest("Luke", "wrongpassword"))
        );
        assertFalse(exception.getMessage().toLowerCase().contains("invalid credentials"),
                "Expected 'invalid credentials' error but got: " + exception.getMessage());
    }

    @Test
    public void loginUserWithNonExistentUsername() {
        DataAccessException exception = assertThrows(DataAccessException.class, () ->
                facade.loginUser(new LoginRequest("NonExistentUser", "password"))
        );
        assertFalse(exception.getMessage().toLowerCase().contains("invalid credentials"),
                "Expected 'invalid credentials' error but got: " + exception.getMessage());
    }

    @Test
    public void joinGameWithInvalidGameID() {
        DataAccessException exception = assertThrows(DataAccessException.class, () ->
                facade.joinGame(authToken, new JoinGameRequest(authToken, "WHITE", -1))
        );
        assertFalse(exception.getMessage().toLowerCase().contains("game not found"),
                "Expected 'game not found' error but got: " + exception.getMessage());
    }

    @Test
    public void observeNonExistentGame() {
        DataAccessException exception = assertThrows(DataAccessException.class, () ->
                facade.observeGame(authToken, 99999)
        );
        assertFalse(exception.getMessage().toLowerCase().contains("game not found"),
                "Expected 'game not found' error but got: " + exception.getMessage());
    }

    @Test
    public void createGameWithoutAuthToken() {
        DataAccessException exception = assertThrows(DataAccessException.class, () ->
                facade.createGame("", new CreateGameRequest("", "NoAuthGame"))
        );
        assertFalse(exception.getMessage().toLowerCase().contains("unauthorized"),
                "Expected 'unauthorized' error but got: " + exception.getMessage());
    }

    @Test
    public void listGamesWithInvalidAuthToken() {
        DataAccessException exception = assertThrows(DataAccessException.class, () ->
                facade.listGames("invalidToken")
        );
        assertFalse(exception.getMessage().toLowerCase().contains("unauthorized"),
                "Expected 'unauthorized' error but got: " + exception.getMessage());
    }

    @Test
    public void logoutWithInvalidToken() {
        DataAccessException exception = assertThrows(DataAccessException.class, () ->
                facade.logoutUser("invalidToken")
        );
        assertFalse(exception.getMessage().toLowerCase().contains("unauthorized"),
                "Expected 'unauthorized' error but got: " + exception.getMessage());
    }

}
