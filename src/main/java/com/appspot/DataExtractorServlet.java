package com.appspot;

import com.google.visualization.datasource.DataSourceServlet;
import com.google.visualization.datasource.base.TypeMismatchException;
import com.google.visualization.datasource.datatable.ColumnDescription;
import com.google.visualization.datasource.datatable.DataTable;
import com.google.visualization.datasource.datatable.TableCell;
import com.google.visualization.datasource.datatable.TableRow;
import com.google.visualization.datasource.datatable.value.DateTimeValue;
import com.google.visualization.datasource.datatable.value.DateValue;
import com.google.visualization.datasource.datatable.value.Value;
import com.google.visualization.datasource.datatable.value.ValueType;
import com.google.visualization.datasource.query.Query;
import com.googlecode.objectify.ObjectifyService;
import com.ibm.icu.util.GregorianCalendar;
import com.ibm.icu.util.TimeZone;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

// This example extends DataSourceServlet
public class DataExtractorServlet extends DataSourceServlet {

    @Override
    public DataTable generateDataTable(Query query, HttpServletRequest request) {
        // Create a data table,
        DataTable data = new DataTable();

        String cookieKm = 1303 + "";
        String cookieCount = 15 + "";

        int km = -1;
        int count = -1;

        Cookie[] cookies = request.getCookies();
        boolean kmFount = false;
        boolean countFount = false;

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


        if (request.getAttribute("count") != null) {
            count = Integer.parseInt(request.getParameter("count"));
        } else if (request.getParameter("count") != null&&!request.getParameter("count").equals("")) {
            count = Integer.parseInt(request.getParameter("count"));
        } else if (countFount) {
            count = Integer.parseInt(cookieCount);
        } else {
            count = 15;
        }
        if (request.getAttribute("km") != null) {
            km = Integer.parseInt(request.getParameter("km"));
        } else if (request.getParameter("km") != null&&!request.getParameter("km").equals("")) {
            km = Integer.parseInt(request.getParameter("km"));
        } else if (kmFount) {
            km = Integer.parseInt(cookieKm);
        } else {
            km = 1303;
        }


        ArrayList cd = new ArrayList();
        cd.add(new ColumnDescription("date", ValueType.DATE, "Date"));
        cd.add(new ColumnDescription("level", ValueType.NUMBER, "Level"));

        ColumnDescription tooltip = new ColumnDescription("delta", ValueType.TEXT, "Delta");
        tooltip.setCustomProperty("role", "tooltip");
        tooltip.setCustomProperty("p", "{'html': true}");
        cd.add(tooltip);

        cd.add(new ColumnDescription("levelextrapolated", ValueType.NUMBER, "LevelExtrapolated"));

        ColumnDescription tooltip1 = new ColumnDescription("delta1", ValueType.TEXT, "Delta");
        tooltip1.setCustomProperty("role", "tooltip");
        tooltip1.setCustomProperty("p", "{'html': true}");
        cd.add(tooltip1);

        //cd.add("{role: 'tooltip', p:{html:true}}");
        //cd.add(new ColumnDescription("population", ValueType.NUMBER, "Population size"));
        //cd.add(new ColumnDescription("vegeterian", ValueType.BOOLEAN, "Vegetarian?"));

        data.addColumns(cd);

        List<DataEntry> datasets;
        if (count != -1) {
            datasets = ObjectifyService.ofy()
                    .load()
                    .type(DataEntry.class) // We want only Greetings
                    .ancestor(new StreamGauge((long) km))    // Anyone in this book
                    .orderKey(true)
                    .limit(count)             // Only show 5 of them.
                    .list();
        } else {
            datasets = ObjectifyService.ofy()
                    .load()
                    .type(DataEntry.class) // We want only Greetings
                    .ancestor(new StreamGauge((long) km))    // Anyone in this book
                    //.order("-date")
                    .list();

        }
        //Collections.reverse(datasets);
        for (DataEntry de : datasets) {
            Date pointDate = new Date(de.date);

            // Fill the data table.
            try {
                //data.addRowFromValues(11,12,-1);

                TableRow tr = new TableRow();
                SimpleDateFormat df = new SimpleDateFormat("yyyy");
                String year = df.format(pointDate);

                Calendar cal = Calendar.getInstance();
                cal.setTime(pointDate);
                int day = cal.get(Calendar.DAY_OF_MONTH);

                tr.addCell(new DateValue(Integer.parseInt(year), pointDate.getMonth(), day));
                if (de.extrapolation) {
                    tr.addCell(Value.getNullValueFromValueType(ValueType.NUMBER));
                    tr.addCell(Value.getNullValueFromValueType(ValueType.TEXT));
                    tr.addCell(de.level);
                    if (de.delta!=null) {
                        if (de.delta > 0) {
                            tr.addCell(pointDate.getMonth() + 1 + "/" + day + ": " + de.level + "м<br>Данные экстраполированы");
                        } else if (de.delta < 0) {
                            tr.addCell(pointDate.getMonth() + 1 + "/" + day + ": " + de.level + "м<br>Данные экстраполированы");
                        } else {
                            tr.addCell(pointDate.getMonth() + 1 + "/" + day + ": " + de.level + "м<br>Данные экстраполированы");
                        }
                    }
                    else
                    {
                        tr.addCell(pointDate.getMonth() + 1 + "/" + day + ": " + de.level + "м<br>Данные экстраполированы");
                    }
                } else {
                    tr.addCell(de.level);
                    if (de.delta!=null) {

                        if (de.delta > 0) {
                            tr.addCell(pointDate.getMonth() + 1 + "/" + day + ": " + de.level + "м " + de.phys + "<br><font size=\"+2\"><b>↑</b></font>" + de.delta + "см");
                        } else if (de.delta < 0) {
                            tr.addCell(pointDate.getMonth() + 1 + "/" + day + ": " + de.level + "м " + de.phys + "<br><font size=\"+2\"><b>↓</b></font>" + de.delta + "см");
                        } else {
                            tr.addCell(pointDate.getMonth() + 1 + "/" + day + ": " + de.level + "м " + de.phys + "<br><font size=\"+2\"><b>⟳</b></font>" + de.delta + "см");
                        }
                    }else{
                     tr.addCell(pointDate.getMonth() + 1 + "/" + day + ": " + de.level + "м<br>Изменения не передавались");
                    }
                    tr.addCell(Value.getNullValueFromValueType(ValueType.NUMBER));
                    tr.addCell(Value.getNullValueFromValueType(ValueType.TEXT));
                }

                data.addRow(tr);

                //data.addRowFromValues(new DateValue(calendar),12,-1);
                //data.addRowFromValues(new SimpleDateFormat("dd.MM.yyyy").parse("12.12.2015"),5.5,0);
                //data.addRowFromValues(new SimpleDateFormat("dd.MM.yyyy").parse("15.12.2015"),14,0.01);
                //data.addRowFromValues(new SimpleDateFormat("dd.MM.yyyy").parse("18.12.2015"),7,4);
            } catch (TypeMismatchException e) {
                System.out.println("Invalid type!");
                e.printStackTrace();
            }
        }

        return data;
    }
}