package com.appspot;

import com.google.appengine.api.datastore.Blob;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Parent;

import java.util.Date;

/**
 * Created by eljah32 on 12/21/2015.
 */
@Entity
public class UploadedFile {
    @Id
    public Long date;
    public Date datevisible;
    public String name;

    public Long getDate() {
        return date;
    }

    public void setDate(Long date) {
        this.date = date;
    }

    public Date getDatevisible() {
        return datevisible;
    }

    public void setDatevisible(Date datevisible) {
        this.datevisible = datevisible;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    Blob file;

    public UploadedFile() {
    }

    public UploadedFile(long date) {
        this.date = date;
        this.datevisible=new Date(date);
        this.file=ObjectifyService.ofy().load().entity(this).now().getFile();
        this.name=ObjectifyService.ofy().load().entity(this).now().getName();
    }


    public UploadedFile(String name, Blob image, long date) {
        this.date = date;
        this.datevisible=new Date(date);
        this.name = name;
        this.file = image;
        ObjectifyService.ofy().save().entity(this).now();
    }

    public Blob getFile() {
        return file;
    }

    public void setFile(Blob image) {
        this.file = image;
    }
}


