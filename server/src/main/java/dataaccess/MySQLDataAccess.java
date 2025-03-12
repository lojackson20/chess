package dataaccess;

import com.google.gson.Gson;
import exception.ResponseException;
import model.AuthData;
import model.GameData;
import model.UserData;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import static java.sql.Statement.RETURN_GENERATED_KEYS;
import static java.sql.Types.NULL;


public class MySqlDataAccess implements DataAccess {

    public MySqlDataAccess() throws ResponseException {
        configureDatabase();
    }


    @Override
    public void clear() throws ResponseException {
        executeUpdate("TRUNCATE users");
        executeUpdate("TRUNCATE games");
        executeUpdate("TRUNCATE auths");
    }

    @Override
    public boolean createUser(UserData user) throws ResponseException {
        var statement = "INSERT INTO users (username, password, json) VALUES (?, ?, ?)";
        var json = new Gson().toJson(user);
        int id = executeUpdate(statement, user.username(), user.password(), json);
        return id > 0;
    }

    @Override
    public UserData getUser(String username) throws ResponseException {
        try (var conn = DatabaseManager.getConnection()) {
            var statement = "SELECT json FROM users WHERE username=?";
            try (var ps = conn.prepareStatement(statement)) {
                ps.setString(1, username);
                try (var rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return new Gson().fromJson(rs.getString("json"), UserData.class);
                    }
                }
            }
        } catch (Exception e) {
            throw new ResponseException(500, "Unable to read data: " + e.getMessage());
        }
        return null;
    }

    @Override
    public Integer createGame(GameData game) throws ResponseException {
        var statement = "INSERT INTO games (gameID, json) VALUES (?, ?)";
        var json = new Gson().toJson(game);
        return executeUpdate(statement, game.gameID(), json);
    }

    @Override
    public GameData getGame(Integer id) throws ResponseException {
        try (var conn = DatabaseManager.getConnection()) {
            var statement = "SELECT json FROM games WHERE gameID=?";
            try (var ps = conn.prepareStatement(statement)) {
                ps.setInt(1, id);
                try (var rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return new Gson().fromJson(rs.getString("json"), GameData.class);
                    }
                }
            }
        } catch (Exception e) {
            throw new ResponseException(500, "Unable to read data: " + e.getMessage());
        }
        return null;
    }

    @Override
    public ArrayList<GameData> listGames(GameData games) throws ResponseException {
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
            throw new ResponseException(500, "Unable to read data: " + e.getMessage());
        }
        return result;
    }

    @Override
    public GameData updateGame(GameData game) throws ResponseException {
        var statement = "UPDATE games SET json=? WHERE gameID=?";
        var json = new Gson().toJson(game);
        executeUpdate(statement, json, game.gameID());
        return game;
    }

    @Override
    public boolean createAuth(AuthData auth) throws ResponseException {
        var statement = "INSERT INTO auths (authToken, json) VALUES (?, ?)";
        var json = new Gson().toJson(auth);
        return executeUpdate(statement, auth.authToken(), json) > 0;
    }

    @Override
    public AuthData getAuth(String auth) throws ResponseException {
        try (var conn = DatabaseManager.getConnection()) {
            var statement = "SELECT json FROM auths WHERE authToken=?";
            try (var ps = conn.prepareStatement(statement)) {
                ps.setString(1, auth);
                try (var rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return new Gson().fromJson(rs.getString("json"), AuthData.class);
                    }
                }
            }
        } catch (Exception e) {
            throw new ResponseException(500, "Unable to read data: " + e.getMessage());
        }
        return null;
    }

    @Override
    public boolean deleteAuth(String auth) throws ResponseException {
        var statement = "DELETE FROM auths WHERE authToken=?";
        return executeUpdate(statement, auth) > 0;
    }

    private int executeUpdate(String statement, Object... params) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            try (var ps = conn.prepareStatement(statement, RETURN_GENERATED_KEYS)) {
                for (var i = 0; i < params.length; i++) {
                    if (params[i] instanceof String p) ps.setString(i + 1, p);
                    else if (params[i] instanceof Integer p) ps.setInt(i + 1, p);
                    else if (params[i] == null) ps.setNull(i + 1, NULL);
                }
                ps.executeUpdate();

                var rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    return rs.getInt(1);
                }
                return 0;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Unable to update database: " + e.getMessage(), 500);
        }
    }

    private void configureDatabase() throws DataAccessException {
        DatabaseManager.createDatabase();
        // Add SQL statements to create users, games, and auths tables if needed.
    }

}

