package dataaccess;

import model.AuthData;
import model.GameData;
import model.UserData;

import java.util.ArrayList;

public interface DataAccess {
    void clear() throws DataAccessException;

    // create user, get user
    boolean createUser(UserData user) throws DataAccessException;

    UserData getUser(String username) throws DataAccessException;

    // create game, get game, list game, update game
    Integer createGame(GameData game) throws DataAccessException;

//    GameData getGame(String id);

//    GameData getGame(int id);

    GameData getGame(Integer id) throws DataAccessException;

    ArrayList<GameData> listGames() throws DataAccessException;

    GameData updateGame(GameData game) throws DataAccessException;

    // create auth, get auth, delete auth
    boolean createAuth(AuthData auth) throws DataAccessException;

    AuthData getAuth(String auth) throws DataAccessException;

    boolean deleteAuth(String auth) throws DataAccessException;

}
