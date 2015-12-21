package com.appspot;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.googlecode.objectify.ObjectifyService;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Created by eljah32 on 12/20/2015.
 */
public class WelcomeServlet extends HttpServlet {
    public void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        String cookieKm = 1303 + "";
        String cookieCount = 15 + "";
        boolean kmFount = false;
        boolean countFount = false;
        int km = -1;
        int count = -1;


        Cookie[] cookies = req.getCookies();
        if (cookies != null) {
            for (int i = 0; i < cookies.length; i++) {
                Cookie cookie = cookies[i];
                String cookieName = cookie.getName();
                String cookieValue = cookie.getValue();
                if (cookieName.equals("km")) {
                    kmFount = true;
                    cookieKm = cookieValue;
                }
                if (cookieName.equals("count")) {
                    countFount = true;
                    cookieCount = cookieValue;
                }
            }
        }

        if (req.getAttribute("count") != null) {
            count = Integer.parseInt(req.getParameter("count"));
            System.out.println("Attribute count is set: "+count);
        } else if (req.getParameter("count") != null && !req.getParameter("count").equals("")) {
            count = Integer.parseInt(req.getParameter("count"));
            System.out.println("Parameter count is set: "+count);
        } else if (countFount) {
            count = Integer.parseInt(cookieCount);
            System.out.println("Cookie count is set: "+cookieCount);
        } else {
            count = 15;
            System.out.println("Default count is set: 15");
        }


        if (req.getAttribute("km") != null) {
            km = Integer.parseInt(req.getParameter("km"));
            System.out.println("Attribute km is set: "+km);
        } else if (req.getParameter("km") != null && !req.getParameter("km").equals("")) {
            km = Integer.parseInt(req.getParameter("km"));
            System.out.println("Parameter km is set: "+km);
        } else if (kmFount) {
            km = Integer.parseInt(cookieKm);
            System.out.println("Cookie km is set: "+cookieKm);
        } else {
            String region = req.getHeader("X-AppEngine-Region");
            System.out.println(region);
            km = -1;
            if (region != null) {
                switch (region) {
                    case "ta":
                        km = 1303;
                        break;
                    case "yar":
                        km = 423;
                        break;
                    case "kos":
                        km = 601;
                        break;
                    case "ngr":
                        km = 850;
                        break;
                    case "cu":
                        km = 1185;
                        break;
                    case "me":
                        km = 1185;
                        break;
                    case "uly":
                        km = 1531;
                        break;
                    case "sam":
                        km = 1665;
                        break;
                    case "sar":
                        km = 2006;
                        break;
                    case "ast":
                        km = 3044;
                        break;
                    default:
                        km = 1303;

                }
            } else {
                km = 1303;

            }
            System.out.println("Geolocator km is set: " + km);

        }
        resp.addCookie(new Cookie("km", "" +
                km));
        req.setAttribute("km", ""+km);


        resp.addCookie(new Cookie("count", "" +
                count));
        req.setAttribute("count", count);

        List<UploadedFile> allfileslist = ObjectifyService.ofy()
                .load()
                .type(UploadedFile.class) // We want only Greetings
                //.ancestor(theBook)    // Anyone in this book
                //.order("-date")       // Most recent first - date is indexed.
                //.limit(5)             // Only show 5 of them.
                .list();

        req.setAttribute("allfiles", allfileslist);

        List<StreamGauge> allstreamgauges = ObjectifyService.ofy()
                .load()
                .type(StreamGauge.class) // We want only Greetings
                //.ancestor(theBook)    // Anyone in this book
                //.order("-date")       // Most recent first - date is indexed.
                //.limit(5)             // Only show 5 of them.
                .list();

        List<String[]> labels=new ArrayList<String[]>(){};
        for (StreamGauge st: allstreamgauges)
        {
            labels.add(new String[]{st.getName()+" ("+st.getKm()+")",st.getKm()+""});
        }

        req.setAttribute("streamgauges", labels);



        RequestDispatcher dispatcher = getServletContext().getRequestDispatcher("/show.jsp");
        try

        {
            dispatcher.forward(req, resp);
        } catch (
                ServletException e
                )
        {
            e.printStackTrace();
        }
    }
}

