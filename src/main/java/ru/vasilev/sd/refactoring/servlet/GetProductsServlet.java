package ru.vasilev.sd.refactoring.servlet;

import ru.vasilev.sd.refactoring.dao.ProductDAO;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.util.List;
import java.util.Map;

/**
 * @author akirakozov
 */
public class GetProductsServlet extends HttpServlet {
    private final ProductDAO productDAO = new ProductDAO("jdbc:sqlite:test.db");

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            List<Map.Entry<String, Long>> resultList = productDAO.getProducts();
            PrintWriter writer = response.getWriter();

            writer.println("<html><body>");
            resultList.forEach(entry -> writer.println(entry.getKey() + "\t" + entry.getValue().toString() + "</br>"));
            writer.println("</body></html>");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        response.setContentType("text/html");
        response.setStatus(HttpServletResponse.SC_OK);
    }
}
