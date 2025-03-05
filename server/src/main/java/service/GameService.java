package service;

import chess.ChessGame;
import dataaccess.DataAccessException;
import dataaccess.DataAccess;
import model.AuthData;
import model.GameData;

public class GameService {
    private final DataAccess dataAccess;
    private Integer iDNumber = 1;

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

    public CreateGameResult createGame(String authToken, String gameName) throws DataAccessException {
        if (authToken == null || authToken.isEmpty()) {
            throw new DataAccessException("Error: bad request", 400);
        }

        AuthData authData = dataAccess.getAuth(authToken);
        if (authData == null) {
            throw new DataAccessException("Error: unauthorized", 401);
        }

        GameData newGame = new GameData(iDNumber++, null, null, gameName, new ChessGame());
        int gameID = dataAccess.createGame(newGame);
        return new CreateGameResult(gameID);
    }

    public JoinGameResult joinGame(String authToken, int gameID, String playerColor) throws DataAccessException {
        if (authToken == null || authToken.isEmpty() || playerColor == null || playerColor.isEmpty()) {
            throw new DataAccessException("{message: Error: bad request}", 400);
        }

        AuthData authData = dataAccess.getAuth(authToken);
        if (authData == null) {
            throw new DataAccessException("{message: Error: unauthorized}", 401);
        }

        GameData game = dataAccess.getGame(gameID);
        if (game == null) {
            throw new DataAccessException("{message: Error: game not found}", 404);
        }

        GameData updatedGame = getGameData(playerColor, game, authData);

        dataAccess.updateGame(updatedGame);
        return new JoinGameResult();
    }

    private static GameData getGameData(String playerColor, GameData game, AuthData authData) throws DataAccessException {
        String whitePlayer = game.whiteUsername();
        String blackPlayer = game.blackUsername();

        // Assign player to the requested color if available
        if (playerColor.equalsIgnoreCase("white")) {
            if (whitePlayer != null) {
                throw new DataAccessException("{message: Error: white player already taken}", 403);
            }
            whitePlayer = authData.username();
        } else if (playerColor.equalsIgnoreCase("black")) {
            if (blackPlayer != null) {
                throw new DataAccessException("{message: Error: black player already taken}", 403);
            }
            blackPlayer = authData.username();
        } else {
            throw new DataAccessException("{message: Error: invalid player color}", 400);
        }

        return new GameData(game.gameID(), whitePlayer, blackPlayer, game.gameName(), game.game());
    }

}
