package com.tugasbesardua;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.tugasbesardua.models.UserData;

import java.util.Objects;

public class ProfileActivity extends AppCompatActivity {

    private final FirebaseDatabase database = FirebaseDatabase.getInstance();
    private final FirebaseAuth auth = FirebaseAuth.getInstance();

    TextView tvTitle;
    TextView tvName;
    TextView tvPhone;
    TextView tvEmail;
    TextView tvCity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        initView();
    }

    private void initView() {
        tvTitle = findViewById(R.id.tv_profile_title);
        tvName = findViewById(R.id.tv_profile_name);
        tvPhone = findViewById(R.id.tv_profile_phone);
        tvEmail = findViewById(R.id.tv_profile_email);
        tvCity = findViewById(R.id.tv_profile_city);

        String title = "My Profile";
        tvTitle.setText(title);
        retrieveFromDb();
    }

    private void retrieveFromDb() {
        String uid = Objects.requireNonNull(auth.getCurrentUser()).getUid();
        DatabaseReference ref = database.getReference("/user/" + uid);
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                UserData userData = snapshot.getValue(UserData.class);
                if (userData != null) {
                    tvName.setText(userData.getName());
                    tvPhone.setText(userData.getPhone());
                    tvEmail.setText(userData.getEmail());
                    tvCity.setText(userData.getCity());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}