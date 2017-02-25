package com.appspot;

/**
 * Created by eljah32 on 2/24/2017.
 */

import com.google.appengine.api.memcache.ErrorHandlers;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.googlecode.objectify.ObjectifyService;
import com.rometools.rome.feed.CopyFrom;
import com.rometools.rome.feed.module.Module;
import com.rometools.rome.feed.module.ModuleImpl;
import com.rometools.rome.feed.synd.*;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedOutput;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.Document;
import org.jdom2.*;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.XMLOutputter;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;


import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.*;
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

    private static final Namespace ATOM_NS = Namespace.getNamespace("atom", FeedServlet.AtomNSModule.URI);

    private static final String DEFAULT_FEED_TYPE = "default.feed.type";
    private static final String FEED_TYPE = "type";
    private static final String MIME_TYPE = "application/xml; charset=UTF-8";
    private static final String COULD_NOT_GENERATE_FEED_ERROR = "Could not generate feed";

    private static final DateFormat DATE_PARSER = new SimpleDateFormat("yyyy-MM-dd");

    private String _defaultFeedType;

    public void init() {
        _defaultFeedType = getServletConfig().getInitParameter(DEFAULT_FEED_TYPE);
        // _defaultFeedType = (_defaultFeedType != null) ? _defaultFeedType : "atom_0.3";
        _defaultFeedType = (_defaultFeedType != null) ? _defaultFeedType : "rss_2.0";
    }

    public void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        try {
            String km = req.getParameter("km");
            String count = req.getParameter("count");
            Long kmLong = (km != null) ? Long.parseLong(km) : 1303;
            Integer countLong = (count != null) ? Integer.parseInt(count) : 5;
            StreamGauge streamGauge = new StreamGauge((long) kmLong);

            List<DataEntry> dataEntries = null;
            MemcacheService syncCache = MemcacheServiceFactory.getMemcacheService();
            syncCache.setErrorHandler(ErrorHandlers.getConsistentLogAndContinue(Level.INFO));
            dataEntries = (List<DataEntry>) syncCache.get(count + "_" + km); // Read from cache.

            if (dataEntries == null) {
                dataEntries = ObjectifyService.ofy()
                        .load()
                        .type(DataEntry.class) // We want only Greetings
                        .ancestor(streamGauge)    // Anyone in this book
                        .orderKey(true)
                        .limit(countLong)             // Only show 5 of them.
                        .list();
                syncCache.put(count + "_" + km, dataEntries);
            }


            SyndFeed feed = getFeed(req, dataEntries, streamGauge);

            String feedType = req.getParameter(FEED_TYPE);
            feedType = (feedType != null) ? feedType : _defaultFeedType;
            feed.setFeedType(feedType);
            //feed.setGenerator("com.appspot.AtomNSModuleGenerator");
            Module module = new AtomNSModuleImpl();
            res.setContentType(MIME_TYPE);
            SyndFeedOutput output = new SyndFeedOutput();

            Writer writer = new StringWriter();
            output.output(feed, writer);

            SAXBuilder db = null;
            Document doc = null;
            db = new SAXBuilder();
            InputSource is = new InputSource();
            is.setCharacterStream(new StringReader(writer.toString()));
            try {
                doc = db.build(is);
            } catch (JDOMException e) {
                e.printStackTrace();
            }

            Element root =
                    doc.getRootElement();

            Element channel = root.getChild("channel");

            FeedServlet.AtomNSModule atomNSModule = (FeedServlet.AtomNSModule) module;
            root.addNamespaceDeclaration(ATOM_NS);

            Element atomLink = new Element("link", ATOM_NS);
            atomNSModule.setLink("http://volgalevel.appspot.com/feed");
            atomLink.setAttribute("href", atomNSModule.getLink());
            atomLink.setAttribute("rel", "self");
            atomLink.setAttribute("type", "application/rss+xml");

            channel.addContent(0, atomLink);
            //res.getWriter().println(doc.toString());
            //output.output(feed, res.getWriter());

            XMLOutputter outputter = new XMLOutputter();
            outputter.output(doc, res.getWriter());
        } catch (FeedException ex) {
            String msg = COULD_NOT_GENERATE_FEED_ERROR;
            log(msg, ex);
            res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, msg);
        }
    }

    protected SyndFeed getFeed(HttpServletRequest req, List<DataEntry> dataEntries, StreamGauge streamGauge) throws IOException, FeedException {
        SyndFeed feed = new SyndFeedImpl();

        feed.setTitle("Архив уровней Волги");
        feed.setLink("http://volgalevel.appspot.com");
        feed.setDescription("Водомерный пост " + streamGauge.getName());

        List entries = new ArrayList();
        SyndEntry entry;
        SyndContent description;

        for (DataEntry dataEntry : dataEntries) {
            System.out.println(dataEntry.level);
            entry = new CustomEntryImpl();
            entry.setTitle(String.valueOf(dataEntry.visibleDate));
            entry.setLink("http://volgalevel.appspot.com/welcome?km=1303&count=" + dataEntries.indexOf(dataEntry));
            entry.setPublishedDate(dataEntry.visibleDate);
            description = new SyndContentImpl();
            description.setType("text/plain");
            NumberFormat formatter = new DecimalFormat("#0.00");
            description.setValue("Уровень: " + formatter.format(dataEntry.level) + " (" + dataEntry.delta + ")" + ((dataEntry.phys.trim() != "" && dataEntry.phys != null) ? ", " + dataEntry.phys : "") + (dataEntry.extrapolation ? ", уровень экстраполирован" : ""));
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


    public class CustomEntryImpl extends SyndEntryImpl {

        protected Date pubDate;

        @Override
        public Date getPublishedDate() {
            return pubDate;
        }

        @Override
        public void setPublishedDate(final Date pubDate) {
            this.pubDate = new Date(pubDate.getTime());
        }
    }

    public interface AtomNSModule extends Module {
        public static final String URI = "http://www.w3.org/2005/Atom";

        String getLink();

        void setLink(String href);
    }

    public class AtomNSModuleImpl extends ModuleImpl implements AtomNSModule {
        private String link;

        public AtomNSModuleImpl() {
            super(AtomNSModule.class, URI);
        }

        public String getLink() {
            return link;
        }

        public void setLink(String link) {
            this.link = link;
        }

        public Class getInterface() {
            return AtomNSModule.class;
        }

        @Override
        public void copyFrom(CopyFrom copyFrom) {
            AtomNSModule module = (AtomNSModule) copyFrom;
            module.setLink(this.link);
        }

        public void copyFrom(Object obj) {
            AtomNSModule module = (AtomNSModule) obj;
            module.setLink(this.link);
        }
    }


}