package com.tugasbesardua;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.tugasbesardua.models.RentalData;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class InvoiceActivity extends AppCompatActivity {

    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final String uid = auth.getCurrentUser().getUid();
    private final FirebaseDatabase database = FirebaseDatabase.getInstance();
    private final DatabaseReference ref = database.getReference("/car-rental/" + uid);
    private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy", Locale.getDefault());
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, d MMM y", Locale.getDefault());
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());

    private static final int IMAGE_CODE = 823;

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
    TextView tvStatus;
    Button btnCancel;
    Button btnUploadPhoto;
    Button btnBackToHome;

    String name;
    String phone;
    String email;
    String carBrand;
    String carModel;
    String price;
    String startDateStrg;
    String endDateStrg;
    String dateBookStrg;
    String status;

    Boolean isRenting = false;

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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == IMAGE_CODE && data != null) {
                final Uri uri = data.getData();
                Intent intent = new Intent(this, ImagePreviewActivity.class);
                intent.putExtra("uri", uri.toString());
                intent.putExtra("dateBook", dateBook.getTime());
                startActivity(intent);
            }
        }
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
        tvStatus = findViewById(R.id.tv_invoice_status);
        btnCancel = findViewById(R.id.btn_invoice_cancel);
        btnUploadPhoto = findViewById(R.id.btn_invoice_upload_photo);
        btnBackToHome = findViewById(R.id.btn_invoice_back_home);

        Bundle extras = getIntent().getExtras();
        RentalData data = extras.getParcelable("dataRental");

        startDate.setTime(data.getStartDate());
        endDate.setTime(data.getEndDate());
        dateBook.setTime(data.getDateBook());

        name = "Name\t\t\t\t:\t" + data.getName();
        phone = "Phone\t\t\t:\t" + data.getPhone();
        email = "Email\t\t\t\t:\t" + data.getEmail();
        carBrand = "Selected Car\t\t:\t" + data.getCarBrand();
        carModel = "Car Type\t\t\t:\t" + data.getCarBrand();
        price = "Total\t\t\t\t:\t" + "Rp " + data.getPrice();
        startDateStrg = "Start from\t\t:\t" + sdf.format(startDate);
        endDateStrg = "End\t\t\t\t:\t" + sdf.format(endDate);
        dateBookStrg = dateFormat.format(dateBook) + "\n" + timeFormat.format(dateBook);
        status = "Status\t\t\t:\t" + data.getStatus();

        tvName.setText(name);
        tvPhone.setText(phone);
        tvEmail.setText(email);
        tvCarBrand.setText(carBrand);
        tvCarModel.setText(carModel);
        tvPrice.setText(price);
        tvStartDate.setText(startDateStrg);
        tvEndDate.setText(endDateStrg);
        tvDateBook.setText(dateBookStrg);
        tvStatus.setText(status);

        if (!data.getStatus().equals("BOOKED")) {
            btnUploadPhoto.setText("DONE");
            btnCancel.setEnabled(false);
            btnCancel.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#C31919")));
        }

        switch (data.getStatus()) {
            case "RENTING":
                isRenting = true;
                btnUploadPhoto.setEnabled(true);
                break;
            case "DONE":
            case "CANCELED":
                isRenting = false;
                btnUploadPhoto.setEnabled(false);
                break;
        }

        btnCancel.setOnClickListener(view -> {
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
            dialogBuilder.setMessage("Your book will canceled")
                    .setPositiveButton("Yes", (dialogInterface, i) -> updateStatus(this, ref, "canceled", dateBook))
                    .setNegativeButton("No", (dialogInterface, i) -> dialogInterface.cancel())
                    .create().show();
        });
        btnUploadPhoto.setOnClickListener(view -> {
            if (isRenting) {
                updateStatus(this, ref, "done", dateBook);
            } else {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);

                startActivityForResult(intent, IMAGE_CODE);
            }
        });
        btnBackToHome.setOnClickListener(view -> HomeActivity.launchIntentClearTask(this));
    }

    public void updateStatus(Context context, DatabaseReference rentRef, String newStatus, Date date) {
        Query query = rentRef.orderByChild("dateBook").equalTo(date.getTime());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                DatabaseReference newRef = snapshot.getRef();
                for (DataSnapshot data: snapshot.getChildren()) {
                    newRef = data.getRef();
                }
                HashMap<String, Object> status = new HashMap<>();
                status.put("status", newStatus.toUpperCase());
                newRef.updateChildren(status).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        HomeActivity.launchIntentClearTask(context);
                    } else {
                        Toast.makeText(context, "Something went wrong", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getApplicationContext(), "Unknown Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public static void launchInvoice(Context context, RentalData rentalData) {
        Intent intent = new Intent(context, InvoiceActivity.class).putExtra("dataRental", rentalData);
        context.startActivity(intent);
    }
}