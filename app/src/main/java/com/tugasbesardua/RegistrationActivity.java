package com.tugasbesardua;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.tugasbesardua.models.UserData;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.graphics.ImageDecoder.decodeBitmap;

public class RegistrationActivity extends AppCompatActivity {

    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final FirebaseDatabase database = FirebaseDatabase.getInstance();
    private final FirebaseStorage storage = FirebaseStorage.getInstance();

    private static final int GALLERY = 505;
    private static final int CAMERA = 111;

//    private ImageView ivPhoto;
    private CircleImageView civPhoto;
    private EditText etName;
    private EditText etEmail;
    private EditText etPassword;
    private EditText etPhone;
    private EditText etCity;

    private Uri photoUri;
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

            if (photoUri != null) civPhoto.setImageBitmap(bitmap);
            else
                Toast.makeText(this, "Something error. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }

    private void initView() {
        civPhoto = findViewById(R.id.civ_registration_photo);
//        ivPhoto = findViewById(R.id.iv_registration_photo);
        etName = findViewById(R.id.et_registration_name);
        etEmail = findViewById(R.id.et_registration_email);
        etPassword = findViewById(R.id.et_registration_password);
        etPhone = findViewById(R.id.et_registration_phone);
        etCity = findViewById(R.id.et_registration_city);

        civPhoto.setOnClickListener(view -> showDialog());

        Button btnRegister = findViewById(R.id.btn_registration_register);
        btnRegister.setOnClickListener(view -> register());
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
                    uploadPhoto(Objects.requireNonNull(authResult.getResult().getUser()).getUid());
                } else {
                    Toast.makeText(this, "Failed create an account", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void uploadPhoto(String uid) {
        if (photoUri != null) {
            StorageReference ref = storage.getReference("/photo-profile/" + uid);
            ref.putFile(photoUri).addOnCompleteListener(this, task -> {
                if (task.isSuccessful()) {
                    task.getResult().getStorage().getDownloadUrl().addOnSuccessListener(this, uri -> {
                        Toast.makeText(this, "URL:" + uri.toString(), Toast.LENGTH_LONG).show();
                        photoUri = uri;
                        uploadUserData(uid, photoUri.toString());
                    });
                }
            });
        } else uploadUserData(uid, null);
    }

    private void uploadUserData(String uid, String url) {
        DatabaseReference ref = database.getReference("/user/" + uid);
        UserData userData = new UserData(name, email, phone, city, url);
        ref.setValue(userData).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                Toast.makeText(this, "Welcome to " + getString(R.string.app_name), Toast.LENGTH_SHORT).show();
                HomeActivity.launchIntentClearTask(this);
            } else {
                Toast.makeText(this, "Please try again later", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private Uri uriFromBitmap(Bitmap bitmap) {
        File newFile = new File(getExternalFilesDir(Environment.DIRECTORY_DCIM).getPath());

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 60, bytes);
        try {
            FileOutputStream fos = new FileOutputStream(newFile);
            fos.write(bytes.toByteArray());
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return Uri.fromFile(newFile);
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
