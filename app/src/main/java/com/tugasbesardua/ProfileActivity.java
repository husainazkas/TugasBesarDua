package com.tugasbesardua;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;
import com.tugasbesardua.models.UserData;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final FirebaseStorage storage = FirebaseStorage.getInstance();
    private final FirebaseDatabase database = FirebaseDatabase.getInstance();
    private final String uid = Objects.requireNonNull(auth.getCurrentUser()).getUid();


    private static final int CAMERA = 934;
    private static final int GALLERY = 8;

    TextView tvTitle;
    TextView tvName;
    TextView tvPhone;
    TextView tvEmail;
    TextView tvCity;
    CircleImageView civPhoto;

    Uri photoUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        initView();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null) {
            final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            Bitmap bitmap = null;

            if (requestCode == CAMERA) {
                File newFile = new File(getExternalFilesDir(Environment.DIRECTORY_DCIM).getPath());

                bitmap = (Bitmap) data.getExtras().get("data");
                bitmap.compress(Bitmap.CompressFormat.PNG, 60, bytes);
                try {
                    FileOutputStream fos = new FileOutputStream(newFile);
                    fos.write(bytes.toByteArray());
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                photoUri = Uri.fromFile(newFile);
            } else if (requestCode == GALLERY) {
                try {
                    photoUri = data.getData();
                    final InputStream imageStream = getContentResolver().openInputStream(photoUri);
                    bitmap = BitmapFactory.decodeStream(imageStream);
                    bitmap.compress(Bitmap.CompressFormat.PNG, 60, bytes);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }

            if (photoUri != null) updatePhoto(bitmap);
            else
                Toast.makeText(this, "Something error. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }

    private void initView() {
        tvTitle = findViewById(R.id.tv_profile_title);
        tvName = findViewById(R.id.tv_profile_name);
        tvPhone = findViewById(R.id.tv_profile_phone);
        tvEmail = findViewById(R.id.tv_profile_email);
        tvCity = findViewById(R.id.tv_profile_city);
        civPhoto = findViewById(R.id.civ_profile_photo);

        String title = "My Profile";
        tvTitle.setText(title);
        retrieveFromDb();
        civPhoto.setOnClickListener(view -> showDialog());
    }

    private void retrieveFromDb() {
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
                    if (userData.getPhotoUrl() != null) Picasso.get().load(userData.getPhotoUrl()).into(civPhoto);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void showDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_select_photo, null);

        AlertDialog dialog = dialogBuilder.setView(dialogView)
                .setTitle("Select source")
                .setCancelable(true)
                .create();
        dialog.show();

        ConstraintLayout camera = dialogView.findViewById(R.id.layout_dialog_camera);
        ConstraintLayout gallery = dialogView.findViewById(R.id.layout_dialog_gallery);

        camera.setOnClickListener(view -> {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            dialog.cancel();
            startActivityForResult(intent, CAMERA);
        });
        gallery.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            dialog.cancel();

            startActivityForResult(intent, GALLERY);
        });
    }

    private void updatePhoto(Bitmap bitmap) {
        StorageReference storageRef = storage.getReference("/photo-profile/" + uid);
        storageRef.putFile(photoUri).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                task.getResult().getStorage().getDownloadUrl().addOnSuccessListener(this, uri -> photoUri = uri);
            }
        });

        DatabaseReference dbRef = database.getReference("/user/" + uid);
        HashMap<String, Object> photo = new HashMap<>();
        photo.put("photoUrl", photoUri.toString());
        dbRef.updateChildren(photo).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                Toast.makeText(this, "Photo updated", Toast.LENGTH_SHORT).show();
                civPhoto.setImageBitmap(bitmap);
            } else {
                Toast.makeText(this, "Please try again later", Toast.LENGTH_SHORT).show();
            }
        });
    }
}