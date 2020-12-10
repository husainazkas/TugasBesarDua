package com.tugasbesardua;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class RentFormActivity extends AppCompatActivity {

    private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy",Locale.getDefault());
    private final Calendar calendar = Calendar.getInstance();

    TextView tvStartDate;
    TextView tvEndDate;
    TextView tvPrice;
    Spinner spCarBrand;
    Spinner spCarModel;

    Button btnNext;
    Date startDate;
    Date endDate;

    String carBrandSelected;
    String carModelSelected;
    Integer price;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rent_form);

        initView();
    }

    private void initView() {
        tvStartDate = findViewById(R.id.tv_rent_form_start_date);
        tvEndDate = findViewById(R.id.tv_rent_form_end_date);
        spCarBrand = findViewById(R.id.sp_rent_form_car_brand);
        spCarModel = findViewById(R.id.sp_rent_form_car_model);
        tvPrice = findViewById(R.id.tv_rent_form_price);
        btnNext = findViewById(R.id.btn_rent_form_next);

        tvStartDate.setOnClickListener(view -> pickStartDate());
        tvEndDate.setOnClickListener(view -> pickEndDate());

        spCarModel.setEnabled(false);

        spCarBrand.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Resources res = getResources();
                String[] items = {};
                carBrandSelected = adapterView.getSelectedItem().toString();

                switch (carBrandSelected) {
                    case "Daihatsu":
                        items = res.getStringArray(R.array.daihatsu_model);
                        break;
                    case "Honda":
                        items = res.getStringArray(R.array.honda_model);
                        break;
                    case "Mazda":
                        items = res.getStringArray(R.array.mazda_model);
                        break;
                    case "Suzuki":
                        items = res.getStringArray(R.array.suzuki_model);
                        break;
                    case "Toyota":
                        items = res.getStringArray(R.array.toyota_model);
                        break;
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<>(RentFormActivity.this, android.R.layout.simple_spinner_item, items);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spCarModel.setAdapter(adapter);
                spCarModel.setEnabled(true);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                Toast.makeText(RentFormActivity.this, "No car selected", Toast.LENGTH_SHORT).show();
                spCarModel.setEnabled(false);
            }
        });

        spCarModel.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                carModelSelected = adapterView.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                Toast.makeText(RentFormActivity.this, "No model selected", Toast.LENGTH_SHORT).show();
            }
        });

        btnNext.setOnClickListener(v -> {
            if(validate()) {
                Intent intent = new Intent(this, IdentityConfirmActivity.class);
                intent.putExtra("startDate", startDate.getTime());
                intent.putExtra("endDate", endDate.getTime());
                intent.putExtra("carBrand", carBrandSelected);
                intent.putExtra("carModel", carModelSelected);
                intent.putExtra("price", price);
                startActivity(intent);
            } else{
                Toast.makeText(RentFormActivity.this, "Check again", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void pickStartDate() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            final DatePickerDialog pickerDialog = new DatePickerDialog(this);
            pickerDialog.setOnDateSetListener((datePicker, year, month, dayOfMonth) -> {
                calendar.set(year, month, dayOfMonth);
                startDate = calendar.getTime();
                if(startDate.before(new Date())){
                    Toast.makeText(this, "Invalid start date", Toast.LENGTH_SHORT).show();
                }
                else{
                    tvStartDate.setText(sdf.format(startDate));
                    pickerDialog.dismiss();
                    setPrice();
                }
            });
            pickerDialog.show();
        }
    }

    private void pickEndDate() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            final DatePickerDialog pickerDialog = new DatePickerDialog(this);
            pickerDialog.setOnDateSetListener((datePicker, year, month, dayOfMonth) -> {
                calendar.set(year, month, dayOfMonth);
                endDate = calendar.getTime();
                if(!endDate.after(new Date())){
                    Toast.makeText(this, "Invalid end date", Toast.LENGTH_SHORT).show();
                } else if (TimeUnit
                        .MILLISECONDS
                        .toDays(endDate.getTime() - startDate.getTime()) % 365 > 15) {
                    Toast.makeText(this, "Maximum 15 days", Toast.LENGTH_SHORT).show();
                } else {
                    tvEndDate.setText(sdf.format(endDate));
                    pickerDialog.dismiss();
                    setPrice();
                }
            });
            pickerDialog.show();
        }
    }

    private void setPrice() {
        if (startDate != null && endDate != null) {
            int initPrice = 250;
            long diffInTime = endDate.getTime() - startDate.getTime();
            long inDays
                    = TimeUnit
                    .MILLISECONDS
                    .toDays(diffInTime)
                    % 365;

            if (inDays > 3) {
                if (inDays <= 6) {
                    price = initPrice * 2;
                } else if (inDays <= 9) {
                    price = initPrice * 3;
                } else if (inDays <= 12) {
                    price = initPrice * 4;
                } else if (inDays <= 15) {
                    price = initPrice * 5;
                }
            } else price = initPrice;

            String sumPrice = "Total\t:\t" + "Rp " + price * 1000;
            tvPrice.setText(sumPrice);
        }
    }

    private boolean validate() {
        boolean valid = true;
        if (startDate == null || endDate == null) valid = false;
        if (endDate != null && !endDate.after(startDate)) valid = false;
        if (carBrandSelected == null) valid = false;
        if (carModelSelected == null) valid = false;
        return valid;
    }
}

//class GFG {
//
//    // Function to print difference in
//    // time start_date and end_date
//    static void findDifference(String start_date, String end_date) {
//
//        // SimpleDateFormat converts the
//        // string format to date object
//        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault());
//
//        // Try Block
//        try {
//
//            // parse method is used to parse
//            // the text from a string to
//            // produce the date
//            Date d1 = sdf.parse(start_date);
//            Date d2 = sdf.parse(end_date);
//
//            // Calucalte time difference
//            // in milliseconds
//            long difference_In_Time
//                    = d2.getTime() - d1.getTime();
//
//            // Calucalte time difference in
//            // seconds, minutes, hours, years,
//            // and days
//            long difference_In_Seconds
//                    = (difference_In_Time
//                    / 1000)
//                    % 60;
//
//            long difference_In_Minutes
//                    = (difference_In_Time
//                    / (1000 * 60))
//                    % 60;
//
//            long difference_In_Hours
//                    = (difference_In_Time
//                    / (1000 * 60 * 60))
//                    % 24;
//
//            long difference_In_Years
//                    = (difference_In_Time
//                    / (1000L * 60 * 60 * 24 * 365));
//
//            long difference_In_Days
//                    = (difference_In_Time
//                    / (1000 * 60 * 60 * 24))
//                    % 365;
//            double a = Math.ceil(difference_In_Days);
//Log.d("days" , "ini" + a);
//            // Print the date difference in
//            // years, in days, in hours, in
//            // minutes, and in seconds
//
//            System.out.print("Difference " + "between two dates is: ");
//
//            System.out.println(
//                    difference_In_Years + " years, " +
//                            difference_In_Days + " days, " +
//                            difference_In_Hours + " hours, " +
//                            difference_In_Minutes + " minutes, " +
//                            difference_In_Seconds + " seconds");
//            Date d = new Date();
//            d.setTime(difference_In_Time);
//            System.out.println(sdf.format(d));
//
//        } catch (ParseException e) {
//            e.printStackTrace();
//        }
//    }
//
//    // Driver Code
//    public static void main(String[] args)
//    {
//        // Given start Date
//        String start_date
//                = "10-01-2018 15:10:20";
//
//        // Given end Date
//        String end_date
//                = "11-01-2020 15:10:20";
//
//        // Function Call
//        findDifference(start_date, end_date);
//    }
//}
