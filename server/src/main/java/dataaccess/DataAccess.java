package dataaccess;

import model.AuthData;
import model.GameData;
import model.UserData;

import java.util.ArrayList;

public interface DataAccess {
    void clear();

    // create user, get user
    boolean createUser(UserData user) throws DataAccessException;

    UserData getUser(String username);

    // create game, get game, list game, update game
    Integer createGame(GameData game) throws DataAccessException;

//    GameData getGame(String id);

//    GameData getGame(int id);

    GameData getGame(Integer id);

    ArrayList<GameData> listGames(GameData games);

    GameData updateGame(GameData game);

    // create auth, get auth, delete auth
    boolean createAuth(AuthData auth);

    AuthData getAuth(String auth);

    boolean deleteAuth(String auth);

}
