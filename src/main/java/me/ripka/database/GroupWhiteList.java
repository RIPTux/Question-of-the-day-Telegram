package me.ripka.database;

import java.sql.*;

public class GroupWhiteList {
    private String url = "jdbc:sqlite:groupList.db";
    private Connection conn;
    private Statement stmt;
    private PreparedStatement pstmt;

    public void createGroupWhiteListDataBase() {
        try {
            conn = DriverManager.getConnection(url);

            String createTable = "CREATE TABLE IF NOT EXISTS groupWhiteList(" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "groupId TEXT NOT NULL," +
                    "active TEXT NOT NULL )";

            stmt = conn.createStatement();
            stmt.execute(createTable);
            System.out.println("Таблица вайтлиста груп создана/проверена!");

        } catch (SQLException e) {
            System.err.println("Ошибка создания таблицы вайт листа групп: " + e);
        }
    }

    public String addGroup(long groupId) {
        try {
            if (!groupCheck(groupId)) {
                String insertSQL = "INSERT INTO groupWhiteList (groupId, active) VALUES (?, ?)";

                pstmt = conn.prepareStatement(insertSQL);

                pstmt.setLong(1, groupId);
                pstmt.setString(2, "active");

                pstmt.executeUpdate();

                return "Группа добавлена!";
            } else {
                return "Группа active!";
            }
        } catch (SQLException e) {
            System.err.println("Ошибка добавления группы: " + e);
            return "Не удалось добавить группу!";
        }
    }

    public StringBuilder getGroupWhiteList() {
        try {
            String searchSQL = "SELECT * FROM groupWhiteList";

            pstmt = conn.prepareStatement(searchSQL);
            ResultSet rs = pstmt.executeQuery();

            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Группы: \n\n");

            boolean hasRows = false;
            while (rs.next()) {
                hasRows = true;
                stringBuilder.append("ID: " + rs.getString("id") + " GroupId: " + rs.getString("groupId") + " Status: " + rs.getString("active") + "\n");
            }

            if (!hasRows) {
                return new StringBuilder("Групп нету");
            }

            return stringBuilder;

        } catch (SQLException e) {
            System.err.println("Ошибка получения групп getGroupWhiteList: " + e);
            return new StringBuilder("Ошибка получения групп!");
        }
    }

    public boolean isGroupExists(long groupId) {
        try {
            String sql = "SELECT * FROM groupWhiteList WHERE groupId = ? AND active = 'active'";
            pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, groupId);
            ResultSet rs = pstmt.executeQuery();

            return rs.next();

        } catch (SQLException e) {
            System.err.println("Ошибка проверки группы isGroupExists: " + e);
            return false;
        }
    }



    public boolean groupCheck(Long groupId) {
        try {
            String sqlSearch = "SELECT * FROM groupWhiteList WHERE groupId = ? AND active = 'deactive'";
            pstmt = conn.prepareStatement(sqlSearch);
            pstmt.setLong(1, groupId);
            ResultSet rs = pstmt.executeQuery();

            if (!rs.next()) {
                return false;
            }

            String sqlSetActive = "UPDATE groupWhiteList SET active = 'active' WHERE groupId = ? AND active = 'deactive'";
            pstmt = conn.prepareStatement(sqlSetActive);
            pstmt.setLong(1, groupId);
            pstmt.executeUpdate();

            return true;

        } catch (SQLException e) {
            System.err.println("Ошибка при проверке состояния групп groupCheck: " + e);
            return false;
        }
    }


    public String deleteGroup(long id) {
        try {
            String sql = "UPDATE groupWhiteList SET active = ? WHERE id = ?";

            pstmt = conn.prepareStatement(sql);

            pstmt.setString(1, "deactive");
            pstmt.setLong(2, id);

            int edit = pstmt.executeUpdate();

            if (edit > 0) {
                return "Группа деактивирована!";
            } else {
                return "Группа не найдена!";
            }
        } catch (SQLException e) {
            System.out.println("Ошибка деактивации группы deleteGroup: " + e);
            return "Ошибка деактивации группы";

        }
    }
}
