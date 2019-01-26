package ru.altspace;

import java.sql.*;
import java.util.HashMap;

public class Network {

    private Connection con;
    private AltEconomy altEconomy = new AltEconomy();
    private Utils util = new Utils();
    private static HashMap<String, PreparedStatement> preparedStatements = new HashMap<>();

    private String url = altEconomy.getConfig().getString("url");
    private String user = altEconomy.getConfig().getString("user");
    private String password = altEconomy.getConfig().getString("password");


    public Connection getConnection() { return con; }

    public void createConnection() throws SQLException {

        try {

            Class.forName("com.mysql.jdbc.Driver");
            con = DriverManager.getConnection(url, user, password);
            util.getLogger().info("Database is not connecneted!");

        } catch (SQLException sqlEx) {

            sqlEx.printStackTrace();
            util.getLogger().info("Database is not connecneted!\n" + "-check params(url: " + url + "; user: " + user + "password: " + password + "\n-Checl internet connection");

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        synchronized (this) {
            if (con != null && !con.isClosed()) {
                return;
            }

            preparedStatements.put("addBalance", con.prepareStatement(

                    "UPDATE `alteconomy` SET `value`=? WHERE `player`=? AND `type`=?"));

            preparedStatements.put("getBalance", con.prepareStatement(

                    "SELECT `type` FROM `alteconomy` WHERE `player`=? AND `type`=?"));

            preparedStatements.put("addPlayer", con.prepareStatement(

                    "INSERT INTO `alteconomy` (player) VALUES (?);"));

            preparedStatements.put("addCurrency", con.prepareStatement(

                    "ALTER TABLE `alteconomy` ADD `type`=? FLOAT;"));
        }
    }

    public void addPlayer(final String player) {
        try {

            PreparedStatement addPlayer = preparedStatements.get("addPlayer");
            addPlayer.setString(1, player);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean addCurrency(final String currencyType) {
        try {

            PreparedStatement addCurrency = preparedStatements.get("addCurrency");
            addCurrency.setString(1, currencyType);
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public float getBalance(final String player,final String currencyType) {
        try {

            PreparedStatement getBalance = preparedStatements.get("getBalance");
            getBalance.setString(1, player);
            getBalance.setString(2, currencyType);

            ResultSet rs = getBalance.executeQuery();

            if (rs.next()) {
                return rs.getFloat(currencyType);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0;
    }

    public void addBalance(final String player,final String currencyType,final Float value) {

        try {

            PreparedStatement addBalance = preparedStatements.get("addBalance");
            addBalance.setFloat(2, getBalance(player, currencyType) + value);
            addBalance.setString(2, player);
            addBalance.setString(3, currencyType);
            addBalance.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
