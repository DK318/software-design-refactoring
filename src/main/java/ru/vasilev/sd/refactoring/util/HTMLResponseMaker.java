package ru.vasilev.sd.refactoring.util;

import java.io.PrintWriter;
import java.util.function.Consumer;

public class HTMLResponseMaker {
    private HTMLResponseMaker() {
        // Utility class
    }

    public static void withHTMLWrapper(PrintWriter writer, Consumer<PrintWriter> act) {
        writer.println("<html><body>");
        act.accept(writer);
        writer.println("</body></html>");
    }

    public static String makeRow(String key, String value) {
        return key + "\t" + value + "</br>";
    }

    public static String makeHeader(String str) {
        return "<h1>" + str + "</h1>";
    }
}
