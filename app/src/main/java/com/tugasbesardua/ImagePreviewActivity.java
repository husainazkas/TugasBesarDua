package com.tugasbesardua;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Date;

public class ImagePreviewActivity extends AppCompatActivity {

    private final FirebaseDatabase database = FirebaseDatabase.getInstance();
    private final FirebaseStorage storage = FirebaseStorage.getInstance();
    private final FirebaseAuth auth = FirebaseAuth.getInstance();

    private final Date dateBook = new Date();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_preview);

        initView();
    }

    private void initView() {
        ImageView ivImage = findViewById(R.id.iv_image_preview_image);
        FloatingActionButton fabCancel = findViewById(R.id.fab_image_preview_cancel);
        FloatingActionButton fabAccept = findViewById(R.id.fab_image_preview_accept);

        Uri uri = Uri.parse(getIntent().getStringExtra("uri"));
        dateBook.setTime(getIntent().getLongExtra("dateBook", 0));
        setImage(ivImage, uri);

        fabCancel.setOnClickListener(view -> {
            startActivity(new Intent(this, InvoiceActivity.class));
            finish();
        });
        fabAccept.setOnClickListener(view -> uploadImage(uri));
    }

    private void setImage(ImageView ivImage, Uri uri) {
        final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try {
            final InputStream imageStream = getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(imageStream);
            bitmap.compress(Bitmap.CompressFormat.PNG, 60, bytes);
            ivImage.setImageBitmap(bitmap);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void uploadImage(Uri imageUri) {
        String uid = auth.getCurrentUser().getUid();
        DatabaseReference dbRef = database.getReference("/car-rental/" + uid);
        StorageReference storageRef = storage.getReference("/bookings/" + uid);
        storageRef.putFile(imageUri).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                task.getResult()
                        .getStorage()
                        .getDownloadUrl()
                        .addOnSuccessListener(this, uri -> new InvoiceActivity().updateStatus(this, dbRef, "renting", dateBook));
            }
        });

    }
}