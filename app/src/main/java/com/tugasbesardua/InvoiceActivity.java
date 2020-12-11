package com.tugasbesardua;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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


    private static final String verificationCode = "kelompok5";

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
    Button btnEnterCode;
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
        btnEnterCode = findViewById(R.id.btn_invoice_enter_code);
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

        if (data.getStatus().equals("RENTING")) {
            isRenting = true;
            btnEnterCode.setText("DONE");
            btnCancel.setEnabled(false);
            btnCancel.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#C31919")));
        } else if (data.getStatus().equals("DONE")) {
            isRenting = false;
            btnEnterCode.setText("DONE");
            btnEnterCode.setEnabled(false);
            btnCancel.setEnabled(false);
            btnCancel.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#C31919")));
        }

        btnCancel.setOnClickListener(view -> {
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
            dialogBuilder.setMessage("Your book will canceled")
                    .setPositiveButton("Yes", (dialogInterface, i) -> cancelBook(ref))
                    .setNegativeButton("No", (dialogInterface, i) -> dialogInterface.cancel())
                    .create().show();
        });
        btnEnterCode.setOnClickListener(view -> {
            if (isRenting) {
                done();
            } else {
                enterCode();
            }
        });
        btnBackToHome.setOnClickListener(view -> HomeActivity.launchIntentClearTask(this));
    }

    private void cancelBook(DatabaseReference rentRef) {
        Query query = rentRef.orderByChild("dateBook").equalTo(dateBook.getTime());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot child: snapshot.getChildren()) {
                    child.getRef().removeValue((error, ref) -> {
                        Toast.makeText(InvoiceActivity.this, "This book has canceled", Toast.LENGTH_SHORT).show();
                        HomeActivity.launchIntentClearTask(InvoiceActivity.this);
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(InvoiceActivity.this, "Unknown Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void done() {
        updateStatus(ref, "done");
    }

    private void enterCode() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_enter_code, null);

        dialogBuilder
                .setView(dialogView)
                .setTitle("Enter verification code")
                .setPositiveButton("Yes", (dialogInterface, i) -> {
                    EditText etCode = dialogView.findViewById(R.id.et_dialog_enter_code);
                    String code = etCode.getText().toString();
                    if (code.equals(verificationCode)){
                        updateStatus(ref, "renting");
                    } else {
                        Toast.makeText(this, "Invalid code", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("No", (dialogInterface, i) -> dialogInterface.cancel())
                .create().show();
    }

    private void updateStatus(DatabaseReference rentRef, String newStatus) {
        Query query = rentRef.orderByChild("dateBook").equalTo(dateBook.getTime());
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
                        HomeActivity.launchIntentClearTask(InvoiceActivity.this);
                    } else {
                        Toast.makeText(InvoiceActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(InvoiceActivity.this, "Unknown Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public static void launchInvoice(Context context, RentalData rentalData) {
        Intent intent = new Intent(context, InvoiceActivity.class).putExtra("dataRental", rentalData);
        context.startActivity(intent);
    }
}