package com.tugasbesardua.models;

import android.os.Parcel;
import android.os.Parcelable;

public class RentalData implements Parcelable {
    String uid, name, phone, email, carBrand, carModel;
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

    public RentalData() {}

    public RentalData(String uid, String name, String phone, String email, String carBrand, String carModel, Integer price, Long startDate, Long endDate, Long dateBook) {
        this.uid = uid;
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.carBrand = carBrand;
        this.carModel = carModel;
        this.price = price;
        this.startDate = startDate;
        this.endDate = endDate;
        this.dateBook = dateBook;
    }

    protected RentalData(Parcel in) {
        uid = in.readString();
        name = in.readString();
        phone = in.readString();
        email = in.readString();
        carBrand = in.readString();
        carModel = in.readString();
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

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(uid);
        dest.writeString(name);
        dest.writeString(phone);
        dest.writeString(email);
        dest.writeString(carBrand);
        dest.writeString(carModel);
        if (price == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(price);
        }
        if (startDate == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeLong(startDate);
        }
        if (endDate == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeLong(endDate);
        }
        if (dateBook == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeLong(dateBook);
        }
    }

    @Override
    public int describeContents() {
        return 0;
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
}
