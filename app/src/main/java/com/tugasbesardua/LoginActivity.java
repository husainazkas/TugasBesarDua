package com.tugasbesardua;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity {

    private final FirebaseAuth auth = FirebaseAuth.getInstance();

    private EditText etEmail;
    private EditText etPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initView();
    }

    private void initView() {
        etEmail = findViewById(R.id.et_login_email);
        etPassword = findViewById(R.id.et_login_password);

        Button btnLogin = findViewById(R.id.btn_login_sign_in);
        Button btnRegister = findViewById(R.id.btn_login_sign_up);

        btnLogin.setOnClickListener(view -> login());
        btnRegister.setOnClickListener(view -> startActivity(new Intent(this, RegistrationActivity.class)));
    }

    private void login() {
        String email = etEmail.getText().toString().trim();
        String pass = etPassword.getText().toString().trim();
        if (validate(email, pass)) {
            auth.signInWithEmailAndPassword(email,pass).addOnCompleteListener(this,(authResult) -> {
                if (authResult.isSuccessful()) {
                    String userEmail = Objects.requireNonNull(authResult.getResult().getUser()).getEmail();
                    Toast.makeText(this, "Welcome back " + userEmail, Toast.LENGTH_SHORT).show();

                    HomeActivity.launchIntentClearTask(this);
                } else {
                    Toast.makeText(this, "Invalid password", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private boolean validate(String email, String pass){
        boolean valid = true;

        if(email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            etEmail.setError("Invalid email.");
            etEmail.requestFocus();
            valid = false;
        } else etPassword.setError(null);

        if(pass.isEmpty()){
            etPassword.setError("Enter your password here.");
            etPassword.requestFocus();
            valid = false;
        }
        return valid;
    }
}
