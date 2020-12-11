package com.tugasbesardua;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.tugasbesardua.adapter.RentalListAdapter;
import com.tugasbesardua.models.RentalData;
import com.xwray.groupie.GroupAdapter;
import com.xwray.groupie.ViewHolder;

import java.util.HashMap;
import java.util.Objects;

public class HomeActivity extends AppCompatActivity {

    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final FirebaseDatabase database = FirebaseDatabase.getInstance();

    GroupAdapter<ViewHolder> adapter = new GroupAdapter<>();
    HashMap<String, RentalData> hashMap = new HashMap<>();
    RecyclerView rvList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        initView();
    }

    private void initView() {
        rvList = findViewById(R.id.rv_home_listing);
        FloatingActionButton fabAdd = findViewById(R.id.fab_home_add);
        rentList();

        fabAdd.setOnClickListener(view -> startActivity(new Intent(this, RentFormActivity.class)));
    }

    private void rentList() {
        String uid = Objects.requireNonNull(auth.getCurrentUser()).getUid();

        DatabaseReference ref = database.getReference("/car-rental/" + uid);
        ref.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Toast.makeText(HomeActivity.this, "NEW RECORD ADDED", Toast.LENGTH_SHORT).show();

                RentalData data = snapshot.getValue(RentalData.class);
                hashMap.put(snapshot.getKey(), data);
                refreshList();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Toast.makeText(HomeActivity.this, "NEW RECORD CHANGED", Toast.LENGTH_SHORT).show();

                RentalData data = snapshot.getValue(RentalData.class);
                hashMap.put(snapshot.getKey(), data);
                refreshList();
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                Toast.makeText(HomeActivity.this, "NEW RECORD REMOVED", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Toast.makeText(HomeActivity.this, "NEW RECORD MOVED", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(HomeActivity.this, "SOMETHING ERROR: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        adapter.setOnItemClickListener((item, view) -> {
            RentalListAdapter data = (RentalListAdapter) item;
            InvoiceActivity.launchInvoice(view.getContext(), data.data);
        });
        rvList.setAdapter(adapter);
    }

    private void refreshList() {
        adapter.clear();
        for (RentalData data: hashMap.values()) {
            adapter.add(new RentalListAdapter(data));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.home_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch(item.getItemId()){
            case R.id.menu_home_profile:
                startActivity(new Intent(this, ProfileActivity.class));
                break;
            case R.id.menu_home_logout:
                logout();
                break;
            default:
                return false;
        }
        return true;
    }

    private void logout() {
        try {
            auth.signOut();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } catch (Exception e){
            Toast.makeText(this, "Error: " + e.toString(), Toast.LENGTH_SHORT).show();
        }
    }

    public static void launchIntentClearTask(Context context) {
        Intent intent = new Intent(context, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
    }
}
