package com.appspot;

import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.datastore.Blob;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.googlecode.objectify.ObjectifyService;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by eljah32 on 12/18/2015.
 */

public class DowloadAndParseNavigationDataServlet extends HttpServlet {
    final String BASE_URL = "http://xn--80adbch2buek4ak3i.xn--p1ai";
    private static final Logger log = Logger.getLogger(DowloadAndParseNavigationDataServlet.class.getName());
    private BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) {

        MemcacheServiceFactory.getMemcacheService().clearAll(); //clearing cache since we are updating the db
        URL url = null;
        try {
            url = new URL(BASE_URL + "/info/path/");
        } catch (MalformedURLException e) {
            log.severe("Document path has error in " + BASE_URL + "/info/path/");
            e.printStackTrace();
        }

        Document document = null;
        try {
            document = Jsoup.parse(url.openStream(), "Windows-1251", url.toString());
            log.info("Page containing link to the document downloaded well from " + url.toString());
        } catch (IOException e) {
            log.severe("Page containing link to the document can't be loaded at " + url.toString());
            e.printStackTrace();
        }
        Element element = document.body().select("a:contains(Информационный бюллетень)").first();
        Pattern pattern = Pattern.compile(".(\\d{2}.\\d{2}.\\d{4}).");
        Date date = null;
        Matcher matcher = pattern.matcher(element.text());
        if (matcher.find()) {
            try {
                date = new SimpleDateFormat("dd.MM.yyyy").parse(matcher.group(0));
                log.info("Document date is parsed as " + date + " from document name " + element.text());
            } catch (ParseException e) {
                date = null;
                log.severe("Document date parse error in document name " + element.text());
            }

        } else {
            log.severe("Date not parsed: date format group ot fount");

        }
        log.info("The xls file url is " + BASE_URL + element.attr("href"));
        URL urlXLS = null;
        try {
            urlXLS = new URL(BASE_URL + element.attr("href"));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        InputStream excelFileToRead = null;
        try {
            excelFileToRead = urlXLS.openStream();
            log.info("Document successfully loaded from " + urlXLS.toString());
        } catch (IOException e) {
            log.severe("Document can't be loaded at " + urlXLS.toString());
            e.printStackTrace();
        }

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        int nRead;
        byte[] data = new byte[16384];


        try {
            while ((nRead = excelFileToRead.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }
            buffer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }


        Blob imageBlob = new Blob(buffer.toByteArray());
        UploadedFile file = new UploadedFile(urlXLS.getFile(), imageBlob,date.getTime());


        InputStream excelfileagain =new ByteArrayInputStream(file.getFile().getBytes());

        HSSFWorkbook wb = null;
        try {
            wb = new HSSFWorkbook(excelfileagain);
            log.info("Document successfully parsed");
        } catch (IOException e) {
            log.severe("Document can't be pasred as XLS");
            e.printStackTrace();
        }
        HSSFSheet sheet = wb.getSheet("уровни воды");
        if (sheet != null) {
            log.info("Levels sheet fount");
            HSSFCell datecell = sheet.getRow(11).getCell(9);
            if (datecell != null) {
                log.info("Document date is shown as " + datecell.getStringCellValue() + " from th corresponding collumn ");
            } else {
                log.info("Document date is not fount in the corresponding cell; cell not fount ");
            }
            HSSFRow row;
            HSSFRow rowTableStarts = sheet.getRow(19);
            Iterator rows = sheet.rowIterator();
            List<DataEntry> dataentries=new ArrayList<DataEntry>(){};
            while (rows.hasNext()) {
                row = (HSSFRow) rows.next();
                if (row.getRowNum() >= rowTableStarts.getRowNum() && row.getRowNum() < 50) {
                    Long km = (long) row.getCell(0).getNumericCellValue();
                    if (km == 0) {
                        km = ((long) (sheet.getRow(row.getRowNum() - 1)).getCell(0).getNumericCellValue() + 1);
                    }
                    String pointname = row.getCell(1, Row.CREATE_NULL_AS_BLANK).getStringCellValue();

                    String phys = row.getCell(2, Row.CREATE_NULL_AS_BLANK).getStringCellValue();


                    Double level = null;
                    try {
                        level = row.getCell(3, Row.CREATE_NULL_AS_BLANK).getNumericCellValue();
                    }
                    catch (java.lang.IllegalStateException exception)
                    {
                        level = Double.parseDouble(row.getCell(3, Row.CREATE_NULL_AS_BLANK).getStringCellValue().replaceAll("([^\\d]+)", "."));

                    } catch (java.lang.NumberFormatException exception) {
                        level = Double.parseDouble((row.getCell(3, Row.CREATE_NULL_AS_BLANK).getStringCellValue().replaceAll("([^\\d]+)", ".")));
                    }

                    Double delta=null;
                    try
                    {
                        delta = row.getCell(4, Row.CREATE_NULL_AS_BLANK).getNumericCellValue();
                    }
                    catch (java.lang.IllegalStateException exception)
                    {
                        delta = Double.parseDouble(row.getCell(4, Row.CREATE_NULL_AS_BLANK).getStringCellValue().replaceAll("([^\\d]+)", "."));
                    } catch (java.lang.NumberFormatException exception) {
                        delta = Double.parseDouble((row.getCell(4, Row.CREATE_NULL_AS_BLANK).getStringCellValue().replaceAll("([^\\d]+)", ".")));
                    }
                        //todo Oka Vetluga
                    log.info("Km: " + km);
                    new StreamGauge(km, pointname);
                    dataentries.add(new DataEntry(km, date, phys, level, delta, false));
                }

            }
            log.info("All the data successfully extracted from the spreadsheet");
            int extrapolationStartValue=-1;
            int extrapolationEndValue=-1;
            boolean extrapolationstartedflag=false;
            DataEntry[] dataEntriesArray=dataentries.toArray(new DataEntry[dataentries.size()]);

            for (int i=0; i<dataEntriesArray.length; i++)
            {
                 if (i>0&&(dataEntriesArray[i].level==0)&&(extrapolationstartedflag==false))
                 {
                     extrapolationStartValue=i-1;
                     extrapolationstartedflag=true;
                 }

                if (i>0&&(dataEntriesArray[i].level!=0)&&(extrapolationstartedflag==true))
                {
                    extrapolationEndValue=i;
                    extrapolationstartedflag=false;
                    long calculatedKmDelta=(dataEntriesArray[extrapolationEndValue].streamGauge.getId()-dataEntriesArray[extrapolationStartValue].streamGauge.getId());
                    Double calculatedLevelDelta=(dataEntriesArray[extrapolationEndValue].level-dataEntriesArray[extrapolationStartValue].level);
                    Double LevelPerKm=calculatedLevelDelta/calculatedKmDelta;

                    for (int j=extrapolationStartValue+1;j<=(extrapolationEndValue-1);j++)
                    {
                        long kmsBetween=(dataEntriesArray[j].streamGauge.getId()-dataEntriesArray[j-1].streamGauge.getId());
                        new DataEntry(dataEntriesArray[j].streamGauge.getId(),date,dataEntriesArray[j].phys,dataEntriesArray[extrapolationStartValue].level+kmsBetween*LevelPerKm,dataEntriesArray[j].delta,true);
                    }
                }
            }

        } else {
            log.severe("Levels sheet not fount");
        }
        resp.setContentType("text/plain");
        PrintWriter wr = null;
        resp.setCharacterEncoding("UTF-8");
        try {
            wr = resp.getWriter();
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (StreamGauge sg : ObjectifyService.ofy()
                .load()
                .type(StreamGauge.class)) {
            List<DataEntry> datasets = ObjectifyService.ofy()
                    .load()
                    .type(DataEntry.class) // We want only Greetings
                    .ancestor(sg)    // Anyone in this book
                   // .limit(5)             // Only show 5 of them.
                    .list();
            wr.println(sg.getKm()+"km ("+sg.getName()+")");
            for (DataEntry de : datasets) {
                wr.println(new Date(de.date) + ": " + de.level + "m (" + de.delta + ") cm, extrapolation="+de.extrapolation);
            }
        }
    }
}