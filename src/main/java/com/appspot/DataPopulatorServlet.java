package com.appspot;

import com.google.appengine.api.memcache.MemcacheServiceFactory;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by eljah32 on 12/19/2015.
 */
public class DataPopulatorServlet extends HttpServlet {
    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) {
        MemcacheServiceFactory.getMemcacheService().clearAll(); //clearing cache since we are updating the db
        String r1=req.getParameter("r1");
        String r2=req.getParameter("r2");
        DataPopulator.main(new String[]{r1,r2});
        String country=req.getHeader("X-AppEngine-Country");
        String region=req.getHeader("X-AppEngine-Region");
        // https://ru.wikipedia.org/wiki/ISO_3166-2:RU yahoooo!
        String city=req.getHeader("X-AppEngine-City");
        String coordinates=req.getHeader("X-AppEngine-CityLatLong");

        System.out.println(region);
    }
}