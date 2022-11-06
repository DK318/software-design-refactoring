package ru.vasilev.sd.refactoring.servlet;

import ru.vasilev.sd.refactoring.dao.ProductDAO;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.Map;

/**
 * @author akirakozov
 */
public class QueryServlet extends HttpServlet {
    private final ProductDAO productDAO = new ProductDAO("jdbc:sqlite:test.db");

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String command = request.getParameter("command");
        ProductDAO.AggregateQuery query = ProductDAO.AggregateQuery.fromString(command);
        PrintWriter writer = response.getWriter();

        if (query == null) {
            writer.println("Unknown command: " + command);
        } else {
            try {
                writer.println("<html><body>");
                Map.Entry<String, Long> result = productDAO.aggregate(query);
                switch (query) {
                    case MAX:
                        writer.println("<h1>Product with max price: </h1>");
                        writer.println(result.getKey() + "\t" + result.getValue().toString() + "</br>");
                        break;
                    case MIN:
                        writer.println("<h1>Product with min price: </h1>");
                        writer.println(result.getKey() + "\t" + result.getValue().toString() + "</br>");
                        break;
                    case SUM:
                        writer.println("Summary price: ");
                        writer.println(result.getValue());
                        break;
                    case COUNT:
                        writer.println("Number of products: ");
                        writer.println(result.getValue());
                        break;
                }
                writer.println("</body></html>");
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        response.setContentType("text/html");
        response.setStatus(HttpServletResponse.SC_OK);
    }

}
