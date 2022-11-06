package ru.vasilev.sd.refactoring.servlet;

import ru.vasilev.sd.refactoring.dao.ProductDAO;
import ru.vasilev.sd.refactoring.util.HTMLResponseMaker;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
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

        if (query == null) {
            response.getWriter().println("Unknown command: " + command);
        } else {
            try {
                Map.Entry<String, Long> result = productDAO.aggregate(query);
                HTMLResponseMaker.withHTMLWrapper(response.getWriter(), writer -> {
                    switch (query) {
                        case MAX:
                            writer.println(HTMLResponseMaker.makeHeader("Product with max price: "));
                            writer.println(HTMLResponseMaker.makeRow(result.getKey(), result.getValue().toString()));
                            break;
                        case MIN:
                            writer.println(HTMLResponseMaker.makeHeader("Product with min price: "));
                            writer.println(HTMLResponseMaker.makeRow(result.getKey(), result.getValue().toString()));
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
                });
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        response.setContentType("text/html");
        response.setStatus(HttpServletResponse.SC_OK);
    }

}
