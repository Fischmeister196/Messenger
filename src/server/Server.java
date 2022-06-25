package server;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Server implements Runnable {

    private List<ClientChat> clientChatList;
    private ServerSocket server = null;
    private Thread thread = null;
    private ArrayList<ServerThread> threadList = null;

    public Server(int portNumber) {

        try {
            System.out.println("SERVER=====================================");

            System.out.println("Server-Port: " + portNumber + ", Warte auf Clients...");
            server = new ServerSocket(portNumber);
            server.setReuseAddress(true);
            System.out.println("Server erstellt: " + server);

            System.out.println("===========================================\n");

            threadList = new ArrayList<ServerThread>();
            start();
            clientChatList = new ArrayList<>();

        } catch (IOException e) {
            System.out.println(e);
        }
    }

    /**
     * Startet einen neuen Thread
     */
    public void start() {
        if (thread == null)
            thread = new Thread(this);
        thread.start();
    }

    public void stop() {
        if (thread != null)
            thread.interrupt();
        thread = null;
    }

    /**
     * Solange ein Thread läuft wird run ausgeführt
     */
    @Override
    public void run() {
        while (thread != null) {
            try {
                System.out.println("CLIENT=====================================");
                System.out.println("Warte auf Clients...");
                addThread(server.accept());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Hinzufügen einen neuen Thread aka Client
     *
     * @param socket Verbindung zum neuen Client
     */
    public void addThread(Socket socket) {
        System.out.println("Verbunden mit Client: " + socket);
        System.out.println("===========================================\n");
        ServerThread serverThread = new ServerThread(this, socket);
        threadList.add(serverThread);
        startThread();
        serverThread.askName();

    }

    /**
     * Nach Hinzufügen des Threads durch addThread() wird der neune Thread gestartet
     */
    public void startThread() {
        try {
            int newThread = threadList.size() - 1;
            threadList.get(newThread).open();
            threadList.get(newThread).start();
        } catch (IOException ioe) {
            System.out.println("Thread konnte nicht gestartet werden: " + ioe);
        }
    }

    /**
     * Methode zum finden von Thread in threadList
     *
     * @param id
     * @return
     */
    public int findThread(int id) {

        int positionID = -1;

        for (ServerThread st : threadList) {
            if (st.getID() == id) {
                positionID = threadList.indexOf(st);
            }
        }

        return positionID;
    }

    /**
     * Löschen eines Thread
     *
     * @param id
     */
    public void removeThread(int id) {

        int removePosID = findThread(id);

        if (removePosID != -1) {
            ServerThread removeThread = threadList.get(removePosID);
            threadList.remove(removePosID);

            try {
                System.out.println("Removing client: " + id);
                removeThread.close();
            } catch (IOException ioe) {
                System.out.println("Could not close thread: " + ioe);
            }
        }
    }

    /**
     * Senden von input an alle Clients
     *
     * @param id
     * @param input
     */
    public synchronized void handle(int id, String input) {
        for (ServerThread client : threadList) {

            String[] messageArray = input.split(":");
            if (messageArray.length <= 2) {
                if (client.getID() != id) {
                    client.send(id + "(" + messageArray[0] + "): " + messageArray[1]);
                }

                if (messageArray[0].equals("askName")) {
                    if (existsName(messageArray[1])) {
                        sendChat(client, messageArray[1]);
                    } else {
                        clientChatList.add(new ClientChat(client.getID(), messageArray[1]));
                    }
                }

                for (ClientChat clientChat : clientChatList) {
                    clientChat.addMessage(id + "(" + messageArray[0] + "): " + messageArray[1]);
                }
                //addMessageToClientChat(messageArray[0], id + "(" + messageArray[0] + "): " + messageArray[1]);


            } else {
                try {
                    if (client.getID() == Integer.parseInt(messageArray[1])) {
                        client.send(id + "(" + messageArray[0] + ":private): " + messageArray[2]);

                        for (ClientChat clientChat : clientChatList) {
                            if(messageArray[0].equals(clientChat.getName())) {
                                clientChat.addMessage(id + "(" + messageArray[0] + "): " + messageArray[1]);
                            }
                        }

                        //addMessageToClientChat(messageArray[0], id + "(" + messageArray[0] + ":private): " + messageArray[2]);
                    }
                } catch (Exception e) {
                    client.send("Server: " + e.getMessage());
                }
            }

        }

        if (input.equalsIgnoreCase("ENDE!")) {
            threadList.get((findThread(id))).send("Tschüss vom Server!");
            removeThread(id);
        }
    }

    /**
     * Main
     * ######################################################
     *
     * @param args
     */
    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);
        System.out.println("Server Port:");
        int port = input.nextInt();

        try {
            System.out.println("IP Adresse: " + InetAddress.getLocalHost() + "\n");
        } catch (UnknownHostException e) {
            System.out.println(e);
        }

        Server server = new Server(port);
        input.close();
    }

    private void addMessageToClientChat(String name, String message) {
        for (ClientChat clientChat : clientChatList) {
            if (clientChat.getName().equals(name)) {
                clientChat.addMessage(message);
            }
        }
    }

    private boolean existsName(String name) {
        for (ClientChat clientChat : clientChatList) {
            if (clientChat.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    private String getChatFromClient(int id) {
        for (ClientChat clientChat : clientChatList) {
            if (clientChat.getId() == id) {
                return clientChat.getChat();
            }
        }

        return new String();
    }

    private synchronized void sendChat(ServerThread client, String name) {
        for (ClientChat clientChat : clientChatList) {
            if (clientChat.getName().equals(name)) {
                client.send(clientChat.getChat());
            }
        }

    }


}

