package com.tugasbesardua;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.tugasbesardua.models.RentalData;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class InvoiceActivity extends AppCompatActivity {

    private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy", Locale.getDefault());
    private final SimpleDateFormat bookFormat = new SimpleDateFormat("EEEE, d MMM y HH:mm:ss", Locale.getDefault());

    final Date startDate = new Date();
    final Date endDate = new Date();
    final Date dateBook = new Date();

    TextView tvName;
    TextView tvPhone;
    TextView tvEmail;
    TextView tvCarBrand;
    TextView tvCarModel;
    TextView tvPrice;
    TextView tvStartDate;
    TextView tvEndDate;
    TextView tvDateBook;

    String name;
    String phone;
    String email;
    String carBrand;
    String carModel;
    String price;
    String startDateStrg;
    String endDateStrg;
    String dateBookStrg;
    Button btnBackToHome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invoice);

        initView();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        HomeActivity.launchIntentClearTask(this);
    }

    private void initView() {
        tvName = findViewById(R.id.tv_invoice_name);
        tvPhone = findViewById(R.id.tv_invoice_phone);
        tvEmail = findViewById(R.id.tv_invoice_email);
        tvCarBrand = findViewById(R.id.tv_invoice_car_brand);
        tvCarModel = findViewById(R.id.tv_invoice_car_model);
        tvPrice = findViewById(R.id.tv_invoice_price);
        tvStartDate = findViewById(R.id.tv_invoice_start_date);
        tvEndDate = findViewById(R.id.tv_invoice_end_date);
        tvDateBook = findViewById(R.id.tv_invoice_date_book);
        btnBackToHome = findViewById(R.id.btn_invoice_back_home);

        Bundle extras = getIntent().getExtras();
        RentalData data = extras.getParcelable("dataRental");

        startDate.setTime(data.getStartDate());
        endDate.setTime(data.getEndDate());
        dateBook.setTime(data.getDateBook());

        name = "Name\t\t\t:\t" + data.getName();
        phone = "Phone\t\t:\t" + data.getPhone();
        email = "Email\t\t\t:\t" + data.getEmail();
        carBrand = "Selected Car\t:\t" + data.getCarBrand();
        carModel = "Car Type\t\t:\t" + data.getCarBrand();
        price = "Total\t\t\t:\t" + "Rp " + data.getPrice();
        startDateStrg = "Start from\t:\t" + sdf.format(startDate);
        endDateStrg = "End\t\t\t:\t" + sdf.format(endDate);
        dateBookStrg = bookFormat.format(dateBook);

        tvName.setText(name);
        tvPhone.setText(phone);
        tvEmail.setText(email);
        tvCarBrand.setText(carBrand);
        tvCarModel.setText(carModel);
        tvPrice.setText(price);
        tvStartDate.setText(startDateStrg);
        tvEndDate.setText(endDateStrg);
        tvDateBook.setText(dateBookStrg);

        btnBackToHome.setOnClickListener(view -> HomeActivity.launchIntentClearTask(this));
    }

    public static void launchInvoice(Context context, RentalData rentalData) {
        Intent intent = new Intent(context, InvoiceActivity.class).putExtra("dataRental", rentalData);
        context.startActivity(intent);
    }
}