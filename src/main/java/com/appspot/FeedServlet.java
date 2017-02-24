package com.appspot;

/**
 * Created by eljah32 on 2/24/2017.
 */

import com.google.appengine.api.memcache.ErrorHandlers;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.googlecode.objectify.ObjectifyService;
import com.rometools.rome.feed.synd.*;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedOutput;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

/**
 * Sample Servlet that serves a feed created with ROME.
 * <p/>
 * The feed type is determined by the 'type' request parameter, if the parameter is missing it defaults
 * to the 'default.feed.type' servlet init parameter, if the init parameter is missing it defaults to 'atom_0.3'
 * <p/>
 *
 * @author Alejandro Abdelnur
 */
public class FeedServlet extends HttpServlet {
    private static final String DEFAULT_FEED_TYPE = "default.feed.type";
    private static final String FEED_TYPE = "type";
    private static final String MIME_TYPE = "application/xml; charset=UTF-8";
    private static final String COULD_NOT_GENERATE_FEED_ERROR = "Could not generate feed";

    private static final DateFormat DATE_PARSER = new SimpleDateFormat("yyyy-MM-dd");

    private String _defaultFeedType;

    public void init() {
        _defaultFeedType = getServletConfig().getInitParameter(DEFAULT_FEED_TYPE);
        _defaultFeedType = (_defaultFeedType != null) ? _defaultFeedType : "atom_0.3";
    }

    public void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        try {
            String km = req.getParameter("km");
            String count = req.getParameter("count");
            Long kmLong=Long.parseLong(km);
            Integer countLong=Integer.parseInt(count);
            StreamGauge streamGauge=new StreamGauge((long) kmLong);

            List<DataEntry> dataEntries=null;
            MemcacheService syncCache = MemcacheServiceFactory.getMemcacheService();
            syncCache.setErrorHandler(ErrorHandlers.getConsistentLogAndContinue(Level.INFO));
            dataEntries = (List<DataEntry>) syncCache.get(count +"_" +km); // Read from cache.

            if (dataEntries==null) {
                dataEntries = ObjectifyService.ofy()
                        .load()
                        .type(DataEntry.class) // We want only Greetings
                        .ancestor(streamGauge)    // Anyone in this book
                        .orderKey(true)
                        .limit(countLong)             // Only show 5 of them.
                        .list();
                syncCache.put(count + "_" + km, dataEntries);
            }


            SyndFeed feed = getFeed(req, dataEntries,streamGauge);

            String feedType = req.getParameter(FEED_TYPE);
            feedType = (feedType != null) ? feedType : _defaultFeedType;
            feed.setFeedType(feedType);

            res.setContentType(MIME_TYPE);
            SyndFeedOutput output = new SyndFeedOutput();
            output.output(feed, res.getWriter());
        } catch (FeedException ex) {
            String msg = COULD_NOT_GENERATE_FEED_ERROR;
            log(msg, ex);
            res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, msg);
        }
    }

    protected SyndFeed getFeed(HttpServletRequest req, List<DataEntry> dataEntries, StreamGauge streamGauge) throws IOException, FeedException {
        SyndFeed feed = new SyndFeedImpl();

        feed.setTitle("Архив уровней Волги имени Эрнста Галимовича Улумбекова");
        feed.setLink("http://volgalevel.appspot.com");
        feed.setDescription("Архив уровней Волги по данным водомерного поста "+streamGauge.getName());

        List entries = new ArrayList();
        SyndEntry entry;
        SyndContent description;

        for (DataEntry dataEntry : dataEntries) {
            System.out.println(dataEntry.level);
            entry = new SyndEntryImpl();
            entry.setTitle(String.valueOf(dataEntry.visibleDate));
            //entry.setLink("http://wiki.java.net/bin/view/Javawsxml/rome01");
            entry.setPublishedDate(dataEntry.visibleDate);
            description = new SyndContentImpl();
            description.setType("text/plain");
            description.setValue("Уровень: "+dataEntry.level+" ("+dataEntry.delta+")"+((dataEntry.phys!="")?", "+dataEntry.phys:""));
            entry.setDescription(description);
            entries.add(entry);
        }
      /*
        entry = new SyndEntryImpl();
        entry.setTitle("ROME v0.4");
        entry.setLink("http://wiki.java.net/bin/view/Javawsxml/rome04");
        try {
            entry.setPublishedDate(DATE_PARSER.parse("2004-09-24"));
        }
        catch (ParseException ex) {
            // IT CANNOT HAPPEN WITH THIS SAMPLE
        }
        description = new SyndContentImpl();
        description.setType("text/html");
        description.setValue("<p>Bug fixes, API changes, some new features, Unit testing completed</p>"+
                "<p>For details check the <a href=\"https://rometools.jira.com/wiki/display/ROME/Change+Log#ChangeLog-Changesmadefromv0.4tov0.5\">Changes Log for 0.4</a></p>");
        entry.setDescription(description);
        entries.add(entry);
*/
        feed.setEntries(entries);

        return feed;
    }

}