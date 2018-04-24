package com.example.cedric.timecapsule;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.cedric.timecapsule.UserInformation.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SignUpActivity extends AppCompatActivity {

    // Firebase
    FirebaseDatabase database;
    DatabaseReference users;
    DatabaseReference userNames;

    // User Input
    EditText username, password, email;

    // Button
    Button cancelBtn, createBtn;

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
        setContentView(R.layout.activity_sign_up);

        u = new Utils();

        // setting up firebase
        database = FirebaseDatabase.getInstance();
        users = database.getReference("users");

        userNames = database.getReference("usernames");

        // setting up user input
        username = (EditText) findViewById(R.id.SignUpUsername);
        password = (EditText) findViewById(R.id.SignUpPassword);
        email = (EditText) findViewById(R.id.SignUpEmail);

        // setting up buttons
        cancelBtn = (Button) findViewById(R.id.btnCancel);
        createBtn = (Button) findViewById(R.id.btnCreate);

        // user clicked cancel button (goes back to StartActivity with no input)
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(SignUpActivity.this, StartActivity.class);
                startActivity(i);
            }
        });

        // user clicked create button (goes back to StartActivity with user input)
        createBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isEmailValid(email.getText().toString())) {
                    User user = new User(username.getText().toString(), password.getText().toString(), email.getText().toString());

                    checkEmailFree(user);
                } else {
                    email.setError("Please enter a valid email address");
                }
            }
        });

    }


    private void checkEmailFree(final User user) {
        users.child(encodeString(user.getEmail())).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.getValue() != null) {
                    Toast.makeText(SignUpActivity.this, "That Email Address Already Exists!",
                            Toast.LENGTH_SHORT).show();
                } else {
                    checkUsernameFree(user);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // work left
            }
        });
    }

    private void checkUsernameFree(final User user) {
        userNames.child(encodeString(user.getUsername())).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.getValue() != null) {
                    Toast.makeText(SignUpActivity.this, "That Username Already Exists!",
                            Toast.LENGTH_SHORT).show();
                } else {
                    createUser(user);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // work left
            }
        });
    }

    private void createUser(User user) {

        String plainTextPassword = user.getPassword();
        user.setPassword(u.getHashedPassword(plainTextPassword));

        users.child(encodeString(user.getEmail())).setValue(user);


        userNames.child(encodeString(user.getUsername())).setValue("1");

        Toast.makeText(SignUpActivity.this, "Success Register!",
                Toast.LENGTH_SHORT).show();

        Intent i = new Intent(SignUpActivity.this, LoginActivity.class);
        i.putExtra("email", user.getEmail().toString());
        i.putExtra("password", plainTextPassword);
        startActivity(i);

    }
}
