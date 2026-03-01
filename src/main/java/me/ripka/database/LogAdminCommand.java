package me.ripka.database;

import java.sql.*;

public class LogAdminCommand {
    private String url = "jdbc:sqlite:messageDataBase.db";
    private Connection conn;
    private Statement stmt;
    private PreparedStatement pstmt;

    public void createLogAdminDataBase() {
        try {
            conn = DriverManager.getConnection(url);

            String createTable = "CREATE TABLE IF NOT EXISTS adminLog(" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "type TEXT NOT NULL," +
                    "username TEXT," +
                    "userId TEXT NOT NULL," +
                    "access)";

            stmt = conn.createStatement();
            stmt.execute(createTable);
            System.out.println("База лога админ команд создана/проверена");

        } catch (SQLException e) {
            System.err.println("Ошибка создания базы логов админ команд: " + e);
        }
    }

    public void addLog(String type, String username, long userId, String access) {
        try {
            String insertSQL = "INSERT INTO adminLog (type, username, userId, access) VALUES (?,?,?,?)";

            pstmt = conn.prepareStatement(insertSQL);

            pstmt.setString(1, type);
            pstmt.setString(2, username);
            pstmt.setLong(3, userId);
            pstmt.setString(4, access);

            pstmt.executeUpdate();

            System.out.println("Добавлен новый админ лог");
        } catch (SQLException e) {
            System.err.println("Ошибка создания лога админ команды: " + e);
        }
    }
}
