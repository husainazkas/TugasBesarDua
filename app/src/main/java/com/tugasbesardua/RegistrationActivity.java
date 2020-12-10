package com.tugasbesardua;

import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.tugasbesardua.models.UserData;

import java.util.Objects;

public class RegistrationActivity extends AppCompatActivity {

    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final FirebaseDatabase database = FirebaseDatabase.getInstance();

    private EditText etName;
    private EditText etEmail;
    private EditText etPassword;
    private EditText etPhone;
    private EditText etCity;

    private String name;
    private String email;
    private String phone;
    private String city;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        initView();
    }

    private void initView() {
        etName = findViewById(R.id.et_registration_name);
        etEmail = findViewById(R.id.et_registration_email);
        etPassword = findViewById(R.id.et_registration_password);
        etPhone = findViewById(R.id.et_registration_phone);
        etCity = findViewById(R.id.et_registration_city);

        Button btnRegister = findViewById(R.id.btn_registration_register);
        btnRegister.setOnClickListener(view -> register());
    }

    private void register() {
        name = etName.getText().toString();
        email = etEmail.getText().toString();
        String pass = etPassword.getText().toString();
        phone = etPhone.getText().toString();
        city = etCity.getText().toString();

        if (validate(name, email, pass, phone, city)) {
            auth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener(this, (authResult)-> {
                if (authResult.isSuccessful()) {
                    Toast.makeText(this, "Please wait ...", Toast.LENGTH_SHORT).show();
                    uploadUserData(Objects.requireNonNull(authResult.getResult().getUser()).getUid());
                } else {
                    Toast.makeText(this, "Failed create an account", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void uploadUserData(String uid) {
        DatabaseReference ref = database.getReference("/user/" + uid);
        UserData userData = new UserData(name, email, phone, city);
        ref.setValue(userData).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                Toast.makeText(this, "Welcome to " + getString(R.string.app_name), Toast.LENGTH_SHORT).show();
                HomeActivity.launchIntentClearTask(this);
            } else {
                Toast.makeText(this, "Please try again later", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean validate(String name, String email, String pass, String phone, String city) {
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

        if(pass.length() > 10 || pass.length() < 4){
            etPassword.setError("Password should be 4 to 10 characters long");
            etPassword.requestFocus();
            valid = false;
        } else etPassword.setError(null);

        if(!Patterns.PHONE.matcher(phone).matches()){
            etPhone.setError("Invalid Phone Number");
            etPhone.requestFocus();
            valid = false;
        } else etPhone.setError(null);

        if(city.isEmpty()){
            etCity.setError("Please enter your city");
            etCity.requestFocus();
            valid = false;
        } else etCity.setError(null);

        return valid;
    }
}
