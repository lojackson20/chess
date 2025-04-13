package ui.websocket;
import org.json.JSONObject;

public class NotificationHelperHandler implements NotificationHandler{
    @Override
    public void notify(String notification) {
        try {
            JSONObject json = new JSONObject(notification);
            String type = json.getString("type");
            String username = json.optString("username", "Unknown");

            switch (type) {
                case "JOIN" -> {
                    String color = json.optString("color", null);
                    if (color != null) {
                        System.out.println(username + " joined the game as " + color.toLowerCase() + ".");
                    } else {
                        System.out.println(username + " joined the game as an observer.");
                    }
                }
                case "MOVE" -> {
                    String move = json.optString("move", "an unknown move");
                    System.out.println(username + " made a move: " + move);
                }
                case "LEAVE" -> {
                    System.out.println(username + " has left the game.");
                }
                case "RESIGN" -> {
                    System.out.println(username + " has resigned.");
                }
                case "CHECK" -> {
                    System.out.println(username + " is in check.");
                }
                case "CHECKMATE" -> {
                    System.out.println(username + " is in checkmate!");
                }
                default -> {
                    System.out.println("Unknown notification type: " + type);
                }
            }
        } catch (Exception e) {
            System.out.println("Invalid notification message: " + notification);
        }
    }
}
