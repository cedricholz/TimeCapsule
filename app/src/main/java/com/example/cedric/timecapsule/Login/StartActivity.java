package com.example.cedric.timecapsule.Login;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.example.cedric.timecapsule.Maps.MapsActivity;
import com.example.cedric.timecapsule.R;
import com.example.cedric.timecapsule.UserInformation.User;
import com.example.cedric.timecapsule.Utils.Utils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class StartActivity extends AppCompatActivity {

    Button signUpBtn, logInBtn;

    Utils u = new Utils();
    FirebaseDatabase database;

    DatabaseReference lowercaseUserNames;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        signUpBtn = (Button) findViewById(R.id.btnSignUp);
        logInBtn = (Button) findViewById(R.id.btnLogIn);

        database = FirebaseDatabase.getInstance();
        lowercaseUserNames = database.getReference("lowercaseUsers");

        // user clicked sign-up button
        signUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(StartActivity.this, SignUpActivity.class);
                startActivity(i);
            }
        });

        // user clicked log-in button
        logInBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(StartActivity.this, LoginActivity.class);
                startActivity(i);

            }
        });

        u = new Utils();

        startIfLoggedIn();

    }

    private void startMaps(User user) {
        Intent i = new Intent(StartActivity.this, MapsActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        String username = user.getUsername();

        u.setUsername(StartActivity.this, username);

        i.putExtra("username", user.getUsername());
        startActivity(i);
    }

    private void startIfLoggedIn() {

        String username = u.getUsername(StartActivity.this);

        if (!username.equals("Default") && !username.equals("")) {
            String password = u.getPassword(StartActivity.this);
            String email = u.getEmail(StartActivity.this);

            User user = new User(username, password, email);
            startMaps(user);
        }
    }

}
