package ui;
import com.google.gson.Gson;
import dataaccess.DataAccessException;
import model.GameData;
import ui.websocket.NotificationHandler;
import websocket.messages.ErrorMessage;
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

    public void notify(String notification) {
        Gson gson = new Gson();
        ServerMessage baseMessage = gson.fromJson(notification, ServerMessage.class);

        if (baseMessage.getServerMessageType() == ServerMessage.ServerMessageType.NOTIFICATION) {
            Notification newNotification = gson.fromJson(notification, Notification.class);
            System.out.print("\n" + SET_TEXT_COLOR_YELLOW + newNotification.message() + RESET_TEXT_COLOR);
            printPrompt();
        } else if (baseMessage.getServerMessageType() == ServerMessage.ServerMessageType.LOAD_GAME) {
            LoadGameMessage loadGameMessage = gson.fromJson(notification, LoadGameMessage.class);
            GameData gameData = loadGameMessage.gameData();

            boolean isNotWhitePerspective = client.getPlayerName().equals(gameData.blackUsername());
            System.out.print("\n");
            client.drawBoard(!isNotWhitePerspective, gameData, new ArrayList<>());
            client.updateGameData(gameData);
            printPrompt();
        } else if (baseMessage.getServerMessageType() == ServerMessage.ServerMessageType.ERROR) {
            ErrorMessage errorMessage = gson.fromJson(notification, ErrorMessage.class);
            System.out.print("\n" + SET_TEXT_COLOR_YELLOW + errorMessage.message() + RESET_TEXT_COLOR);
            printPrompt();
        } else {
            System.out.print("\n" + SET_TEXT_COLOR_RED + "Unknown message type received from server." + RESET_TEXT_COLOR);
            printPrompt();
        }

    }
}


// show that observer has joined********
// show actual position that it moved from in the notification
// cant move not your piece********
// in chessclient handle promotion piece*******
// change error message to invalid move********


