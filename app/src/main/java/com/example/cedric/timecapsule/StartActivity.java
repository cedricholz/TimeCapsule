package com.example.cedric.timecapsule;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.example.cedric.timecapsule.UserInformation.User;

import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

public class StartActivity extends AppCompatActivity {

    Button signUpBtn, logInBtn;

    Utils u = new Utils();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        signUpBtn = (Button) findViewById(R.id.btnSignUp);
        logInBtn = (Button) findViewById(R.id.btnLogIn);

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
        String username = user.getUsername();

        u.setUsername(StartActivity.this, username);

        i.putExtra("username", user.getUsername());
        startActivity(i);
    }

    private void startIfLoggedIn() {

        String username = u.getUsername(StartActivity.this);

        if (!username.equals("Default")) {

            String password = u.getPassword(StartActivity.this);
            String email = u.getEmail(StartActivity.this);

            User user = new User(username, password, email);
            startMaps(user);
        }
    }

}
