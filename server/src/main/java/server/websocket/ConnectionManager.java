//package server.websocket;
//
//import org.eclipse.jetty.websocket.api.Session;
//import websocket.messages.ServerMessage;
//
//import java.io.IOException;
//import java.util.*;
//import java.util.concurrent.ConcurrentHashMap;
//
//public class ConnectionManager {
//
//    public final ConcurrentHashMap<String, Connection> connections = new ConcurrentHashMap<>();
//    private final ConcurrentHashMap<Integer, Set<String>> gameConnections = new ConcurrentHashMap<>();
//
//    public void add(String authToken, Session session, int gameID) {
//        var connection = new Connection(authToken, session);
//        connections.put(authToken, connection);
//        gameConnections.computeIfAbsent(gameID, k -> ConcurrentHashMap.newKeySet()).add(authToken);
//    }
//
//    public void remove(String authToken) {
//        connections.remove(authToken);
//        for (Set<String> tokens : gameConnections.values()) {
//            tokens.remove(authToken);
//        }
//    }
//
//    public void broadcastExcept(String excludeToken, int gameID, ServerMessage message) throws IOException {
//        var removeList = new ArrayList<String>();
//        Set<String> tokens = gameConnections.getOrDefault(gameID, Set.of());
//
//        for (String token : tokens) {
//            if (!token.equals(excludeToken)) {
//                Connection connection = connections.get(token);
//                if (connection != null && connection.session.isOpen()) {
//                    connection.send(message.toString());
//                } else {
//                    removeList.add(token);
//                }
//            }
//        }
//
//        for (String token : removeList) {
//            remove(token);
//        }
//    }
//
//    public void broadcastToGame(int gameID, ServerMessage message) throws IOException {
//        var removeList = new ArrayList<String>();
//        Set<String> tokens = gameConnections.getOrDefault(gameID, Set.of());
//
//        for (String token : tokens) {
//            Connection connection = connections.get(token);
//            if (connection != null && connection.session.isOpen()) {
//                connection.send(message.toString());
//            } else {
//                removeList.add(token);
//            }
//        }
//
//        for (String token : removeList) {
//            remove(token);
//        }
//    }
//
//}
package server.websocket;

import org.eclipse.jetty.websocket.api.Session;
import websocket.messages.ServerMessage;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionManager {

    public final ConcurrentHashMap<String, Connection> connections = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Integer, Set<String>> gameConnections = new ConcurrentHashMap<>();

    public void add(String authToken, Session session, int gameID) {
        var connection = new Connection(authToken, session);
        connections.put(authToken, connection);
        gameConnections.computeIfAbsent(gameID, k -> ConcurrentHashMap.newKeySet()).add(authToken);
    }

    public void remove(String authToken) {
        connections.remove(authToken);
        for (Set<String> tokens : gameConnections.values()) {
            tokens.remove(authToken);
        }
    }

    public void broadcastExcept(String excludeToken, int gameID, ServerMessage message) throws IOException {
        broadcast(gameID, message, excludeToken);
    }

    public void broadcastToGame(int gameID, ServerMessage message) throws IOException {
        broadcast(gameID, message, null);
    }

    private void broadcast(int gameID, ServerMessage message, String excludeToken) throws IOException {
        var removeList = new ArrayList<String>();
        Set<String> tokens = gameConnections.getOrDefault(gameID, Set.of());

        for (String token : tokens) {
            if (token.equals(excludeToken)) {
                continue;
            }

            Connection connection = connections.get(token);
            if (connection != null && connection.session.isOpen()) {
                connection.send(message.toString());
            } else {
                removeList.add(token);
            }
        }

        for (String token : removeList) {
            remove(token);
        }
    }
}