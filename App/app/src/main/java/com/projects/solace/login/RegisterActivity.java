package com.projects.solace.login;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.projects.solace.R;
import com.projects.solace.common.IotApiService;
import com.projects.solace.common.RetrofitClient;
import com.projects.solace.models.ResponseModel;
import com.projects.solace.models.User;

import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {
    private final Context context = RegisterActivity.this;
    ProgressDialog progressDialog;
    private EditText etEmail, etPassword, etName;
    private RadioGroup rgUserType;
    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        // For Realtime Database
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    User user = snapshot.getValue(User.class);
                    Log.d("User Info", "Email: " + user.email + ", UserType: " + user.userType);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w("DatabaseError", "loadPost:onCancelled", databaseError.toException());
            }
        });

        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        rgUserType = findViewById(R.id.rgUserType);

        findViewById(R.id.registerButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = etName.getText().toString().trim();
                String email = etEmail.getText().toString().trim();
                String password = etPassword.getText().toString().trim();
                int selectedUserTypeId = rgUserType.getCheckedRadioButtonId();
                String userType = selectedUserTypeId == R.id.rbDriver ? "Driver" : "Student";

                if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(RegisterActivity.this, "Name/Email/Password cannot be empty", Toast.LENGTH_SHORT).show();
                    return;
                }
                progressDialog = new ProgressDialog(context);
                progressDialog.setMessage("Registering, please wait...");
                progressDialog.setCancelable(false); // Prevent dismissing by clicking outside
                progressDialog.show();
                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                FirebaseUser user = mAuth.getCurrentUser();


                                // Get FCM token
                                FirebaseMessaging.getInstance().getToken()
                                        .addOnCompleteListener(tokenTask -> {
                                            if (tokenTask.isSuccessful()) {
                                                String token = tokenTask.getResult();
                                                assert user != null;
                                                saveUserToDatabase(user.getUid(), name, email, userType, token);
                                            } else {
                                                Toast.makeText(context, "Failed to get FCM token", Toast.LENGTH_SHORT).show();
                                            }
                                        });

                            } else {
                                Toast.makeText(RegisterActivity.this, "Registration Failed", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
    }

    private void navigateToLoginActivity() {
        Intent intent;
        intent = new Intent(this, LoginActivity.class);

        startActivity(intent);
        finish();
    }

    private void saveUserToDatabase(String userId, String name, String email, String userType, String token) {
        User newUser = new User(name, email, userType, token);
        databaseReference.child(userId).setValue(newUser)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        sendDataToServer(name, email, userType, token);

                    } else {
                        Toast.makeText(this, "Failed to save user data", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void sendDataToServer(String name, String email, String userType, String token) {

        IotApiService apiService = RetrofitClient.getIotApiService();

        HashMap<String, String> data = new HashMap<>();
        data.put("name", name);
        data.put("email", email);
        data.put("user_type", userType);
        data.put("token", token);

        Call<ResponseModel> call = apiService.saveFcmToken(data);
        call.enqueue(new Callback<ResponseModel>() {
            @Override
            public void onResponse(Call<ResponseModel> call, Response<ResponseModel> response) {
                if (response.isSuccessful()) {
                    Log.d("API", "Token saved successfully!");
                    progressDialog.cancel();
                    Toast.makeText(context, "Registration Successful", Toast.LENGTH_SHORT).show();

                    navigateToLoginActivity();
                } else {
                    Log.e("API", "Failed to save token.");
                }
            }

            @Override
            public void onFailure(Call<ResponseModel> call, Throwable t) {
                Log.e("API", "Error: " + t.getMessage());
            }
        });

    }
}
