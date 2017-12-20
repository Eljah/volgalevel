package com.appspot;

/**
 * Created by eljah32 on 2/25/2017.
 */
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.appengine.api.datastore.Blob;
import com.google.code.appengine.awt.*;
import com.google.code.appengine.awt.image.BufferedImage;
import com.google.code.appengine.imageio.ImageIO;
import com.rometools.rome.feed.synd.SyndEntryImpl;
import org.jCharts.axisChart.AxisChart;
import org.jCharts.chartData.AxisChartDataSet;
import org.jCharts.chartData.ChartDataException;
import org.jCharts.chartData.DataSeries;
import org.jCharts.encoders.JPEGEncoder;
import org.jCharts.encoders.PNGEncoder;
import org.jCharts.imageMap.ImageMap;
import org.jCharts.properties.*;
import org.jCharts.properties.util.ChartFont;
import org.jCharts.test.TestDataGenerator;
import org.jCharts.types.ChartType;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public class ChartServlet  extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        long filedate=Long.parseLong(req.getParameter("date"));
        fileFor(filedate, resp);
    }

    void fileFor(long date, HttpServletResponse res) {

        // serve the first image
        res.setContentType("image/png");
        //res.setHeader("Content-Disposition", "attachment; filename="+file.getName());
        try {
            String[] xAxisLabels= { "Казань", "1999", "2000", "2001", "2002", "2003", "2004" };
            String xAxisTitle= "Years";
            String yAxisTitle= "Problems";
            String title= "Micro$oft at Work";
            //DataSeries dataSeries = new DataSeries( xAxisLabels, xAxisTitle, yAxisTitle, title );
            DataSeries dataSeries = new DataSeries( xAxisLabels, null, null, title );

            double[][] data= new double[][]{ { 250, 45, -36, 66, 145, 80, 55 } };
            String[] legendLabels= { "Bugs" };
            Paint[] paints=  TestDataGenerator.getRandomPaints( 1 );

            Stroke[] strokes= { LineChartProperties.DEFAULT_LINE_STROKE };
            Shape[] shapes= { PointChartProperties.SHAPE_DIAMOND };
            LineChartProperties lineChartProperties= new LineChartProperties( strokes, shapes );

            AxisChartDataSet axisChartDataSet= null;
            try {
                axisChartDataSet = new AxisChartDataSet( data, null, paints, ChartType.LINE, lineChartProperties );
            } catch (ChartDataException e) {
                e.printStackTrace();
            }
            dataSeries.addIAxisPlotDataSet( axisChartDataSet );

            ChartProperties chartProperties= new ChartProperties();
            AxisProperties axisProperties= new AxisProperties();
            axisProperties.setXAxisLabelsAreVertical(true);
            //LegendProperties legendProperties= new LegendProperties();

            ChartFont xScaleChartFont= new ChartFont( new Font( "Georgia Negreta cursiva", Font.PLAIN, 13 ), Color.blue );
            axisProperties.getXAxisProperties().setScaleChartFont( xScaleChartFont );
            AxisChart axisChart= new AxisChart( dataSeries, chartProperties, axisProperties, null, 1000, 400 );
            //legendProperties.setPlacement( LegendAreaProperties.RIGHT );
            chartProperties.setTitlePadding(10);

            try {
                PNGEncoder.encode(axisChart,res.getOutputStream());//axisChart.getBufferedImage();
            } catch (ChartDataException e) {
                e.printStackTrace();
            } catch (PropertyException e) {
                e.printStackTrace();
            }
            //ImageIO.write(bufferedImage, "png", res.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
