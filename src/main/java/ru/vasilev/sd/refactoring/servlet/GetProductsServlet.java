package ru.vasilev.sd.refactoring.servlet;

import ru.vasilev.sd.refactoring.dao.ProductDAO;
import ru.vasilev.sd.refactoring.util.HTMLResponseMaker;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
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
            HTMLResponseMaker.withHTMLWrapper(response.getWriter(), writer -> resultList.forEach(entry ->
                    writer.println(HTMLResponseMaker.makeRow(entry.getKey(), entry.getValue().toString()))
            ));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        response.setContentType("text/html");
        response.setStatus(HttpServletResponse.SC_OK);
    }
}
