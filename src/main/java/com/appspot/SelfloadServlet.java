package com.appspot;

import com.google.appengine.api.datastore.Blob;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by eljah32 on 4/13/2016.
 */
public class SelfloadServlet extends HttpServlet {
    private static final Logger log = Logger.getLogger(DowloadAndParseNavigationDataServlet.class.getName());

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        MemcacheServiceFactory.getMemcacheService().clearAll(); //clearing cache since we are updating the db
        long filedate = Long.parseLong(req.getParameter("date"));
        fileFor(filedate, resp);
    }

    void fileFor(long dt, HttpServletResponse res) {

        UploadedFile file = new UploadedFile(dt);
        Blob fileBytes = file.getFile();

        Date date = new Date(dt);

        InputStream excelfileagain = new ByteArrayInputStream(file.getFile().getBytes());

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


                    Double level = null;
                    try {
                        level = row.getCell(3, Row.CREATE_NULL_AS_BLANK).getNumericCellValue();
                    } catch (java.lang.IllegalStateException exception) {
                        level = Double.parseDouble(row.getCell(3, Row.CREATE_NULL_AS_BLANK).getStringCellValue().replaceAll("([^\\d]+)", "."));

                    } catch (java.lang.NumberFormatException exception) {
                        level = Double.parseDouble(row.getCell(3, Row.CREATE_NULL_AS_BLANK).getStringCellValue().replaceAll("([^\\d]+)", "."));
                    }

                    Double delta = null;
                    try {
                        delta = row.getCell(4, Row.CREATE_NULL_AS_BLANK).getNumericCellValue();
                    } catch (java.lang.IllegalStateException exception) {
                        delta = Double.parseDouble(row.getCell(4, Row.CREATE_NULL_AS_BLANK).getStringCellValue().replaceAll("([^\\d]+)", "."));
                    } catch (java.lang.NumberFormatException exception) {
                        delta = Double.parseDouble(row.getCell(4, Row.CREATE_NULL_AS_BLANK).getStringCellValue().replaceAll("([^\\d]+)", "."));
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

            // serve the first image
        }
    }
}
