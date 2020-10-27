package com.github.taymindis.jh;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;

public class Helper {
    public static String getRequestBody2(HttpServletRequest req) throws IOException {
        BufferedReader br = req.getReader();
        String buf;
        StringBuilder sb = new StringBuilder();
        while ((buf = br.readLine()) != null) {
            sb.append(buf);
        }

        return sb.toString();
    }

    public static String getRequestBody(HttpServletRequest req) throws IOException {
        java.util.Scanner s = new java.util.Scanner(req.getInputStream(), "UTF-8").useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }
}
