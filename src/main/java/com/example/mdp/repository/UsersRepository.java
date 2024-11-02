package com.example.mdp.repository;

import com.example.mdp.model.User;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;


@Repository
public class UsersRepository {
    private final JdbcTemplate jdbcTemplate;

    @Getter
    private ArrayList<User> users;

    @Autowired
    public UsersRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.users = (ArrayList<User>) this.setListUsers();
    }

    private List<User> setListUsers() {
        users = new ArrayList<>();
        try (Connection conn = jdbcTemplate.getDataSource().getConnection()) {
            String sql = "SELECT * FROM Users";
            Statement statement = conn.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);
            while (resultSet.next()) {
                users.add(User.builder()
                        .id(resultSet.getInt("id"))
                        .firstName(resultSet.getString("firstName"))
                        .secondName(resultSet.getString("secondName"))
                        .email(resultSet.getString("email"))
                        .login(resultSet.getString("userLogin"))
                        .password(resultSet.getString("userPassword"))
                        .gender(resultSet.getString("gender"))
                        .theme(resultSet.getString("theme"))
                        .creationDate(resultSet.getString("creationDate"))
                        .build());
            }
            return users;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    public User getUserByLogin(String login) {
        return users.stream()
                .filter(user -> user.getLogin().equals(login))
                .findFirst()
                .orElse(null);
    }

    public void save(User user) {
        String sql = "exec dbo.AddNewUser ?, ?, ?, ?, ?, ?, ?";
        String theme = user.isDarkTheme() ? "Dark" : "White";
        jdbcTemplate.update(sql, user.getFirstName(),
                user.getSecondName(), user.getEmail(), user.getLogin(),
                user.getPassword(), user.getGender(), theme);
        setListUsers();
    }

    public void updateThemeOnUser(User user) {
        String sql = "exec dbo.updateUser ?, ?";
        String theme = user.isDarkTheme() ? "Dark" : "White";
        jdbcTemplate.update(sql, user.getLogin(), theme);

        for (User currentUser : users) {
            if (currentUser.getLogin().equals(user.getLogin())) {
                currentUser.setDarkTheme(user.isDarkTheme());
            }
        }
    }

}
