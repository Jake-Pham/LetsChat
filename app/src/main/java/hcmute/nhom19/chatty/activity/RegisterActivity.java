package hcmute.nhom19.chatty.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import hcmute.nhom19.chatty.R;

public class RegisterActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference storeUserDefaultDataReference;

    private Toolbar mToolbar;
    private ProgressDialog loadingBar;

    private TextView textViewLogin;
    private EditText RegisterUserName, RegisterEmail, RegisterPassword, RegisterConfirmPwd;
    private Button RegisterButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();

        /*mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Sign Up");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);*/

        RegisterUserName = (EditText) findViewById(R.id.register_username);
        RegisterEmail = (EditText) findViewById(R.id.register_email);
        RegisterPassword = (EditText) findViewById(R.id.register_password);
        RegisterConfirmPwd = (EditText) findViewById(R.id.register_confirmpassword);
        RegisterButton = (Button) findViewById(R.id.btnRegister);
        loadingBar = new ProgressDialog(this);

        textViewLogin = findViewById(R.id.textViewLogin);
        textViewLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });

        RegisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = RegisterUserName.getText().toString();
                String email = RegisterEmail.getText().toString();
                String password = RegisterPassword.getText().toString();
                String confirmpwd = RegisterConfirmPwd.getText().toString();

                RegisterAccount(name, email, password, confirmpwd);
            }
        });
    }

    //Đăng ký tài khoản
    private void RegisterAccount(String name, String email, String password, String confirmpwd) {
        //Kiểm tra trường bỏ trống
        if(TextUtils.isEmpty(name) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password)){
            Toast.makeText(this, "Please Fill All Empty Fields!", Toast.LENGTH_SHORT).show();
        }
        //Kiểm tra độ dài mật khẩu
        if(password.length() < 6){
            Toast.makeText(this, "Password must Be At Least 6 Characters!", Toast.LENGTH_SHORT).show();
        }
        //Kiểm tra mật khẩu trùng nhau
        if(!password.equals(confirmpwd)){
            Toast.makeText(this, "Password Not Match!", Toast.LENGTH_SHORT).show();
        }else{
            loadingBar.setTitle("Creating New Account");
            loadingBar.setMessage("Please Wait, While We Creating Account!");
            loadingBar.show();
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            //Lưu dữ liệu người dùng đăng ký vào Firebase
                            if(task.isSuccessful()){
                                String current_userid  = mAuth.getCurrentUser().getUid();
                                storeUserDefaultDataReference = FirebaseDatabase.getInstance().getReference().child("Users").child(current_userid);
                                storeUserDefaultDataReference.child("user_name").setValue(name);
                                storeUserDefaultDataReference.child("user_email").setValue(email);
                                storeUserDefaultDataReference.child("user_password").setValue(password);
                                storeUserDefaultDataReference.child("user_status").setValue("Let's Chat");
                                storeUserDefaultDataReference.child("user_image").setValue("default_image")
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                //Đăng ký thành công
                                                if(task.isSuccessful()){
                                                    Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                    startActivity(intent);
                                                    finish();
                                                }
                                            }
                                        });
                            }else{
                                Toast.makeText(RegisterActivity.this, "Error Occured, Try Again", Toast.LENGTH_SHORT).show();
                            }
                            loadingBar.dismiss();
                        }
                    });
        }
    }
    /*//Kiểm tra Email tồn tại
    public void checkEmail(View v){
        mAuth.fetchSignInMethodsForEmail(RegisterEmail.getText().toString())
                .addOnCompleteListener(new OnCompleteListener<SignInMethodQueryResult>() {
                    @Override
                    public void onComplete(@NonNull Task<SignInMethodQueryResult> task) {
                        boolean check = !task.getResult().
                    }
                });
    }*/
}