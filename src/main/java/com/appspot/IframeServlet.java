package com.appspot;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by eljah32 on 12/21/2015.
 */
public class IframeServlet extends HttpServlet {
    public void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        RequestDispatcher dispatcher = getServletContext().getRequestDispatcher("/iframe.jsp");
        if (req.getParameter("km") != null) {
            req.setAttribute("km", req.getParameter("km"));
            try {
                dispatcher.forward(req, resp);
            } catch (ServletException e) {
                e.printStackTrace();
            }

        }
        else {
            req.setAttribute("km", "1303");
            try {
                dispatcher.forward(req, resp);
            } catch (ServletException e) {
                e.printStackTrace();
            }

        }
    }
}




