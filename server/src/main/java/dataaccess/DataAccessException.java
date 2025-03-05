package dataaccess;

import com.google.gson.Gson;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * Indicates there was an error connecting to the database
 */
public class DataAccessException extends Exception{
    public DataAccessException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    final private int statusCode;

    public String toJson() {
        return new Gson().toJson(Map.of("message", this.getMessage()));
    }

    public static DataAccessException fromJson(InputStream stream) {
        var map = new Gson().fromJson(new InputStreamReader(stream), HashMap.class);
        var status = ((Double)map.get("status")).intValue();
        String message = map.get("message").toString();
        return new DataAccessException(message, status);
    }

    public int StatusCode() {
        return statusCode;
    }
}
