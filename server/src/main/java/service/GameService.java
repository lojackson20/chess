package service;

import chess.ChessGame;
import dataaccess.DataAccessException;
import dataaccess.DataAccess;
import model.AuthData;
import model.GameData;

import java.util.ArrayList;

public class GameService {
    private final DataAccess dataAccess;

    public GameService(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    public ListGamesResult listGames(String authToken) throws DataAccessException {
        if (authToken == null || authToken.isEmpty()) {
            throw new DataAccessException("{message: Error: bad request}", 400);
        }

        AuthData authData = dataAccess.getAuth(authToken);
        if (authData == null) {
            throw new DataAccessException("{message: Error: unauthorized}", 401);
        }
        return new ListGamesResult(dataAccess.listGames(null));
    }

    public Integer createGame(String authToken) throws DataAccessException {
        if (authToken == null || authToken.isEmpty()) {
            throw new DataAccessException("{message: Error: bad request}", 400);
        }

        AuthData authData = dataAccess.getAuth(authToken);
        if (authData == null) {
            throw new DataAccessException("{message: Error: unauthorized}", 401);
        }

        GameData newGame = new GameData(0, null, null, gameName, new ChessGame());
        int gameID = dataAccess.createGame(newGame);
        return new CreateGameResult(gameID);
    }

    public GameData getGame(int gameID) {
        return dataAccess.getGame(gameID);
    }

    public boolean updateGame(GameData game) {
        return dataAccess.updateGame(game) != null;
    }
}
