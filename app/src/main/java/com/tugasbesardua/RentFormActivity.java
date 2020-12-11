package com.tugasbesardua;

import android.app.DatePickerDialog;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.tugasbesardua.models.RentalData;
import com.tugasbesardua.models.UserData;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class RentFormActivity extends AppCompatActivity {

    private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy", Locale.getDefault());
    private final FirebaseDatabase database = FirebaseDatabase.getInstance();
    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final Calendar calendar = Calendar.getInstance();

    private final String uid = auth.getCurrentUser().getUid();
    private UserData userData;

    TextView tvStartDate;
    TextView tvEndDate;
    TextView tvPrice;
    Spinner spCarBrand;
    Spinner spCarModel;

    Button btnNext;
    Date startDate;
    Date endDate;

    String carBrand;
    String carModel;
    Integer price;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rent_form);

        retrieveUserData();
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
                carBrand = adapterView.getSelectedItem().toString();

                switch (carBrand) {
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
                carModel = adapterView.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                Toast.makeText(RentFormActivity.this, "No model selected", Toast.LENGTH_SHORT).show();
            }
        });

        btnNext.setOnClickListener(v -> button());
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

    private void button() {
        if(validate()) {
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
            dialogBuilder.setMessage("Are you sure to continue?")
                    .setCancelable(false)
                    .setPositiveButton("Yes", (dialogInterface, i) -> sendToDb())
                    .setNegativeButton("No", (dialogInterface, i) -> dialogInterface.cancel())
                    .create().show();
        } else {
            Toast.makeText(RentFormActivity.this, "Check again", Toast.LENGTH_SHORT).show();
        }
    }

    private void sendToDb() {
        String name = userData.getName();
        String phone = userData.getPhone();
        String email = userData.getEmail();

        RentalData data = new RentalData(name, phone, email, carBrand, carModel, price * 1000, startDate.getTime(), endDate.getTime(), new Date().getTime(), "booked".toUpperCase());
        DatabaseReference rentRef = database.getReference("/car-rental/" + uid).push();
        rentRef.setValue(data).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                Toast.makeText(this, "Order success, you just need to pay at first", Toast.LENGTH_LONG).show();
                InvoiceActivity.launchInvoice(this, data);
            } else {
                Toast.makeText(this, "Please try again later", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void retrieveUserData() {
        DatabaseReference userRef = database.getReference("/user/" + uid);
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userData = snapshot.getValue(UserData.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private boolean validate() {
        boolean valid = true;
        if (startDate == null || endDate == null) valid = false;
        if (endDate != null && !endDate.after(startDate)) valid = false;
        if (carBrand == null) valid = false;
        if (carModel == null) valid = false;
        return valid;
    }
}