package com.example.cedric.timecapsule;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.app.LoaderManager.LoaderCallbacks;

import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.Manifest.permission.READ_CONTACTS;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity {

    SharedPreferences prefs;
    // UI references.
    private EditText userNameInput;
    private ImageButton submitButton;
    private FirebaseDatabase database;
    private DatabaseReference myRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        // Set up the login form.
        prefs = this.getSharedPreferences(
                "com.example.cedric.timecapsule", Context.MODE_PRIVATE);


        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("users");

        if (prefs.contains("username")) {
            startMapsActivity();
        } else {
            userNameInput = findViewById(R.id.username_edit_text);
            submitButton = findViewById(R.id.submit_button);

            submitButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    final String username = userNameInput.getText().toString();


                    if (username.length() < 1) {
                        userNameInput.requestFocus();
                    } else {
                        if (username.length() < 24) {


                            myRef.orderByKey().equalTo(username).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {

                                    if (dataSnapshot.getValue() == null) {
                                        //Username is not in database
                                        myRef.child(username).setValue("1");

                                        InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                        mgr.hideSoftInputFromWindow(userNameInput.getWindowToken(), 0);
                                        prefs.edit().putString("username", username).commit();
                                        startMapsActivity();
                                    } else {
                                        // Username is in database
                                        Toast.makeText(LoginActivity.this, "That username is in use...", Toast.LENGTH_SHORT).show();
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError firebaseError) {
                                }
                            });
                        } else {
                            Toast.makeText(LoginActivity.this, "Username too long.", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });
        }
    }

    public void startMapsActivity() {
        Intent mapsActivity = new Intent(LoginActivity.this, MapsActivity.class);
//        mapsActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(mapsActivity);
        ActivityCompat.finishAffinity(LoginActivity.this);
    }
}

