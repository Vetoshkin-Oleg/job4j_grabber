package ru.job4j.grabber;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class PsqlStore implements Store {

    private final Connection cnn;

    public PsqlStore(Properties cfg) throws IOException, ClassNotFoundException {
        try (InputStream in = PsqlStore.class.getClassLoader()
                .getResourceAsStream("app.properties")) {
            cfg.load(in);
            Class.forName(cfg.getProperty("jdbc.driver"));
        }
        String url = cfg.getProperty("url");
        String login = cfg.getProperty("login");
        String password = cfg.getProperty("password");
        try {
            cnn = DriverManager.getConnection(url, login, password);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void save(Post post) {
        try (PreparedStatement statement =
                     cnn.prepareStatement("INSERT INTO post("
                                     + "name, text, link, created) VALUES (?, ?, ?, ?)"
                                     + "ON CONFLICT (link)"
                                     + "DO"
                                     + " UPDATE SET name = EXCLUDED.name,"
                                     + " text = EXCLUDED.text, created = EXCLUDED.created",
                             Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, post.getTitle());
            statement.setString(2, post.getDescription());
            statement.setString(3, post.getLink());
            statement.setTimestamp(4, Timestamp.valueOf(post.getCreated()));
            statement.execute();
            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    post.setId(generatedKeys.getInt(1));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<Post> getAll() {
        List<Post> posts = new ArrayList<>();
        try (PreparedStatement statement = cnn.prepareStatement("SELECT * FROM post")) {
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    posts.add(createPost(resultSet));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return posts;
    }

    @Override
    public Post findById(int id) {
        Post post = null;
        try (PreparedStatement statement =
                     cnn.prepareStatement("SELECT * FROM post"
                                     + " WHERE id = (?);")) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    post = createPost(resultSet);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return post;
    }

    private static Post createPost(ResultSet resultSet) throws SQLException {
        return new Post(
                resultSet.getInt("id"),
                resultSet.getString("name"),
                resultSet.getString("link"),
                resultSet.getString("text"),
                resultSet.getTimestamp("created").toLocalDateTime()
        );
    }

    @Override
    public void close() throws Exception {
        if (cnn != null) {
            cnn.close();
        }
    }

    public void createTable(String tableName) throws SQLException {
        try (Statement statement = cnn.createStatement()) {
            String sql = String.format(
                    "CREATE TABLE IF NOT EXISTS %s(%s, %s, %s, %s, %s);",
                    tableName,
                    "id SERIAL PRIMARY KEY",
                    "name text",
                    "text text",
                    "link text UNIQUE",
                    "created TIMESTAMP"
            );
            statement.execute(sql);
        }
    }
}