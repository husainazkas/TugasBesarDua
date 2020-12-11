package com.tugasbesardua.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

public class RentalData implements Parcelable {
    String name, phone, email, carBrand, carModel, status;
    Integer price;
    Long startDate, endDate, dateBook;

    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }

    public String getEmail() {
        return email;
    }

    public String getCarBrand() {
        return carBrand;
    }

    public String getCarModel() {
        return carModel;
    }

    public Integer getPrice() {
        return price;
    }

    public Long getStartDate() {
        return startDate;
    }

    public Long getEndDate() {
        return endDate;
    }

    public Long getDateBook() {
        return dateBook;
    }

    public String getStatus() {
        return status;
    }

    public RentalData() {}

    public RentalData(String name, String phone, String email, String carBrand, String carModel, Integer price, Long startDate, Long endDate, Long dateBook, String status) {
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.carBrand = carBrand;
        this.carModel = carModel;
        this.price = price;
        this.startDate = startDate;
        this.endDate = endDate;
        this.dateBook = dateBook;
        this.status = status;
    }

    protected RentalData(Parcel in) {
        name = in.readString();
        phone = in.readString();
        email = in.readString();
        carBrand = in.readString();
        carModel = in.readString();
        status = in.readString();
        if (in.readByte() == 0) {
            price = null;
        } else {
            price = in.readInt();
        }
        if (in.readByte() == 0) {
            startDate = null;
        } else {
            startDate = in.readLong();
        }
        if (in.readByte() == 0) {
            endDate = null;
        } else {
            endDate = in.readLong();
        }
        if (in.readByte() == 0) {
            dateBook = null;
        } else {
            dateBook = in.readLong();
        }
    }

    public static final Creator<RentalData> CREATOR = new Creator<RentalData>() {
        @Override
        public RentalData createFromParcel(Parcel in) {
            return new RentalData(in);
        }

        @Override
        public RentalData[] newArray(int size) {
            return new RentalData[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(name);
        parcel.writeString(phone);
        parcel.writeString(email);
        parcel.writeString(carBrand);
        parcel.writeString(carModel);
        parcel.writeString(status);
        if (price == null) {
            parcel.writeByte((byte) 0);
        } else {
            parcel.writeByte((byte) 1);
            parcel.writeInt(price);
        }
        if (startDate == null) {
            parcel.writeByte((byte) 0);
        } else {
            parcel.writeByte((byte) 1);
            parcel.writeLong(startDate);
        }
        if (endDate == null) {
            parcel.writeByte((byte) 0);
        } else {
            parcel.writeByte((byte) 1);
            parcel.writeLong(endDate);
        }
        if (dateBook == null) {
            parcel.writeByte((byte) 0);
        } else {
            parcel.writeByte((byte) 1);
            parcel.writeLong(dateBook);
        }
    }
}
