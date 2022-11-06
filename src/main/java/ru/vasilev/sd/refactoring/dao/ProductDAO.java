package ru.vasilev.sd.refactoring.dao;

import java.sql.*;
import java.util.*;

public class ProductDAO {
    private final String connectionURL;

    public ProductDAO(String connectionURL) {
        this.connectionURL = connectionURL;
    }

    private <R> R selectQuery(String sql, SQLFunction<ResultSet, R> act) throws SQLException {
        try (Connection c = DriverManager.getConnection(connectionURL)) {
            Statement stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            R result = act.apply(rs);

            rs.close();
            stmt.close();

            return result;
        }
    }

    public void addProduct(String name, long price) throws SQLException {
        try (Connection c = DriverManager.getConnection(connectionURL)) {
            String sql = "INSERT INTO PRODUCT " +
                    "(NAME, PRICE) VALUES (\"" + name + "\"," + price + ")";
            Statement stmt = c.createStatement();
            stmt.executeUpdate(sql);
            stmt.close();
        }
    }

    public List<Map.Entry<String, Long>> getProducts() throws SQLException {
        return selectQuery("SELECT * FROM PRODUCT", rs -> {
            List<Map.Entry<String, Long>> resultList = new ArrayList<>();

            while (rs.next()) {
                String name = rs.getString("name");
                long price = rs.getLong("price");
                resultList.add(new AbstractMap.SimpleImmutableEntry<>(name, price));
            }

            return resultList;
        });
    }

    public Map.Entry<String, Long> aggregate(AggregateQuery query) throws SQLException {
        String sql = "";

        switch (query) {
            case MAX:
                sql = "SELECT * FROM PRODUCT ORDER BY PRICE DESC LIMIT 1";
                break;
            case MIN:
                sql = "SELECT * FROM PRODUCT ORDER BY PRICE LIMIT 1";
                break;
            case SUM:
                sql = "SELECT SUM(price) FROM PRODUCT";
                break;
            case COUNT:
                sql = "SELECT COUNT(*) FROM PRODUCT";
                break;
        }

        return selectQuery(sql, rs -> {
            if (rs.next()) {
                switch (query) {
                    case MAX:
                    case MIN:
                        String name = rs.getString("name");
                        long price = rs.getLong("price");
                        return new AbstractMap.SimpleImmutableEntry<>(name, price);
                    default:
                        return new AbstractMap.SimpleImmutableEntry<>("", rs.getLong(1));
                }
            }

            return null;
        });
    }

    @FunctionalInterface
    private interface SQLFunction<T, R> {
        R apply(T x) throws SQLException;
    }

    public enum AggregateQuery {
        MAX, MIN, SUM, COUNT;

        public static AggregateQuery fromString(String str) {
            switch (str) {
                case "max":
                    return MAX;
                case "min":
                    return MIN;
                case "sum":
                    return SUM;
                case "count":
                    return COUNT;
                default:
                    return null;
            }
        }
    }
}
