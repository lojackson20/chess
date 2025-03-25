//package server;
//
//public class ServerFacade {
//}

package server;

import com.google.gson.Gson;
import dataaccess.DataAccessException;
//import exception.ErrorResponse;
//import exception.ResponseException;
import model.GameData;
//import model.Move;

import java.io.*;
import java.net.*;

public class ServerFacade {

    private final String serverUrl;

    public ServerFacade(String url) {
        serverUrl = url;
    }

    public GameData createGame(GameData game) throws DataAccessException {
        var path = "/game";
        return this.makeRequest("POST", path, game, GameData.class);
    }

    public void endGame(int id) throws DataAccessException {
        var path = String.format("/game/%s", id);
        this.makeRequest("DELETE", path, null, null);
    }

    public GameData getGameState(int id) throws DataAccessException {
        var path = String.format("/game/%s/state", id);
        return this.makeRequest("GET", path, null, GameData.class);
    }

//    public void makeMove(Move move) throws DataAccessException {
//        var path = "/move";
//        this.makeRequest("POST", path, move, null);
//    }

    public GameData[] listGames() throws DataAccessException {
        var path = "/game";
        record ListGamesResponse(GameData[] games) {}
        var response = this.makeRequest("GET", path, null, ListGamesResponse.class);
        return response.games();
    }

    private <T> T makeRequest(String method, String path, Object request, Class<T> responseClass) throws DataAccessException {
        try {
            URL url = (new URI(serverUrl + path)).toURL();
            HttpURLConnection http = (HttpURLConnection) url.openConnection();
            http.setRequestMethod(method);
            http.setDoOutput(true);

            writeBody(request, http);
            http.connect();
            throwIfNotSuccessful(http);
            return readBody(http, responseClass);
        } catch (Exception ex) {
            throw new DataAccessException(ex.getMessage(), 500);
        }
    }

    private static void writeBody(Object request, HttpURLConnection http) throws IOException {
        if (request != null) {
            http.addRequestProperty("Content-Type", "application/json");
            String reqData = new Gson().toJson(request);
            try (OutputStream reqBody = http.getOutputStream()) {
                reqBody.write(reqData.getBytes());
            }
        }
    }

    private void throwIfNotSuccessful(HttpURLConnection http) throws IOException, DataAccessException {
        var status = http.getResponseCode();
        if (!isSuccessful(status)) {
            try (InputStream respErr = http.getErrorStream()) {
                if (respErr != null) {
                    throw DataAccessException.fromJson(respErr);
                }
            }
            throw new DataAccessException("other failure: " + status, status);
        }
    }

    private static <T> T readBody(HttpURLConnection http, Class<T> responseClass) throws IOException {
        T response = null;
        if (http.getContentLength() < 0) {
            try (InputStream respBody = http.getInputStream()) {
                InputStreamReader reader = new InputStreamReader(respBody);
                if (responseClass != null) {
                    response = new Gson().fromJson(reader, responseClass);
                }
            }
        }
        return response;
    }

    private boolean isSuccessful(int status) {
        return status / 100 == 2;
    }
}

