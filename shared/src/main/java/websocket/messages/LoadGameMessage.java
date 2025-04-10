package websocket.messages;

import model.GameData;

public class LoadGameMessage extends ServerMessage{
    private GameData game;

    public LoadGameMessage(ServerMessageType type, GameData gameData) {
        super(type);
        GameData game = gameData;
    }
}
