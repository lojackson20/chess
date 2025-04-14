package ui;
import com.google.gson.Gson;
import dataaccess.DataAccessException;
import model.GameData;
import ui.websocket.NotificationHandler;
import websocket.messages.LoadGameMessage;
import websocket.messages.Notification;
import websocket.messages.ServerMessage;

import static java.awt.Color.RED;
import static ui.EscapeSequences.*;

import java.util.ArrayList;
import java.util.Scanner;

public class Repl implements NotificationHandler {
    private final ChessClient client;

    public Repl(String serverUrl) {
        try {
            client = new ChessClient(serverUrl, this);
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public void run() {
        System.out.println("♟️ Welcome to the Chess Game. Sign in to start.");
        System.out.print(client.help());

        Scanner scanner = new Scanner(System.in);
        var result = "";
        while (!result.equals("Goodbye!")) {
            printPrompt();
            String line = scanner.nextLine();

            try {
                result = client.evalPreLogin(line);
                System.out.print(SET_TEXT_COLOR_GREEN + result);
            } catch (Throwable e) {
                var msg = e.toString();
                System.out.print(msg);
            }
        }
        System.out.println();
    }

    private void printPrompt() {
        System.out.print("\n" + RESET_TEXT_COLOR + ">>> " + SET_TEXT_COLOR_MAGENTA);
    }

    public void notify(String notification, ServerMessage.ServerMessageType type) {
        if (type == ServerMessage.ServerMessageType.NOTIFICATION) {
            Notification newNotification = new Gson().fromJson(notification, Notification.class);
            String message = newNotification.message();


        }

        if (type == ServerMessage.ServerMessageType.LOAD_GAME) {
            LoadGameMessage loadGameMessage = new Gson().fromJson(notification, LoadGameMessage.class);
            GameData gameData = loadGameMessage.gameData();

            boolean isWhitePerspective = client.getPlayerName().equals(gameData.whiteUsername());
            System.out.print("\n");
            client.drawBoard(isWhitePerspective, loadGameMessage.gameData(), new ArrayList<>());
            client.updateGameData(loadGameMessage.gameData());
            printPrompt();
        }


    }
}

