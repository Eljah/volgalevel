package com.appspot;

import com.googlecode.objectify.ObjectifyService;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by eljah32 on 12/19/2015.
 */
public class DataPopulator {
    public static void main(String[] args) {

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        Calendar c = Calendar.getInstance();
        try {
            c.setTime(sdf.parse("15/11/2015"));
        } catch (ParseException e) {
            e.printStackTrace();
        }
    for (int i = 1; i < 30; i=i+2) {
            c.add(Calendar.DATE, 2);

            new DataEntry(1303l, c.getTime(), "olo", 83 + Math.random(), Math.random(),false);

        }
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
    */ }

}
