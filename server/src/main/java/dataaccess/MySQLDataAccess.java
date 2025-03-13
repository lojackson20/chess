package dataaccess;
import chess.ChessGame;
import com.google.gson.Gson;
//import exception.ResponseException;
import model.AuthData;
import model.GameData;
import model.UserData;


import java.sql.*;
import java.util.ArrayList;

import static com.mysql.cj.conf.PropertyKey.logger;
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
        try (var conn = DatabaseManager.getConnection()) {
            conn.setAutoCommit(false); // Start transaction
            try (var ps = conn.prepareStatement(statement)) {
                ps.setString(1, user.username());
                ps.setString(2, user.password());
                ps.setString(3, user.email());
                int affectedRows = ps.executeUpdate();

                if (affectedRows == 0) {
                    conn.rollback();
                    return false;
                }

                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                throw new DataAccessException("Error: Error inserting user: " + e.getMessage(), 403);
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error: Database connection error: " + e.getMessage(), 403);
        }
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
            throw new DataAccessException("Error: Unable to read data: " + e.getMessage(), 403);
        }
        return null;
    }

@Override
public Integer createGame(GameData game) throws DataAccessException {
    var statement = "INSERT INTO games (whiteUsername, blackUsername, gameName, game) VALUES (?, ?, ?, ?)";
    var json = new Gson().toJson(game.game());
    try (var conn = DatabaseManager.getConnection()) {
        conn.setAutoCommit(false);
        try (var ps = conn.prepareStatement(statement, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, game.whiteUsername());
            ps.setString(2, game.blackUsername());
            ps.setString(3, game.gameName());
            ps.setString(4, json);
            int affectedRows = ps.executeUpdate();

            if (affectedRows == 0) {
                conn.rollback();
                throw new DataAccessException("Error: Failed to insert game", 403);
            }

            // Retrieve generated gameID
            try (var rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int generatedID = rs.getInt(1);
                    conn.commit();
                    return generatedID;
                } else {
                    conn.rollback();
                    throw new DataAccessException("Error: Failed to retrieve game ID", 403);
                }
            }
        } catch (SQLException e) {
            conn.rollback();
            throw new DataAccessException("Error: Error inserting game: " + e.getMessage(), 403);
        }
    } catch (SQLException e) {
        throw new DataAccessException("Error: Database connection error: " + e.getMessage(), 403);
    }
}


    @Override
    public GameData getGame(Integer id) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            var statement = "SELECT gameID, whiteUsername, blackUsername, gameName, game FROM games WHERE gameID=?";
            try (var ps = conn.prepareStatement(statement)) {
                ps.setInt(1, id);
                try (var rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return new GameData(
                                rs.getInt("gameID"),
                                rs.getString("whiteUsername"),
                                rs.getString("blackUsername"),
                                rs.getString("gameName"),
                                new Gson().fromJson(rs.getString("game"), ChessGame.class)
                        );
                    }
                }
            }
        } catch (Exception e) {
            throw new DataAccessException("Error: Unable to read data: " + e.getMessage(), 403);
        }
        return null;
    }

@Override
public ArrayList<GameData> listGames(GameData games) throws DataAccessException {
    var result = new ArrayList<GameData>();
    try (var conn = DatabaseManager.getConnection()) {
        var statement = "SELECT gameID, whiteUsername, blackUsername, gameName, game FROM games";
        try (var ps = conn.prepareStatement(statement)) {
            try (var rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(new GameData(
                            rs.getInt("gameID"),
                            rs.getString("whiteUsername"),
                            rs.getString("blackUsername"),
                            rs.getString("gameName"),
                            new Gson().fromJson(rs.getString("game"), ChessGame.class)
                    ));
                }
            }
        }
    } catch (Exception e) {
        throw new DataAccessException("Error: Unable to list games: " + e.getMessage(), 403);
    }
    return result;
}

//    @Override
//    public GameData updateGame(GameData game) throws DataAccessException {
//        // update this to
//        var statement = "UPDATE games SET json=? WHERE gameID=?";
//        var json = new Gson().toJson(game);
//        executeUpdate(statement, json, game.gameID());
//        return game;
//    }
@Override
public GameData updateGame(GameData game) throws DataAccessException {
    String statement = "UPDATE games SET whiteUsername=?, blackUsername=?, gameName=?, game=? WHERE gameID=?";
    String json = new Gson().toJson(game.game());

    try (Connection conn = DatabaseManager.getConnection();
         PreparedStatement ps = conn.prepareStatement(statement)) {
        ps.setString(1, game.whiteUsername());
        ps.setString(2, game.blackUsername());
        ps.setString(3, game.gameName());
        ps.setString(4, json);
        ps.setInt(5, game.gameID());

        int rowsAffected = ps.executeUpdate();
        if (rowsAffected == 0) {
            throw new DataAccessException("No game found with the given ID.", 403);
        }
    } catch (SQLException e) {
        throw new DataAccessException("Error updating game in database", 403);
    }

    return game;
}



    @Override
public boolean createAuth(AuthData auth) throws DataAccessException {
    var statement = "INSERT INTO auths (authToken, username) VALUES (?, ?)";
    try (var conn = DatabaseManager.getConnection()) {
        conn.setAutoCommit(false);
        try (var ps = conn.prepareStatement(statement)) {
            ps.setString(1, auth.authToken());
            ps.setString(2, auth.username());
            int affectedRows = ps.executeUpdate();
            if (affectedRows == 0) {
                conn.rollback();
                return false;
            }
            conn.commit();
            return true;
        } catch (SQLException e) {
            conn.rollback();
            throw new DataAccessException("Error: Error inserting auth token: " + e.getMessage(), 403);
        }
    } catch (SQLException e) {
        throw new DataAccessException("Error: Database connection error: " + e.getMessage(), 403);
    }
}

    @Override
    public AuthData getAuth(String auth) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            var statement = "SELECT authToken, username FROM auths WHERE authToken=?";
            try (var ps = conn.prepareStatement(statement)) {
                ps.setString(1, auth);
                try (var rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return new AuthData(rs.getString("username"), rs.getString("authToken"));
                    }
                }
            }
        } catch (Exception e) {
            throw new DataAccessException("Error: Unable to read data: " + e.getMessage(), 403);
        }
        return null;
    }


//@Override
//public boolean deleteAuth(String auth) throws DataAccessException {
//    var statement = "DELETE FROM auths WHERE authToken=?";
//    try (var conn = DatabaseManager.getConnection()) {
//        try (var ps = conn.prepareStatement(statement)) {
//            ps.setString(1, auth);
//            int affectedRows = ps.executeUpdate();
//            return affectedRows > 0;
//        }
//    } catch (SQLException e) {
//        throw new DataAccessException("Error: Error deleting auth token: " + e.getMessage(), 403);
//    }
//}
@Override
public boolean deleteAuth(String auth) throws DataAccessException {
    if (auth == null || auth.isEmpty()) {
        throw new DataAccessException("Error: authToken cannot be null or empty", 400);
    }

    String statement = "DELETE FROM auths WHERE authToken=?";
    try (Connection conn = DatabaseManager.getConnection();
         PreparedStatement ps = conn.prepareStatement(statement)) {

        ps.setString(1, auth);
        int affectedRows = ps.executeUpdate();
        return affectedRows > 0;

    } catch (SQLException e) {
        // Handle specific SQL constraint violation (e.g., foreign key violation)
        if (e.getSQLState() != null && e.getSQLState().startsWith("23")) {
            throw new DataAccessException("Error: Integrity constraint violation while deleting auth token", 409);
        }
        throw new DataAccessException("Error deleting auth token: " + e.getMessage(), 500);
    }
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
            throw new DataAccessException("Error: Unable to configure database: " + ex.getMessage(), 403);
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }
    }

}
