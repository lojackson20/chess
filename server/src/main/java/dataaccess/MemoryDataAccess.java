package dataaccess;

import model.AuthData;
import model.GameData;
import model.UserData;

import java.util.ArrayList;
import java.util.HashMap;

public class MemoryDataAccess implements DataAccess {

    final private HashMap<String, UserData> users = new HashMap<>();

    @Override
    public void clear() {

    }

    @Override
    public boolean createUser(UserData user) throws DataAccessException {
        if (users.get(user.username()) == null) {
            users.put(user.username(), user);
            return true;
        }
        throw new DataAccessException("{message: Error: already taken}", 403);
//        return false;
    }

    @Override
    public UserData getUser(String username) {
        return null;
    }

    @Override
    public boolean createGame(GameData game) {
        return false;
    }

    @Override
    public GameData getGame(String id) {
        return null;
    }

    @Override
    public ArrayList<GameData> listGames(GameData games) {
        return null;
    }

    @Override
    public GameData updateGame(GameData game) {
        return null;
    }

    @Override
    public boolean createAuth(AuthData auth) {
        return false;
    }

    @Override
    public AuthData getAuth(String auth) {
        return null;
    }

    @Override
    public void deleteAuth(String auth) {

    }
}
