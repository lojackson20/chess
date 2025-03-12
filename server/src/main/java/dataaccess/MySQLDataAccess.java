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

}

