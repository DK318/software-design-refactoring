package ru.vasilev.sd.refactoring;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import ru.vasilev.sd.refactoring.servlet.AddProductServlet;
import ru.vasilev.sd.refactoring.servlet.GetProductsServlet;
import ru.vasilev.sd.refactoring.servlet.QueryServlet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ServletTest {
    private final AddProductServlet addProductServlet = new AddProductServlet();
    private final GetProductsServlet getProductsServlet = new GetProductsServlet();
    private final QueryServlet queryServlet = new QueryServlet();

    @BeforeClass
    public static void initDB() {
        String sql = "CREATE TABLE IF NOT EXISTS PRODUCT" +
                "(ID INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                " NAME           TEXT    NOT NULL, " +
                " PRICE          INT     NOT NULL)";
        execQuery(sql);
    }

    @After
    public void cleanDB() {
        String sql = "DELETE FROM PRODUCT";
        execQuery(sql);
    }

    @Test
    public void getProductsEmpty() {
        assertGetProducts(makeHTML());
    }

    @Test
    public void addOneProduct() {
        assertAddProduct("Skovorodka", "1234");
        assertGetProducts(makeHTML(makeRow("Skovorodka", "1234")));
    }

    @Test
    public void addOverlapping() {
        assertAddProduct("Beer", "300");
        assertAddProduct("Beer", "400");

        assertGetProducts(makeHTML(
                makeRow("Beer", "300"),
                makeRow("Beer", "400")
        ));
    }

    private void examples() {
        assertAddProduct("Shawarma", "100");
        assertAddProduct("Bottle of vodka", "1000");
        assertAddProduct("Bitcoin", "2000000");
        assertAddProduct("Patak", "0");
    }

    @Test
    public void aggregateMax() {
        examples();
        assertQuery("max", makeHTML(
                "<h1>Product with max price: </h1>",
                makeRow("Bitcoin", "2000000")
        ));
    }

    @Test
    public void aggregateMin() {
        examples();
        assertQuery("min", makeHTML(
                "<h1>Product with min price: </h1>",
                makeRow("Patak", "0")
        ));
    }

    @Test
    public void aggregateSum() {
        examples();
        assertQuery("sum", makeHTML(
                "Summary price: ",
                "2001100"
        ));
    }

    @Test
    public void aggregateCount() {
        examples();
        assertQuery("count", makeHTML(
                "Number of products: ",
                "4"
        ));
    }

    @Test
    public void aggregateUnexpected() {
        examples();
        assertQuery("stick bugged lol", "Unknown command: stick bugged lol\n");
    }

    private static void execQuery(String sql) {
        try (Connection c = DriverManager.getConnection("jdbc:sqlite:test.db")) {
            Statement stmt = c.createStatement();

            stmt.executeUpdate(sql);
            stmt.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private String makeHTML(String ...elements) {
        if (elements.length == 0) {
            return String.join("\n", "<html><body>", "</body></html>", "");
        } else {
            return String.join("\n", "<html><body>", String.join("\n", elements), "</body></html>", "");
        }
    }

    private String makeRow(String key, String val) {
        return key + "\t" + val + "</br>";
    }

    private void assertAddProduct(String name, String price) {
        assertServlet(addProductServlet, "GET", "OK\n", Map.of("name", name, "price", price));
    }

    private void assertGetProducts(String expected) {
        assertServlet(getProductsServlet, "GET", expected, Collections.emptyMap());
    }

    private void assertQuery(String command, String expected) {
        assertServlet(queryServlet, "GET", expected, Collections.singletonMap("command", command));
    }

    @SuppressWarnings("SameParameterValue")
    private static void assertServlet(HttpServlet servlet, String method, String expected, Map<String, String> parameters) {
        HttpServletResponse response = mock(HttpServletResponse.class);
        StringWriter stringWriter = new StringWriter();

        try (PrintWriter printWriter = new PrintWriter(stringWriter)) {
            HttpServletRequest request = mock(HttpServletRequest.class);
            parameters.forEach((key, value) ->
                    when(request.getParameter(key)).thenReturn(value)
            );

            when(request.getMethod()).thenReturn(method);

            when(response.getWriter()).thenReturn(printWriter);
            servlet.service(request, response);
            assertThat(stringWriter).hasToString(expected);
        } catch (IOException | ServletException e) {
            throw new RuntimeException(e);
        }
    }
}
