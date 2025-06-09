package com.projects.solace.login;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
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
import com.projects.solace.R;
import com.projects.solace.common.Constants;
import com.projects.solace.driver.DriverActivity;
import com.projects.solace.models.User;
import com.projects.solace.student.StudentActivity;

public class LoginActivity extends AppCompatActivity {
    private final Context context = LoginActivity.this;
    private EditText loginEmailEditText, loginPasswordEditText;
    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        loginEmailEditText = findViewById(R.id.loginEmailEditText);
        loginPasswordEditText = findViewById(R.id.loginPasswordEditText);
        listeners();

    }

    private void listeners() {
        findViewById(R.id.registerTextView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
                finish();
            }
        });
        findViewById(R.id.loginButton).setOnClickListener(v -> {
            ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("Logging in, please wait...");
            progressDialog.setCancelable(false); // Prevent dismissing by clicking outside
            progressDialog.show();
            String email = loginEmailEditText.getText().toString().trim();
            String password = loginPasswordEditText.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(LoginActivity.this, "Email and Password cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();

                            assert user != null;
                            doLogin(user.getUid());
                        } else {
                            loginPasswordEditText.setText("");
                            Toast.makeText(LoginActivity.this, "Login Failed", Toast.LENGTH_SHORT).show();
                        }
                        progressDialog.dismiss();
                    });
        });
    }

    private void doLogin(String userUid) {


        databaseReference.child(userUid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    User user = dataSnapshot.getValue(User.class);

                    if (user != null) {
                        String userType = user.getUserType();
                        String email = user.getEmail();
                        String name = user.getName();
                        Constants.email = email;
                        Constants.name = name;
                        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("email", email);
                        editor.putString("userType", userType);
                        editor.putString("name", name);
                        editor.putBoolean("isLoggedIn", true);
                        editor.apply();
                        // Access the user data
                        // Perform other operations with the retrieved user data
                        Toast.makeText(context, "Login Successful", Toast.LENGTH_SHORT).show();
                        if (Constants.getUserTypeFromString(userType) == Constants.UserType.STUDENT) {
                            Intent intent = new Intent(context, StudentActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            Intent intent = new Intent(context, DriverActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    }
                } else {
                    // The user data does not exist
                    Log.d("Firebase", "User data not found");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle any errors during the fetch operation
                Log.e("Firebase", "Error fetching user data", databaseError.toException());
            }
        });
    }
}
