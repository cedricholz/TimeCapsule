package com.example.cedric.timecapsule.Login;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.cedric.timecapsule.Maps.MapsActivity;
import com.example.cedric.timecapsule.R;
import com.example.cedric.timecapsule.UserInformation.User;
import com.example.cedric.timecapsule.Utils.Utils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LoginActivity extends AppCompatActivity {

    // Firebase
    FirebaseDatabase database;
    DatabaseReference users;

    // User Input
    EditText password, email;

    // Button
    Button cancelBtn, logInBtn;


    Utils u;

    public static String encodeString(String string) {
        return string.replace(".", ",");
    }

    public static boolean isEmailValid(String email) {
        String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);

        // setting up firebase
        database = FirebaseDatabase.getInstance();
        users = database.getReference("users");

        // setting up user input
        password = (EditText) findViewById(R.id.SignUpPassword);
        email = (EditText) findViewById(R.id.SignUpEmail);

        // setting up buttons
        cancelBtn = (Button) findViewById(R.id.btnCancel);
        logInBtn = (Button) findViewById(R.id.btnLogIn);

        // Check whether this view was called from SignUpActivity
        // If yes, then fill in email address and password for newly created user
        Intent fromSignUp = getIntent();
        Bundle intentExtras = fromSignUp.getExtras();

        if (intentExtras != null) {
            email.setText((String) intentExtras.get("email"));
            password.setText((String) intentExtras.get("password"));
        }

        // user clicked cancel button (goes back to StartActivity with no input)
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(LoginActivity.this, StartActivity.class);
                startActivity(i);
            }
        });

        // user clicked login button
        logInBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String emailAddress = email.getText().toString().toLowerCase();
                if (isEmailValid(emailAddress)) {
                    logIn(emailAddress, password.getText().toString());
                } else {
                    email.setError("Please enter a valid email address");
                }
            }
        });

        u = new Utils();

    }


    private void startMaps(User user) {
        Intent i = new Intent(LoginActivity.this, MapsActivity.class);
        String username = user.getUsername();

        u.setUsername(LoginActivity.this, username);

        i.putExtra("username", user.getUsername());
        startActivity(i);
    }

    private void logIn(final String email, final String password) {

        users.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                System.out.println("comes here");
                if (dataSnapshot.child(encodeString(email)).exists()) {
                    if (!email.isEmpty()) {
                        User login = dataSnapshot.child(encodeString(email)).getValue(User.class);
                        if (login.getPassword().equals(u.getHashedPassword(password))) {
                            Toast.makeText(LoginActivity.this, "Login Successful",
                                    Toast.LENGTH_SHORT).show();

                            u.setUsername(LoginActivity.this, login.getUsername());
                            u.setPassword(LoginActivity.this, login.getPassword());
                            u.setEmail(LoginActivity.this, login.getEmail());

                            startMaps(login);

                        } else {
                            Toast.makeText(LoginActivity.this, "Password Incorrect",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                } else {
                    Toast.makeText(LoginActivity.this, "Email Address is not registered",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
