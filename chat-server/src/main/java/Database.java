import java.sql.*;
public class Database {
    private static final String DB_URL = "jdbc:sqlite:mydatabase.db";

    public static void initialize (){
        try(Connection connection = DriverManager.getConnection(DB_URL)) {
            System.out.println("подключение к базе данных успешно!");
            Statement stmt = connection.createStatement();

            String sql = """
                    CREATE TABLE IF NOT EXISTS users (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    login TEXT UNIQUE NOT NULL,
                    password TEXT NOT NULL
                    );
                    """;
            stmt.execute(sql);
            System.out.println("Таблица users создана");

            String insert = """
                INSERT OR IGNORE INTO users (login, password) VALUES
                ('admin', '1234'),
                ('alex', 'qwerty'),
                ('maria', 'pass');
                """;
            PreparedStatement ps = connection.prepareStatement(insert);
            ps.executeUpdate();

            ResultSet rs = connection.createStatement().executeQuery("SELECT * FROM users");
            while(rs.next()){
                System.out.println(rs.getInt("id")+ " | " +
                        rs.getString("login") + " | " +
                        rs.getString("password"));
            }
        } catch (SQLException e) {
            System.out.println("ошибка подключения: " + e.getMessage());
        } ;
    }

    public static boolean authenticate(String login, String password) {
        String sql = "SELECT * FROM users WHERE login = ? AND password = ?";
        try (Connection connection = DriverManager.getConnection(DB_URL);
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, login);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            System.out.println("Ошибка при аутентификации: " + e.getMessage());
            return false;
        }
    }

}

