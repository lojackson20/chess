package dataaccess;
import chess.ChessGame;
import com.google.gson.Gson;
//import exception.ResponseException;
import model.AuthData;
import model.GameData;
import model.UserData;


import java.sql.*;
import java.util.ArrayList;

import static java.sql.Statement.RETURN_GENERATED_KEYS;
import static java.sql.Types.NULL;


public class MySQLDataAccess implements DataAccess {

    public MySQLDataAccess() throws DataAccessException {
        DatabaseManager.createDatabase();
        configureDatabase();
    }


    @Override
    public void clear() throws DataAccessException {
        executeUpdate("TRUNCATE users");
        executeUpdate("TRUNCATE games");
        executeUpdate("TRUNCATE auths");
    }

    @Override
    public boolean createUser(UserData user) throws DataAccessException {
        var statement = "INSERT INTO users (username, password, email) VALUES (?, ?, ?)";
//        var json = new Gson().toJson(user);
        int id = executeUpdate(statement, user.username(), user.password(), user.email());
        return id > 0;
    }

    @Override
    public UserData getUser(String username) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            var statement = "SELECT username, password, email FROM users WHERE username=?";
            try (var ps = conn.prepareStatement(statement)) {
                ps.setString(1, username);
                try (var rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return new UserData(rs.getString("username"), rs.getString("password"), rs.getString("email"));
                    }
                }
            }
        } catch (Exception e) {
            throw new DataAccessException("Unable to read data: " + e.getMessage(), 403);
        }
        return null;
    }

    @Override
    public Integer createGame(GameData game) throws DataAccessException {
        var statement = "INSERT INTO games (gameID, json) VALUES (?, ?)";
        var json = new Gson().toJson(game);
        return executeUpdate(statement, game.gameID(), json);
    }

    @Override
    public GameData getGame(Integer id) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            var statement = "SELECT gameID, whiteUsername, blackUSername, gameName, game FROM games WHERE gameID=?";
            try (var ps = conn.prepareStatement(statement)) {
                ps.setInt(1, id);
                try (var rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return new GameData(
                                rs.getInt("gameID"),
                                rs.getString("whiteUsername"),
                                rs.getString("blackUsername"),
                                rs.getString("gameName"),
                                new Gson().fromJson(rs.getString("game"), ChessGame.class)  // Deserialize game JSON
                        );
                    }
                }
            }
        } catch (Exception e) {
            throw new DataAccessException("Unable to read data: " + e.getMessage(), 403);
        }
        return null;
    }

    @Override
    public ArrayList<GameData> listGames(GameData games) throws DataAccessException {
        var result = new ArrayList<GameData>();
        try (var conn = DatabaseManager.getConnection()) {
            var statement = "SELECT json FROM games";
            try (var ps = conn.prepareStatement(statement)) {
                try (var rs = ps.executeQuery()) {
                    while (rs.next()) {
                        result.add(new Gson().fromJson(rs.getString("json"), GameData.class));
                    }
                }
            }
        } catch (Exception e) {
            throw new DataAccessException("Unable to read data: " + e.getMessage(), 403);
        }
        return result;
    }

    @Override
    public GameData updateGame(GameData game) throws DataAccessException {
        var statement = "UPDATE games SET json=? WHERE gameID=?";
        var json = new Gson().toJson(game);
        executeUpdate(statement, json, game.gameID());
        return game;
    }

    @Override
    public boolean createAuth(AuthData auth) throws DataAccessException {
        var statement = "INSERT INTO auths (authToken, username) VALUES (?, ?)";
        return executeUpdate(statement, auth.authToken(), auth.username()) > 0;
    }

    @Override
    public AuthData getAuth(String auth) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            var statement = "SELECT authToken, username FROM auths WHERE authToken=?";
            try (var ps = conn.prepareStatement(statement)) {
                ps.setString(1, auth);
                try (var rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return new AuthData(rs.getString("authToken"), rs.getString("username"));
                    }
                }
            }
        } catch (Exception e) {
            throw new DataAccessException("Unable to read data: " + e.getMessage(), 403);
        }
        return null;
    }

    @Override
    public boolean deleteAuth(String auth) throws DataAccessException {
        var statement = "DELETE FROM auths WHERE authToken=?";
        if (getAuth(auth) == null) {
            throw new DataAccessException("Unauthorized: Invalid auth token", 401);
        }
        return executeUpdate(statement, auth) > 0;
    }

    private int executeUpdate(String statement, Object... params) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            try (var ps = conn.prepareStatement(statement, RETURN_GENERATED_KEYS)) {
                for (var i = 0; i < params.length; i++) {
                    switch (params[i]) {
                        case String p -> ps.setString(i + 1, p);
                        case Integer p -> ps.setInt(i + 1, p);
                        case null -> ps.setNull(i + 1, NULL);
                        default -> {
                        }
                    }
                }
                ps.executeUpdate();

                var rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    return rs.getInt(1);
                }
                return 0;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error: Unable to update database: " + e.getMessage(), 403);
        }
    }

    private void configureDatabase() throws DataAccessException {
        DatabaseManager.createDatabase();
        String[] createStatements = {
                """
                CREATE TABLE IF NOT EXISTS users (
                  username VARCHAR(256) PRIMARY KEY,
                  password VARCHAR(256) NOT NULL,
                  email VARCHAR(256) NOT NULL
                )""",
                """
                CREATE TABLE IF NOT EXISTS games (
                  gameID INT AUTO_INCREMENT PRIMARY KEY,
                  whiteUsername VARCHAR(256),
                  blackUsername VARCHAR(256),
                  gameName VARCHAR(256),
                  game TEXT
                )""",
                """
                CREATE TABLE IF NOT EXISTS auths (
                  authToken VARCHAR(256) PRIMARY KEY,
                  username VARCHAR(256) NOT NULL
                )"""
        };
        try (var conn = DatabaseManager.getConnection()) {
            for (var statement : createStatements) {
                try (var preparedStatement = conn.prepareStatement(statement)) {
                    preparedStatement.executeUpdate();
                }
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Unable to configure database: " + ex.getMessage(), 500);
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }
    }

}
