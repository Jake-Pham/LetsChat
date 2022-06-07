package hcmute.nhom19.chatty.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import androidx.appcompat.app.AppCompatActivity;



import android.app.ProgressDialog;
import android.content.Intent;

import android.net.Uri;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;
import hcmute.nhom19.chatty.R;

public class SettingsActivity extends AppCompatActivity {

    private CircleImageView settingsDisplayProfileImage;
    private EditText settingsDisplayName, settingsDisplayStatus, setttingsDisplayEmail, settingsDisplayPassword;
    private Button btnUpdate;
    private String online_userid;

    private DatabaseReference getUserDataReference;
    private FirebaseAuth mAuth;
    private StorageReference storeProfileImageStoreRef;
    private FirebaseDatabase database;
    //Khơi tạo biến xác định mở thư viện trong máy
    private static final int library = 1;
    private FirebaseStorage storageReference;
    //Khởi tạo biến thanh chờ
    private ProgressDialog dialog;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mAuth = FirebaseAuth.getInstance();
        //Get user ID
        online_userid = mAuth.getCurrentUser().getUid();
        //Reference to Users
        getUserDataReference = FirebaseDatabase.getInstance().getReference().child("Users").child(online_userid);
        // get the Firebase  storage reference
        storeProfileImageStoreRef = FirebaseStorage.getInstance().getReference().child("Image_Users");
        storageReference = FirebaseStorage.getInstance();
        database = FirebaseDatabase.getInstance();

        Init();
        RetrieveUserInfo();
        //Open Gallary
        settingsDisplayProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, library);
            }
        });
        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateInfo();
            }
        });
    }

    private void Init(){
        settingsDisplayProfileImage = findViewById(R.id.settings_profile_image);
        settingsDisplayName =  findViewById(R.id.settings_username);
        settingsDisplayStatus = findViewById(R.id.settings_user_status);
        setttingsDisplayEmail = findViewById(R.id.settings_user_email);
        setttingsDisplayEmail.setEnabled(false);
        settingsDisplayPassword = findViewById(R.id.settings_user_password);
        btnUpdate = findViewById(R.id.button_Update);

        dialog = new ProgressDialog(this);

    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == library && resultCode == RESULT_OK && data != null) {
            dialog.setTitle("Uploading Profile Image");
            dialog.setMessage("Please Wait While We Upload Your Profile Image");
            dialog.setCanceledOnTouchOutside(true);
            dialog.show();

            Uri imageUri = data.getData();
            settingsDisplayProfileImage.setImageURI(imageUri);

            StorageReference filePath = storeProfileImageStoreRef.child(online_userid + ".jpg");

            filePath.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if(task.isSuccessful()) {
                        Toast.makeText(SettingsActivity.this, "Upload Success", Toast.LENGTH_SHORT).show();

                        filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                database.getReference().child("Users").child(online_userid).child("user_image").setValue(uri.toString());
                                dialog.dismiss();
                            }
                        });

                    } else {
                        Toast.makeText(SettingsActivity.this, task.getException().toString(), Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    }
                }
            });

        }
    }
    //Lấy thông tin người dùng
    private void RetrieveUserInfo(){
        //Get user Data from Firebase
        getUserDataReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //Get Data from Firebase and store in varibles(name, status, image,...)
                String name = snapshot.child("user_name").getValue().toString();
                String status = snapshot.child("user_status").getValue().toString();
                String image = snapshot.child("user_image").getValue().toString();
                String email = snapshot.child("user_email").getValue().toString();
                String password = snapshot.child("user_password").getValue().toString();

                settingsDisplayName.setText(name);
                settingsDisplayStatus.setText(status);
                setttingsDisplayEmail.setText(email);
                settingsDisplayPassword.setText(password);
                Picasso.get().load(image).placeholder(R.drawable.default_image).into(settingsDisplayProfileImage);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    //Cập nhật thông tin người dùng
    private void updateInfo(){
        String username = settingsDisplayName.getText().toString();
        String status = settingsDisplayStatus.getText().toString();
        String password = settingsDisplayPassword.getText().toString();

        if(TextUtils.isEmpty(username) || TextUtils.isEmpty(status) || TextUtils.isEmpty(password)){
            Toast.makeText(this, "All Fileds Are Required", Toast.LENGTH_SHORT).show();
        }else {
            HashMap<String, Object> user = new HashMap<>();
            user.put("user_id", online_userid);
            user.put("user_status", status);
            user.put("user_name", username);
            user.put("user_password", password);
            getUserDataReference.updateChildren(user)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()) {
                        Toast.makeText(SettingsActivity.this, "Update Successfully!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(SettingsActivity.this, task.getException().toString(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }
}