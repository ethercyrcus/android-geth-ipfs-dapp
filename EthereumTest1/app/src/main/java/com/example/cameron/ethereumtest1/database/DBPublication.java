package com.example.cameron.ethereumtest1.database;

import android.os.Parcel;
import android.os.Parcelable;

public class DBPublication implements Parcelable{

    public int publicationID;
    public String name;
    public String metaData;
    public String adminAddress;
  //  public int numAccessListAddresses;
    public int numPublished;
    public int minSupportCostWei;
    public int adminPaymentPercentage;
    public int uniqueSupporters;
    public boolean subscribedLocally;
    public long adminClaimsOwed;

    public DBPublication (int publicationID, String name, String metaData, String adminAddress,
                          //int numAccessListAddresses,
                          int numPublished, int minSupportCostWei,
                          int adminPaymentPercentage, int uniqueSupporters, boolean subscribedLocally, long adminClaimsOwed) {
        this.publicationID = publicationID;
        this.name = name;
        this.metaData = metaData;
        this.adminAddress = adminAddress;
       // this.numAccessListAddresses = numAccessListAddresses;
        this.numPublished = numPublished;
        this.minSupportCostWei = minSupportCostWei;
        this.adminPaymentPercentage = adminPaymentPercentage;
        this.uniqueSupporters = uniqueSupporters;
        this.subscribedLocally = subscribedLocally;
        this.adminClaimsOwed = adminClaimsOwed;
    }

    protected DBPublication(Parcel in) {
        publicationID = in.readInt();
        name = in.readString();
        metaData = in.readString();
        adminAddress = in.readString();
        numPublished = in.readInt();
        minSupportCostWei = in.readInt();
        adminPaymentPercentage = in.readInt();
        uniqueSupporters = in.readInt();
        subscribedLocally = in.readByte() != 0;
        adminClaimsOwed = in.readLong();
    }

    public static final Creator<DBPublication> CREATOR = new Creator<DBPublication>() {
        @Override
        public DBPublication createFromParcel(Parcel in) {
            return new DBPublication(in);
        }

        @Override
        public DBPublication[] newArray(int size) {
            return new DBPublication[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(publicationID);
        dest.writeString(name);
        dest.writeString(metaData);
        dest.writeString(adminAddress);
        dest.writeInt(numPublished);
        dest.writeInt(minSupportCostWei);
        dest.writeInt(adminPaymentPercentage);
        dest.writeInt(uniqueSupporters);
        dest.writeByte((byte) (subscribedLocally ? 1 : 0));
        dest.writeLong(adminClaimsOwed);
    }
}
