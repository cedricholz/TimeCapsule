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

public class SignUpActivity extends AppCompatActivity {

    // Firebase
    FirebaseDatabase database;
    DatabaseReference users;

    // User Input
    EditText username, password, email;

    // Button
    Button cancelBtn, createBtn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        // setting up firebase
        database = FirebaseDatabase.getInstance();
        users = database.getReference("users");

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
                final User user = new User(username.getText().toString(), password.getText().toString(),
                        email.getText().toString());

                users.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.child(encodeString(user.getEmail())).exists())
                            Toast.makeText(SignUpActivity.this, "The Username Already Exists!",
                                    Toast.LENGTH_SHORT).show();
                        else {
                            users.child(encodeString(user.getEmail())).setValue(user);
                            Toast.makeText(SignUpActivity.this, "Success Register!",
                                    Toast.LENGTH_SHORT).show();
                            Intent i = new Intent(SignUpActivity.this, LoginActivity.class);
                            i.putExtra("email", user.getEmail().toString());
                            i.putExtra("password", user.getPassword().toString());
                            startActivity(i);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        // work left
                    }
                });

            }
        });

    }

    public static String encodeString(String string) {
        return string.replace(".", ",");
    }
}
