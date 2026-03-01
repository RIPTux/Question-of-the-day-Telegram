package me.ripka.database;

import java.sql.*;
import java.util.HashMap;

public class QuestionDataBase {
    private String url = "jdbc:sqlite:questionDataBase.db";
    private Connection conn;
    private Statement stmt;
    private PreparedStatement pstmt;

    public void createQuestionDataBase() {
        try {
            conn = DriverManager.getConnection(url);

            String createTable = "CREATE TABLE IF NOT EXISTS questionDB(" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "question TEXT NOT NULL )";

            stmt = conn.createStatement();
            stmt.execute(createTable);
            System.out.println("Таблица вопросов гравити фолз создана/проверена!");

        } catch (SQLException e) {
            System.err.println("Ошибка создания таблицы вопросов гравити фолз: " + e);
        }
    }

    public String addQuestion(String question) {
        try {
            String insertSQL = "INSERT INTO questionDB (question) VALUES (?)";
            pstmt = conn.prepareStatement(insertSQL);
            pstmt.setString(1, question);
            pstmt.executeUpdate();
            return "Вопрос добавлен!";
        } catch (SQLException e) {
            System.err.println("Ошибка добавления вопроса: " + e);
            return "Не удалось добавить вопрос!";
        }
    }

    public String deleteQuestion(long chatId) {
        try {
            String sql = "DELETE FROM questionDB WHERE id = ?";

            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, chatId);

            int deleted = pstmt.executeUpdate();

            if (deleted > 0) {
                return "вопрос удален удалена!";
            } else {
                return "вопроса нет в базе";
            }

        } catch (SQLException e) {
            System.err.println("Ошибка удаления вопроса deleteQuestion: " + e);
            return "Ошибка удаления вопроса";
        }
    }


    public String getRandomQuestion() {
        try {
            String sql = "SELECT * FROM questionDB ORDER BY RANDOM() LIMIT 1";
            pstmt = conn.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();

            if (!rs.next()) {
                return null;
            }

            return rs.getString("question");
        } catch (SQLException e) {
            System.err.println("Ошибка получения рандомного вопроса: " + e);
            return null;
        }
    }



    public StringBuilder getQuestion() {
        try {
            String searchSQL = "SELECT * FROM questionDB";

            pstmt = conn.prepareStatement(searchSQL);
            ResultSet rs = pstmt.executeQuery();

            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Вопросы: \n\n");

            boolean hasRows = false;
            while (rs.next()) {
                hasRows = true;
                stringBuilder.append("ID: " + rs.getString("id") + " Question: " + rs.getString("question"));
                stringBuilder.append("\n");
            }

            if (!hasRows) {
                return new StringBuilder("Вопросов нету");
            }

            return stringBuilder;

        } catch (SQLException e) {
            System.err.println("Ошибка получения вопросов getQuestion: " + e);
            return new StringBuilder("Ошибка получения вопросов!");
        }
    }
}
