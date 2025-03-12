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



}

