package dataaccess;

import model.AuthData;
import model.GameData;
import model.UserData;

import java.util.ArrayList;
import java.util.HashMap;

public class MemoryDataAccess implements DataAccess {

    final private HashMap<String, UserData> users = new HashMap<>();
    private final HashMap<Integer, GameData> games = new HashMap<>();
    private final HashMap<String, AuthData> auths = new HashMap<>();

    @Override
    public void clear() {
        users.clear();
        games.clear();
        auths.clear();
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
        return users.get(username);
    }

    @Override
    public boolean createGame(GameData game) {
        if (!games.containsKey(game.gameID())) {
            games.put(game.gameID(), game);
            return true;
        }
        return false;
    }

    @Override
    public GameData getGame(int id) {
        return games.get(id);
    }

    @Override
    public ArrayList<GameData> listGames(GameData games) {
        return new ArrayList<>(this.games.values());
    }

    @Override
    public GameData updateGame(GameData game) {
        if (games.containsKey(game.gameID())) {
            games.put(game.gameID(), game);
            return game;
        }
        return null;
    }

    @Override
    public boolean createAuth(AuthData auth) {
        if (!auths.containsKey(auth.token())) {
            auths.put(auth.token(), auth);
            return true;
        }
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
