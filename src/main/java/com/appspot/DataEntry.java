package com.appspot;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.annotation.*;

import java.util.Date;

/**
 * Created by eljah32 on 12/18/2015.
 */
@Cache
@Entity
public class DataEntry {
    @Parent
    Key<StreamGauge> streamGauge;
    @Id
    public Long date;
    private Date visibleDate;
    public String phys;
    public Double level;
    public Double delta;
    public boolean extrapolation;

    DataEntry()
    {

    }

    DataEntry(Long km, Date date, String phys, Double level, Double delta, boolean extrapolation)
    {
        this();
        this.date=date.getTime();
        this.visibleDate=date;
        this.extrapolation=extrapolation;
        this.streamGauge=Key.create(StreamGauge.class,km);
       /*
        Key<StreamGauge> streamGaugeKey=Key.create(StreamGauge.class,km);
        this.date=date;  //km is unique; we update name if it was changed;
        DataEntry checkIfExist=ObjectifyService.ofy().load().type(DataEntry.class).filter("delta",2.0).first().now();
        if (checkIfExist!=null)
        {*/
            if (phys!=null) {
           //     checkIfExist.phys = phys;
                this.phys=phys;
            }
            if (level!=null) {
            //    checkIfExist.level = level;
                this.level=level;
            }
            if (delta!=null) {
            //    checkIfExist.delta = delta;
                this.delta=delta;
            }

            ObjectifyService.ofy().save().entity(this).now();
  /*      }
        else
        {
            this.phys=phys;
            this.level=level;
            this.delta=delta;
            this.streamGauge=streamGaugeKey;
            ObjectifyService.ofy().save().entity(this).now();
        }
*/
    }

}
