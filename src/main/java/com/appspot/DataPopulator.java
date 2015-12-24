package com.appspot;

import com.google.appengine.api.datastore.Blob;
import com.googlecode.objectify.ObjectifyService;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.URL;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by eljah32 on 12/19/2015.
 */
public class DataPopulator {
    static final String BASE_URL = "http://xn--80adbch2buek4ak3i.xn--p1ai/admingo/uploadimg/";
    static final int MAXCOUNTER = 3100;
    private static final Logger log = Logger.getLogger(DowloadAndParseNavigationDataServlet.class.getName());


    public static void main(String[] args) {

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        Calendar c = Calendar.getInstance();
        try {
            c.setTime(sdf.parse("15/11/2015"));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        //for (int i = 1; i < 30; i=i+2) {
        //        c.add(Calendar.DATE, 2);
///
        //           new DataEntry(1303l, c.getTime(), "olo", 83 + Math.random(), Math.random(),false);
//
        //       }
/*

        try {
            c.setTime(sdf.parse("15/11/2015"));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        c.add(Calendar.DATE, 1);
        for (int i = 1; i < 30; i=i+2) {
            c.add(Calendar.DATE, 2);

            new DataEntry(521l, c.getTime(), "olo", 83 + Math.random(),0d,true);

        }
    */


        int id = 0;
        try {
            while (id < MAXCOUNTER) {

                try {
                    id++;
                    URL url = new URL(BASE_URL + id + ".xls");

                    //String url = "https://pastvu.com/p/" + id;
                    //String url = "https://pastvu.com/p/365037";
                    //String url="https://ya.ru/";

                    try {

                        System.setProperty("https.protocols", "TLSv1.2,SSLv3,SSLv2Hello");
                        //System.setProperty("javax.net.debug", "all");
/*
            SSLContext sc = null;
            try {
                sc = SSLContext.getInstance("TLSv1.2");
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
            // Init the SSLContext with a TrustManager[] and SecureRandom()
            sc.init(null, trustCerts, new java.security.SecureRandom());
*/


                        //URL obj = new URL(url);


                        InputStream excelFileToRead = null;
                        try {
                            excelFileToRead = url.openStream();
                            log.info("Document successfully loaded from " + url.toString());
                        } catch (IOException e) {
                            log.severe("Document can't be loaded at " + url.toString());
                            e.printStackTrace();
                        }

                        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                        ByteArrayOutputStream buffer1 = new ByteArrayOutputStream();

                        int nRead;
                        byte[] data = new byte[16384];


                        try {
                            while ((nRead = excelFileToRead.read(data, 0, data.length)) != -1) {
                                buffer.write(data, 0, nRead);
                                buffer1.write(data, 0, nRead);

                            }
                            buffer.flush();
                            buffer1.flush();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }


                        HSSFWorkbook wb = null;
                        try {
                            wb = new HSSFWorkbook(new ByteArrayInputStream(buffer.toByteArray()));
                            log.info("Document successfully parsed");
                        } catch (IOException e) {
                            log.severe("Document can't be pasred as XLS");
                            e.printStackTrace();
                        }

                        String datecellstrin = null;
                        HSSFSheet sheet = wb.getSheet("уровни воды");
                        if (sheet != null) {
                            log.info("Levels sheet fount");
                            HSSFCell datecell = sheet.getRow(11).getCell(9);
                            if (datecell != null) {
                                log.info("Document date is shown as " + datecell.getStringCellValue() + " from th corresponding collumn ");
                                datecellstrin = datecell.getStringCellValue();
                            } else {
                                log.info("Document date is not fount in the corresponding cell; cell not fount ");
                            }
                        }

                        Pattern pattern = Pattern.compile("(\\d{2}.\\d{2}.\\d{4}).");
                        Date date = null;
                        Matcher matcher = pattern.matcher(datecellstrin);
                        if (matcher.find()) {
                            try {
                                date = new SimpleDateFormat("dd.MM.yyyy").parse(matcher.group(0));
                                log.info("Document date is parsed as " + date + " from document name " + datecellstrin);
                            } catch (ParseException e) {
                                date = null;
                                log.severe("Document date parse error in document name " + datecellstrin);
                            }

                        } else {
                            log.severe("Date not parsed: date format group ot fount");

                        }

                        Blob imageBlob = new Blob(buffer1.toByteArray());
                        UploadedFile file = new UploadedFile(url.getFile(), imageBlob, date.getTime());

                        InputStream excelfileagain = new ByteArrayInputStream(file.getFile().getBytes());

                        wb = null;
                        try {
                            wb = new HSSFWorkbook(excelfileagain);
                            log.info("Document successfully parsed");
                        } catch (IOException e) {
                            log.severe("Document can't be pasred as XLS");
                            e.printStackTrace();
                        }
                        sheet = wb.getSheet("уровни воды");
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
                            List<DataEntry> dataentries = new ArrayList<DataEntry>() {
                            };
                            while (rows.hasNext()) {
                                row = (HSSFRow) rows.next();
                                if (row.getRowNum() >= rowTableStarts.getRowNum() && row.getRowNum() < 50) {
                                    Long km = (long) row.getCell(0).getNumericCellValue();
                                    if (km == 0) {
                                        km = ((long) (sheet.getRow(row.getRowNum() - 1)).getCell(0).getNumericCellValue() + 1);
                                    }
                                    String pointname = row.getCell(1, Row.CREATE_NULL_AS_BLANK).getStringCellValue();
                                    String phys = row.getCell(2, Row.CREATE_NULL_AS_BLANK).getStringCellValue();
                                    Double level = row.getCell(3, Row.CREATE_NULL_AS_BLANK).getNumericCellValue();
                                    Double delta = null;
                                    switch (row.getCell(4).getCellType()) {
                                        case 0:
                                            delta = row.getCell(4, Row.CREATE_NULL_AS_BLANK).getNumericCellValue();
                                            break;
                                        case 1:
                                            delta = Double.parseDouble(row.getCell(4, Row.CREATE_NULL_AS_BLANK).getStringCellValue());
                                            break;
                                    }
                                    //todo Oka Vetluga
                                    log.info("Km: " + km);
                                    new StreamGauge(km, pointname);
                                    dataentries.add(new DataEntry(km, date, phys, level, delta, false));
                                }

                            }
                            log.info("All the data successfully extracted from the spreadsheet");
                            int extrapolationStartValue = -1;
                            int extrapolationEndValue = -1;
                            boolean extrapolationstartedflag = false;
                            DataEntry[] dataEntriesArray = dataentries.toArray(new DataEntry[dataentries.size()]);

                            for (int i = 0; i < dataEntriesArray.length; i++) {
                                if (i > 0 && (dataEntriesArray[i].level == 0) && (extrapolationstartedflag == false)) {
                                    extrapolationStartValue = i - 1;
                                    extrapolationstartedflag = true;
                                }

                                if (i > 0 && (dataEntriesArray[i].level != 0) && (extrapolationstartedflag == true)) {
                                    extrapolationEndValue = i;
                                    extrapolationstartedflag = false;
                                    long calculatedKmDelta = (dataEntriesArray[extrapolationEndValue].streamGauge.getId() - dataEntriesArray[extrapolationStartValue].streamGauge.getId());
                                    Double calculatedLevelDelta = (dataEntriesArray[extrapolationEndValue].level - dataEntriesArray[extrapolationStartValue].level);
                                    Double LevelPerKm = calculatedLevelDelta / calculatedKmDelta;

                                    for (int j = extrapolationStartValue + 1; j <= (extrapolationEndValue - 1); j++) {
                                        long kmsBetween = (dataEntriesArray[j].streamGauge.getId() - dataEntriesArray[j - 1].streamGauge.getId());
                                        new DataEntry(dataEntriesArray[j].streamGauge.getId(), date, dataEntriesArray[j].phys, dataEntriesArray[extrapolationStartValue].level + kmsBetween * LevelPerKm, dataEntriesArray[j].delta, true);
                                    }
                                }
                            }

                        } else {
                            log.severe("Levels sheet not fount");
                        }


                    } catch (Exception e) {
                    }
                } catch(Exception e)
                {
if (e instanceof org.apache.poi.poifs.filesystem.NotOLE2FileException) {log.info("File was not XLS");};
                }
            }
        } catch (Exception e) {

        }
    }
}







