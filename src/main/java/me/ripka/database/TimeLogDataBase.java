package me.ripka.database;

import java.sql.*;
import java.util.HashMap;

public class TimeLogDataBase {
    private String url = "jdbc:sqlite:timeGroupLog.db";
    private Connection conn;
    private Statement stmt;
    private PreparedStatement pstmt;

    public void createTimeLogDataBase() {
        try {
            conn = DriverManager.getConnection(url);

            String createTable = "CREATE TABLE IF NOT EXISTS timelog(" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "groupId TEXT NOT NULL," +
                    "time INTEGER NOT NULL )";

            stmt = conn.createStatement();
            stmt.execute(createTable);
            System.out.println("Таблица последней отправки для груп создана/проверена!");

        } catch (SQLException e) {
            System.err.println("Ошибка создания таблицы времени отправки для групп: " + e);
        }
    }

    public String questionSend(long groupId, long time) {
        try {
            System.out.println("questionSend");
            if (!groupCheck(groupId, time)) {
                System.out.println("questionSend false");

                String insertSQL = "INSERT INTO timelog (groupId, time) VALUES (?, ?)";

                pstmt = conn.prepareStatement(insertSQL);

                pstmt.setLong(1, groupId);
                pstmt.setLong(2, time);

                pstmt.executeUpdate();

                return "Время обновлено!";
            } else {
                return "Время обновлено!";
            }
        } catch (SQLException e) {
            System.err.println("Ошибка добавления группы: " + e);
            return "Не удалось добавить группу!";
        }
    }

    public boolean groupCheck(Long groupId, Long time) {
        try {

            String sqlSearch = "SELECT * FROM timelog WHERE groupId = ?";
            pstmt = conn.prepareStatement(sqlSearch);
            pstmt.setLong(1, groupId);
            ResultSet rs = pstmt.executeQuery();

            if (!rs.next()) {
                return false;
            }

            String sql = "UPDATE timelog SET time = ? WHERE groupId = ?";

            pstmt = conn.prepareStatement(sql);

            pstmt.setLong(1, time);
            pstmt.setLong(2,groupId);

            pstmt.executeUpdate();
            return true;



        } catch (SQLException e) {
            System.err.println("Ошибка при проверке состояния групп groupCheck: " + e);
            return false;
        }
    }


    public HashMap<Long, Long> checkQuestionTime() {
        try {
            HashMap<Long, Long> result = new HashMap<>();
            String sql = "SELECT * FROM timelog";

            pstmt = conn.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                if (rs.getLong("time") + 24 * 60 * 60 * 1000 <= System.currentTimeMillis()) {
                    result.put(rs.getLong("groupId"), rs.getLong("time"));
                }
            }

            return result;

        } catch (Exception e) {
            System.err.println(e);
            return null;
        }
    }

    public String deleteGroup(long chatId) {
        try {
            String sql = "DELETE FROM timelog WHERE groupId = ?";

            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, String.valueOf(chatId));

            int deleted = pstmt.executeUpdate();

            if (deleted > 0) {
                return "Группа удалена!";
            } else {
                return "Группы нет в базе";
            }

        } catch (SQLException e) {
            System.out.println("Ошибка удаления группы deleteGroup: " + e);
            return "Ошибка удаления группы";
        }
    }

    public StringBuilder getGroupTime() {
        try {
            String searchSQL = "SELECT * FROM timelog";

            pstmt = conn.prepareStatement(searchSQL);
            ResultSet rs = pstmt.executeQuery();

            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Группы: \n\n");

            boolean hasRows = false;
            while (rs.next()) {
                hasRows = true;
                stringBuilder.append("ID: " + rs.getString("id") + " GroupId: " + rs.getString("groupId") + " Time: " + rs.getString("time") + "\n");
            }

            if (!hasRows) {
                return new StringBuilder("Групп нету");
            }

            return stringBuilder;

        } catch (SQLException e) {
            System.err.println("Ошибка получения групп getGroupTime: " + e);
            return new StringBuilder("Ошибка получения групп!");
        }
    }
}
