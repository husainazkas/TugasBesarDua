package com.tugasbesardua.adapter;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.tugasbesardua.R;
import com.tugasbesardua.models.RentalData;
import com.xwray.groupie.Item;
import com.xwray.groupie.ViewHolder;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class RentalListAdapter extends Item<ViewHolder> {
    private final SimpleDateFormat bookFormat = new SimpleDateFormat("EEEE, d MMM y", Locale.getDefault());
    public final RentalData data;

    public RentalListAdapter(RentalData rentalData) {
        data = rentalData;
    }

    @Override
    public void bind(@NonNull ViewHolder viewHolder, int position) {
        View itemView = viewHolder.itemView;
        TextView tvCarBrand = itemView.findViewById(R.id.tv_home_car_brand);
        TextView tvCarModel = itemView.findViewById(R.id.tv_home_car_model);
        TextView tvDateBook = itemView.findViewById(R.id.tv_home_date_book);

        tvCarBrand.setText(data.getCarBrand());
        tvCarModel.setText(data.getCarModel());
        tvDateBook.setText(bookFormat.format(data.getDateBook()));
    }

    @Override
    public int getLayout() {
        return R.layout.item_rent_list;
    }
}
