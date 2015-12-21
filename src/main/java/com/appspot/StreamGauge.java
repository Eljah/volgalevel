package com.appspot;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;

/**
 * Created by eljah32 on 12/18/2015.
 */
@Cache
@Entity
public class StreamGauge {
    @Id
    private Long km;
    public String name;

    public Long getKm() {
        return this.km;
    }

    public String getName() {
        return this.name;
    }

    StreamGauge() {
    }

    StreamGauge(Long km) {
        this.km=km;
        this.name=ObjectifyService.ofy().load().entity(this).now().name;

    }

    StreamGauge(Long km, String name) {
        this();
        this.km = km;
        if (
                name != null) {
            this.name = name;
        }
        ;  //km is unique; we update name if it was changed;
        ObjectifyService.ofy().save().entity(this).now();
    }
}
