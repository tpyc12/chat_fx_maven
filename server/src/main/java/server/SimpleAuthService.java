package server;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SimpleAuthService implements AuthService {

    private static Connection connection;
    private static Statement stmt;
    private static PreparedStatement psInsert;

    private class UserData {
        String login;
        String password;
        String nickname;

        public UserData(String login, String password, String nickname) {
            this.login = login;
            this.password = password;
            this.nickname = nickname;
        }
    }

    List<UserData> users;

    public SimpleAuthService() {
        users = new ArrayList<>();
        users.add(new UserData("asd", "asd", "asd"));
        users.add(new UserData("qwe", "qwe", "qwe"));
        users.add(new UserData("zxc", "zxc", "zxc"));
    }

    @Override
    public String getNicknameByLoginAndPassword(String login, String password) {
        for (UserData user: users){
            if(user.login.equals(login) && user.password.equals(password)){
                return user.nickname;
            }
        }
        return null;
    }

    @Override
    public boolean registration(String login, String password, String nickname) {
        for (UserData user: users){
            if(user.login.equals(login) || user.nickname.equals(nickname)){
                return false;
            }
        }
        users.add(new UserData(login, password, nickname));
        try {
            connect();
            prepareAllStatement();
            batchFillTable(login, password, nickname);
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            disconnect();
        }
        return true;
    }

    public static void connect() throws ClassNotFoundException, SQLException {
        Class.forName("org.sqlite.JDBC");
        connection = DriverManager.getConnection("jdbc:sqlite:clients.db");
        stmt = connection.createStatement();
    }

    private static void prepareAllStatement() throws SQLException {
        psInsert = connection.prepareStatement("INSERT INTO clients (login, pass, nickname) VALUES (?, ?, ?);");
    }

    private static void batchFillTable(String login, String password, String nickname) throws SQLException {
        connection.setAutoCommit(false);
        psInsert.setString(1,login);
        psInsert.setString(2,password);
        psInsert.setString(3, nickname);
        psInsert.addBatch();
        psInsert.executeBatch();
        connection.setAutoCommit(true);
    }

    private static void disconnect(){
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        try {
            psInsert.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        try {
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
