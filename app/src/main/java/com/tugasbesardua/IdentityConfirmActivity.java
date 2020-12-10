package com.tugasbesardua;

import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.tugasbesardua.models.RentalData;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class IdentityConfirmActivity extends AppCompatActivity {

    private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy", Locale.getDefault());
    private final FirebaseDatabase database = FirebaseDatabase.getInstance();
    private final FirebaseAuth auth = FirebaseAuth.getInstance();

    final Date startDate = new Date();
    final Date endDate = new Date();

    EditText etName;
    EditText etPhone;
    EditText etEmail;
    Button btnConfirm;

    TextView tvCarBrand;
    TextView tvCarModel;
    TextView tvPrice;
    TextView tvStartDate;
    TextView tvEndDate;

    String carBrand;
    String carModel;
    Integer price;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_identity_confirm);

        initView();
    }

    private void initView() {
        etName = findViewById(R.id.et_identity_confirm_name);
        etPhone = findViewById(R.id.et_identity_confirm_phone_number);
        etEmail = findViewById(R.id.et_identity_confirm_email_address);
        btnConfirm = findViewById(R.id.btn_identity_confirm_confirm);
        tvCarBrand = findViewById(R.id.tv_identity_confirm_car_brand);
        tvCarModel = findViewById(R.id.tv_identity_confirm_car_model);
        tvPrice = findViewById(R.id.tv_identity_confirm_price);
        tvStartDate = findViewById(R.id.tv_identity_confirm_start_date);
        tvEndDate = findViewById(R.id.tv_identity_confirm_end_date);

        Bundle extras = getIntent().getExtras();
        carBrand = extras.getString("carBrand");
        carModel = extras.getString("carModel");
        startDate.setTime(extras.getLong("startDate"));
        endDate.setTime(extras.getLong("endDate"));
        price = extras.getInt("price");

        String brand = "Merk\t:\t" + carBrand;
        String model = "Model\t:\t" + carModel;
        String sumPrice = "Total\t:\t" + "Rp " + price * 1000;
        String start = "Start\t:\t" + sdf.format(startDate);
        String end = "End\t:\t" + sdf.format(endDate);

        tvCarBrand.setText(brand);
        tvCarModel.setText(model);
        tvPrice.setText(sumPrice);
        tvStartDate.setText(start);
        tvEndDate.setText(end);

        btnConfirm.setOnClickListener(view -> sendToDb());
    }

    private void sendToDb() {
        String name = etName.getText().toString();
        String phone = etPhone.getText().toString();
        String email = etEmail.getText().toString();
        String uid = Objects.requireNonNull(auth.getCurrentUser()).getUid();

        RentalData data = new RentalData(uid, name, phone, email, carBrand, carModel, price * 1000, startDate.getTime(), endDate.getTime(), new Date().getTime());
        if (validate(name, phone, email)) {
            DatabaseReference ref = database.getReference("/car-rental/" + uid).push();
            ref.setValue(data).addOnCompleteListener(this, task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(this, "Order success, you just need to pay at first", Toast.LENGTH_SHORT).show();
                    InvoiceActivity.launchInvoice(this, data);
                } else {
                    Toast.makeText(this, "Please try again later", Toast.LENGTH_SHORT).show();
                }
            });
        }

    }

    private boolean validate(String name, String phone, String email) {
        boolean valid = true;

        if(name.isEmpty()){
            etName.setError("Please enter your name");
            etName.requestFocus();
            valid = false;
        } else etName.setError(null);

        if(email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            etEmail.setError("Invalid Email");
            etEmail.requestFocus();
            valid = false;
        } else etEmail.setError(null);

        if(!Patterns.PHONE.matcher(phone).matches()){
            etPhone.setError("Invalid Phone Number");
            etPhone.requestFocus();
            valid = false;
        } else etPhone.setError(null);

        return valid;
    }
}