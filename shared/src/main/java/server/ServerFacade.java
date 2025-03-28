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
import requestAndResult.JoinGameRequest;
import requestAndResult.CreateGameRequest;
import requestAndResult.ListGamesResult;
import requestAndResult.RegisterRequest;
import requestAndResult.RegisterResult;
import requestAndResult.LoginRequest;
import requestAndResult.LoginResult;

import java.io.*;
import java.net.*;

public class ServerFacade {

    private final String serverUrl;

    public ServerFacade(String url) {
        serverUrl = url;
    }

    public RegisterResult registerUser(RegisterRequest request) throws DataAccessException {
        var path = "/user";
        return this.makeRequest("POST", path, request, RegisterResult.class);
    }

    public LoginResult loginUser(LoginRequest request) throws DataAccessException {
        var path = "/session";
        return this.makeRequest("POST", path, request, LoginResult.class);
    }

    public void logoutUser(String authToken) throws DataAccessException {
        var path = "/session";
        this.makeRequestWithAuth("DELETE", path, authToken, null, null);
    }

    public ListGamesResult listGames(String authToken) throws DataAccessException {
        var path = "/game";
        return this.makeRequestWithAuth("GET", path, authToken, null, ListGamesResult.class);
    }

    public GameData createGame(String authToken, CreateGameRequest request) throws DataAccessException {
        var path = "/game";
        return this.makeRequestWithAuth("POST", path, authToken, request, GameData.class);
    }

    public GameData joinGame(String authToken, JoinGameRequest request) throws DataAccessException {
        var path = "/game";
        this.makeRequestWithAuth("PUT", path, authToken, request, null);
        ListGamesResult listGameResult = this.makeRequestWithAuth("GET", path, authToken, null, ListGamesResult.class);
        return listGameResult.games().get(request.gameID() - 1);
    }

    public GameData observeGame(String authToken, Integer gameID) throws DataAccessException {
        var path = "/game";
        ListGamesResult listGameResult = this.makeRequestWithAuth("GET", path, authToken, null, ListGamesResult.class);
        return listGameResult.games().get(gameID - 1);
    }

    public void clearDatabase() throws DataAccessException {
        var path = "/db";
        this.makeRequest("DELETE", path, null, null);
    }

    private <T> T makeRequest(String method, String path, Object request, Class<T> responseClass) throws DataAccessException {
        return makeRequestWithAuth(method, path, null, request, responseClass);
    }

    private <T> T makeRequestWithAuth(String method, String path, String authToken, Object request, Class<T> responseClass) throws DataAccessException {
        try {
            URL url = (new URI(serverUrl + path)).toURL();
            HttpURLConnection http = (HttpURLConnection) url.openConnection();
            http.setRequestMethod(method);
            http.setDoOutput(true);

            if (authToken != null) {
                http.setRequestProperty("Authorization", authToken);
            }

            writeBody(request, http);
            http.connect();
            throwIfNotSuccessful(http);
            return readBody(http, responseClass);
        } catch (DataAccessException ex) {
            throw ex;
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
