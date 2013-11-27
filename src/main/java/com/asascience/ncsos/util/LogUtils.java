package com.asascience.ncsos.util;

import java.io.PrintWriter;
import java.io.StringWriter;

public class LogUtils {

    public static String exceptionAsString(Exception exception) {
        /**
         * Gets the exception stack trace as a string.
         * @param exception
         * @return
         */
        {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            pw.print(" [ ");
            pw.print(exception.getClass().getName());
            pw.print(" ] ");
            pw.print(exception.getMessage());
            exception.printStackTrace(pw);
            return sw.toString();
        }
    }
}
