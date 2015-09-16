package com.ratanachai.popularmovies;

import android.os.Parcel;
import android.os.Parcelable;

public class Review {
    private String key;
    private String name;
    private String type;
    private String site;

    //Take Json string and create an instance
    public Review(String key, String name, String type, String site){
        this.key = key;
        this.name = name;
        this.type = type;
        this.site = site;
    }

    public String[] getAll(){
        String[] all = {key, name, type, site};
        return all;
    }
    public String getName(){return name;}
    public String getKey(){return key;}

    /** Methods needed for implementing Parcelable
     * http://stackoverflow.com/questions/12503836/how-to-save-custom-arraylist-on-android-screen-rotate */

    private Review(Parcel in){
        key = in.readString();
        name = in.readString();
        type = in.readString();
        site = in.readString();
    }

    public int describeContents() {return 0; }
    public void writeToParcel(Parcel out, int flags){
        out.writeString(key);
        out.writeString(name);
        out.writeString(type);
        out.writeString(site);
    }
    public static final Parcelable.Creator<Review> CREATOR = new Parcelable.Creator<Review>(){
        public Review createFromParcel(Parcel in){
            return new Review(in);
        }
        public Review[] newArray(int size){
            return new Review[size];
        }
    };

}
