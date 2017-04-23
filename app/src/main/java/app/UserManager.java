package app;

import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;
import java.util.Calendar;
import java.util.Date;

public class UserManager {
    private Connection connection;
    private String cookie;

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

    public String createCookie(String username) throws SQLException {  // TODO hash cookie
        //PreparedStatement addCookie = connection.prepareStatement("UPDATE user SET cookie = ?, cookie_expiration ? WHERE username = " + username);  // TODO syntax error?

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.HOUR_OF_DAY, 1);
        calendar.getTimeInMillis();

        cookie = username; // TEMPORARY

        return cookie;
    }

    public boolean checkCookie(String cookie, String username) throws SQLException {
        PreparedStatement checkCookie = connection.prepareStatement("SELECT cookie_expiration FROM user WHERE username =" +     // TODO syntax error?
                username + " AND cookie = " + cookie);

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

    private String hash(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt(12));
    }

    private boolean validatePassword(String plainPassword, String hashedPassword) {
        return BCrypt.checkpw(plainPassword, hashedPassword);
    }
}
