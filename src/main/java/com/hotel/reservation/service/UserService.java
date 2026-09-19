package com.hotel.reservation.service;

import com.hotel.reservation.model.User;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {

    private final JdbcTemplate jdbcTemplate;

    public UserService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional
    public boolean registerUser(User newUser) {
        Integer existing = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM app_user WHERE LOWER(email) = LOWER(?) OR user_id = ?",
                Integer.class,
                newUser.getEmail(),
                newUser.getUserId()
        );

        if (existing != null && existing > 0) {
            return false;
        }

        String role = normalizeRole(newUser.getRole());
        newUser.setRole(role);

        try {
            jdbcTemplate.update(
                    "INSERT INTO app_user (user_id, name, email, password, role) VALUES (?, ?, ?, ?, ?)",
                    newUser.getUserId(),
                    newUser.getName(),
                    newUser.getEmail(),
                    newUser.getPassword(),
                    role
            );

            insertSubtype(newUser.getUserId(), role);
            return true;
        } catch (DuplicateKeyException ex) {
            return false;
        }
    }

    public User loginUser(String email, String password) {
        List<User> users = jdbcTemplate.query(
                "SELECT user_id, name, email, password, role FROM app_user " +
                        "WHERE LOWER(email) = LOWER(?) AND password = ?",
                (rs, rowNum) -> new User(
                        rs.getString("user_id"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("password"),
                        rs.getString("role")
                ),
                email,
                password
        );

        return users.isEmpty() ? null : users.get(0);
    }

    public List<User> getUsers() {
        return jdbcTemplate.query(
                "SELECT user_id, name, email, password, role FROM app_user ORDER BY name",
                (rs, rowNum) -> new User(
                        rs.getString("user_id"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("password"),
                        rs.getString("role")
                )
        );
    }

    @Transactional
    public boolean updateUserProfile(String userId, User updatedUserDetails) {
        int rows = jdbcTemplate.update(
                "UPDATE app_user SET name = ?, email = ?, password = ? WHERE user_id = ?",
                updatedUserDetails.getName(),
                updatedUserDetails.getEmail(),
                updatedUserDetails.getPassword(),
                userId
        );
        return rows > 0;
    }

    @Transactional
    public void deleteUser(String userId) {
        jdbcTemplate.update("DELETE FROM app_user WHERE user_id = ?", userId);
    }

    private String normalizeRole(String role) {
        if (role == null || role.isBlank() || role.equalsIgnoreCase("Guest")) {
            return "Customer";
        }
        if (role.equalsIgnoreCase("Admin")) {
            return "Admin";
        }
        if (role.equalsIgnoreCase("Staff")) {
            return "Staff";
        }
        return "Customer";
    }

    private void insertSubtype(String userId, String role) {
        switch (role) {
            case "Admin" -> jdbcTemplate.update("INSERT INTO admin (user_id) VALUES (?)", userId);
            case "Staff" -> jdbcTemplate.update("INSERT INTO staff (user_id) VALUES (?)", userId);
            default -> jdbcTemplate.update("INSERT INTO customer (user_id) VALUES (?)", userId);
        }
    }
}
