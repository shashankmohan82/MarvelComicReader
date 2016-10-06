package com.marvel.comic.comicreader.model;

import android.graphics.Bitmap;

import io.realm.RealmModel;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.RealmClass;

/**
 * Created by shashank on 8/4/2016.
 */
@RealmClass
public class SuperHero implements RealmModel {

    private String name;

    @PrimaryKey
    private String id;

    public SuperHero(){

    }
    public String getId(){
        return id;
    }

    public SuperHero(String id,String name){
        this.id = id;
        this.name = name;
    }
    public String getName(){
        return name;
    }


}
