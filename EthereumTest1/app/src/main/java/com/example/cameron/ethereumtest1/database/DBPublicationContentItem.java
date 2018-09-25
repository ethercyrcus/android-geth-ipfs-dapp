package com.example.cameron.ethereumtest1.database;

import android.os.Parcel;
import android.os.Parcelable;

public class DBPublicationContentItem implements Parcelable {

    public int publicationIndex;
    public int publicationContentIndex;
    public String publishedByEthAddress;
    public String contentIPFS;
    public String imageIPFS;
    public String json;
    public String title;
    public String primaryText;
    public long publishedDate;
    public long uniqueSupporters;
    public String revenueWei;
    public int numComments;


    public DBPublicationContentItem (int publicationIndex, int publicationContentIndex,
                                     String publishedByEthAddress, String contentIPFS,
                                     String imageIPFS, String json, String title, String primaryText,
                                     long publishedDate, long uniqueSupporters, String revenueWei, int numComments) {
        this.publicationIndex = publicationIndex;
        this. publicationContentIndex = publicationContentIndex;
        this.publishedByEthAddress = publishedByEthAddress;
        this.contentIPFS = contentIPFS;
        this.imageIPFS = imageIPFS;
        this.json = json;
        this.title = title;
        this.primaryText = primaryText;
        this.publishedDate = publishedDate;
        this.uniqueSupporters = uniqueSupporters;
        this.revenueWei = revenueWei;
        this.numComments = numComments;
    }

    protected DBPublicationContentItem(Parcel in) {
        publicationIndex = in.readInt();
        publicationContentIndex = in.readInt();
        publishedByEthAddress = in.readString();
        contentIPFS = in.readString();
        imageIPFS = in.readString();
        json = in.readString();
        title = in.readString();
        primaryText = in.readString();
        publishedDate = in.readLong();
        uniqueSupporters = in.readLong();
        revenueWei = in.readString();
        numComments = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(publicationIndex);
        dest.writeInt(publicationContentIndex);
        dest.writeString(publishedByEthAddress);
        dest.writeString(contentIPFS);
        dest.writeString(imageIPFS);
        dest.writeString(json);
        dest.writeString(title);
        dest.writeString(primaryText);
        dest.writeLong(publishedDate);
        dest.writeLong(uniqueSupporters);
        dest.writeString(revenueWei);
        dest.writeLong(numComments);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<DBPublicationContentItem> CREATOR = new Creator<DBPublicationContentItem>() {
        @Override
        public DBPublicationContentItem createFromParcel(Parcel in) {
            return new DBPublicationContentItem(in);
        }

        @Override
        public DBPublicationContentItem[] newArray(int size) {
            return new DBPublicationContentItem[size];
        }
    };
}
