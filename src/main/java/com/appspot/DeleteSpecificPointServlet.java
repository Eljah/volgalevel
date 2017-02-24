package com.appspot;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.cmd.QueryKeys;
import com.googlecode.objectify.impl.Keys;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by eljah32 on 2/23/2017.
 */
public class DeleteSpecificPointServlet extends HttpServlet {
    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) {
        MemcacheServiceFactory.getMemcacheService().clearAll(); //clearing cache since we are updating the db
        String date = req.getParameter("date");
        String streamgauge = req.getParameter("sg");

        List<DataEntry> des =
                null;
        List<com.googlecode.objectify.Key<DataEntry>> keys = null;
        try {

            System.out.println((new SimpleDateFormat("dd.MM.yyyy").parse(date).toString()));

            DataEntry dataEntry = ObjectifyService.ofy()
                    .load()
                    .type(DataEntry.class).
                            ancestor(new StreamGauge(Long.parseLong(streamgauge))).
                            filter("visibleDate =", (new SimpleDateFormat("dd.MM.yyyy").parse(date))).first().now();
            //System.out.println(dataEntry.date);
            //List<com.googlecode.objectify.Key<Object>> keysToDelete=ObjectifyService.ofy().load().ancestor(dataEntry).keys().list();
            List<com.googlecode.objectify.Key<DataEntry>> keysToDelete=ObjectifyService.ofy()
                    .load()
                    .type(DataEntry.class).
                            ancestor(new StreamGauge(Long.parseLong(streamgauge))).
                            filter("visibleDate =", (new SimpleDateFormat("dd.MM.yyyy").parse(date))).keys().list();
            ObjectifyService.ofy().delete().keys(keysToDelete);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
}

