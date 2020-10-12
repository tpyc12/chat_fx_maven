package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Vector;

public class Server {
    private List<ClientHandler> clients;
    private AuthService authService;

    private int PORT = 8189;
    ServerSocket server = null;
    Socket socket = null;

    public Server() {
        clients = new Vector<>();
        authService = new SimpleAuthService();

        try {
            server = new ServerSocket(PORT);
            System.out.println("Сервер запущен");

            while (true) {
                socket = server.accept();
                System.out.println("Клиент подключился");

                new ClientHandler(this, socket);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                server.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public AuthService getAuthService() {
        return authService;
    }

    public void broadcastMsg(ClientHandler sender, String msg){
        SimpleDateFormat formater = new SimpleDateFormat("HH:mm:ss");

        String message = String.format(" %s %s : %s", formater.format(new Date()), sender.getNickname(), msg);
        for (ClientHandler client: clients){
            client.sendMsg(message);
        }
    }

    public void privateMsg(ClientHandler sender, String receiver, String msg){
        String message = String.format("[%s] private [%s] : %s",  sender.getNickname(), receiver, msg);
        for (ClientHandler client: clients){
            if (client.getNickname().equals(receiver)) {
                client.sendMsg(message);
                if (!client.equals(sender)) {
                    sender.sendMsg(message);
                }
                return;
            }
        }

        sender.sendMsg("not found user: " + receiver);
    }


    public void subscribe (ClientHandler clientHandler){
        clients.add(clientHandler);
        broadcastClientList();
    }

    public void unsubscribe (ClientHandler clientHandler){
        clients.remove(clientHandler);
        broadcastClientList();
    }

    public boolean isLoginAuthenticated(String login){
        for(ClientHandler c : clients){
            if (c.getLogin().equals(login)) {
                return true;
            }
        }
        return false;
    }

    public void broadcastClientList(){
        StringBuilder sb = new StringBuilder("/clientlist ");
        for (ClientHandler client : clients){
            sb.append(client.getNickname()).append(" ");
        }

        String msg = sb.toString();
        for (ClientHandler client : clients){
            client.sendMsg(msg);
        }
    }
}
