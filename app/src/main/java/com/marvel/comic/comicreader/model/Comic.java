package com.marvel.comic.comicreader.model;

import android.graphics.Bitmap;

import io.realm.RealmModel;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.RealmClass;

/**
 * Created by shashank on 8/5/2016.
 */
@RealmClass
public class Comic implements RealmModel {


    @PrimaryKey
    private String id;
    private String heroId;
    private String name;
    private int pageCount;
    public Comic(){

    }
    public String getHeroId(){
        return heroId;
    }
    public String getId(){
        return id;
    }

    public Comic(String id, String name,String heroId,int pageCount){
        this.id = id;
        this.name = name;
        this.heroId = heroId;
        this.pageCount = pageCount;

    }
    public int getPageCount(){
        return pageCount;
    }

    public String getName(){
        return name;
    }

}
