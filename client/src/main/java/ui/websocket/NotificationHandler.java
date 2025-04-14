package ui.websocket;

import websocket.messages.Notification;
import websocket.messages.ServerMessage;

public interface NotificationHandler {
    void notify(ServerMessage.ServerMessageType type, String notification);
}