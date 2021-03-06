package app;

import org.mindrot.jbcrypt.BCrypt;

import java.security.SecureRandom;
import java.sql.*;
import java.util.*;
import java.util.Date;

public class UserManager {
    private Connection connection;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final String CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final int COOKIE_LENGTH = 128;

    public UserManager(Connection connection) {
        this.connection = connection;
    }

    public boolean loginUser(String username, String password) throws SQLException {
        PreparedStatement loginStatement = connection.prepareStatement("SELECT password FROM user WHERE username = ?");
        loginStatement.setString(1, username);
        ResultSet result = loginStatement.executeQuery();

        if (result.next()) {
            String hashedPassword = result.getString(1);

            if (validatePassword(password, hashedPassword)) {
                return true;
            }
        }

        return false;
    }

    public boolean registerUser(String username, String password) throws SQLException {
        PreparedStatement isTableEmtpy = connection.prepareStatement("SELECT EXISTS (SELECT * FROM user)");
        ResultSet result = isTableEmtpy.executeQuery();
        boolean adminPermission = false;

        if (result.next()) {  // first account is by default an admin
            if (!result.getBoolean(1)) {
                adminPermission = true;
            }
        }

        PreparedStatement userExist = connection.prepareStatement("SELECT EXISTS (SELECT * FROM user WHERE username = ?)");
        userExist.setString(1, username);
        ResultSet userExistResult = userExist.executeQuery();

        if (userExistResult.next()) {
            if (userExistResult.getBoolean(1)) {
                return false;
            }
        }

        PreparedStatement registerNewUser = connection.prepareStatement("INSERT INTO user (username, password, permission) VALUES (?, ?, ?)");
        registerNewUser.setString(1, username);
        registerNewUser.setString(2, hash(password));
        registerNewUser.setBoolean(3, adminPermission);
        registerNewUser.executeUpdate();

        return true;
    }

    public String createCookie(String username) throws SQLException {
        PreparedStatement addCookie = connection.prepareStatement("UPDATE user SET cookie = ?, cookie_expiration = ? WHERE username = ?");

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.HOUR_OF_DAY, 1);
        String cookie = hashCookie();

        addCookie.setString(1, cookie);
        addCookie.setLong(2, calendar.getTimeInMillis());
        addCookie.setString(3, username);
        addCookie.executeUpdate();

        return cookie;
    }

    public boolean checkCookie(String cookie) throws SQLException {
        if (cookie == null) {
            return false;
        }

        PreparedStatement checkCookie = connection.prepareStatement("SELECT cookie_expiration FROM user WHERE cookie = ?");
        cookie = cookie.replace("id=", "");

        checkCookie.setString(1, cookie);
        checkCookie.executeQuery();

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());

        ResultSet result = checkCookie.executeQuery();

        if (result.next()) {
            if (result.getLong(1) > calendar.getTimeInMillis()) {
                return true;
            }
        }
        return false;
    }

    public String getUsername(String cookie) throws SQLException {
        if (cookie != null) {
            cookie = cookie.replace("id=", "");
        }

        String username = new String();

        if (checkCookie(cookie)) {
            PreparedStatement retrieveUsername = connection.prepareStatement("SELECT username FROM user WHERE cookie = ?");
            retrieveUsername.setString(1, cookie);
            ResultSet result = retrieveUsername.executeQuery();

            if (result.next()) {
                username = result.getString(1);
            }
        }

        return username;
    }

    public boolean isAdmin(String cookie) throws SQLException {
        if (cookie == null) {
            return false;
        }

        cookie = cookie.replace("id=", "");

        PreparedStatement retrievePermission = connection.prepareStatement("SELECT permission FROM user WHERE cookie = ?");
        retrievePermission.setString(1, cookie);
        ResultSet result = retrievePermission.executeQuery();

        boolean permission = false;

        if (result.next()) {
            permission = result.getBoolean(1);
        }

        return permission;
    }

    public Map<String, Boolean> getUsernames() throws SQLException {
        Map<String, Boolean> usernames = new HashMap<>();

        PreparedStatement retrieveUsernames = connection.prepareStatement("SELECT username, permission FROM user");
        ResultSet result = retrieveUsernames.executeQuery();

        while (result.next()) {
            usernames.put(result.getString(1), result.getBoolean(2));
        }

        return usernames;
    }

    public void deleteUser(String username) throws SQLException {
        PreparedStatement deleteStatement = connection.prepareStatement("DELETE FROM user WHERE username = ?");
        deleteStatement.setString(1, username);
        deleteStatement.execute();
    }

    private String hash(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt(12));
    }

    private boolean validatePassword(String plainPassword, String hashedPassword) {
        return BCrypt.checkpw(plainPassword, hashedPassword);
    }

    private String hashCookie() {
        StringBuilder random = new StringBuilder(COOKIE_LENGTH);

        for (int i = 0; i < COOKIE_LENGTH; ++i) {
            random.append(CHARS.charAt(SECURE_RANDOM.nextInt(CHARS.length())));
        }

        return random.toString();
    }
}
