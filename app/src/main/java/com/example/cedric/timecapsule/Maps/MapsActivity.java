package com.example.cedric.timecapsule.Maps;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.cedric.timecapsule.Comments.CommentDialog;
import com.example.cedric.timecapsule.Login.StartActivity;
import com.example.cedric.timecapsule.Messaging.ConversationsDialog;
import com.example.cedric.timecapsule.NearbyBoxes.NearbyDialog;
import com.example.cedric.timecapsule.NearbyBoxes.PlaceTile;
import com.example.cedric.timecapsule.R;
import com.example.cedric.timecapsule.Utils.Utils;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.yarolegovich.lovelydialog.LovelyCustomDialog;
import com.yarolegovich.lovelydialog.LovelyStandardDialog;
import com.yarolegovich.lovelydialog.LovelyTextInputDialog;

import java.io.IOException;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {


    SharedPreferences prefs;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private LatLng lastLocation;
    private LatLng userLocation;
    private ImageButton addButton;
    private ImageButton logoutButton;
    private ImageButton boxesButton;
    private String curAddress = "";
    private String username = "DEFAULT";
    private int mapStyle;
    private FirebaseDatabase database;
    private DatabaseReference locationsRef;

    private DatabaseReference usersRef;
    private GeoFire geoFire;
    private Utils u;
    private ArrayList<PlaceTile> placeTiles;
    private String lastAdded = "";
    private GoogleMap mMap;
    private boolean locationMarked = false;
    private Intent boxDialogIntent;
    private Intent nearbyDialogIntent;
    private ImageButton messagesButton;
    private long mLastClickTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        mapStyle = R.raw.day_style_json;

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        prefs = this.getSharedPreferences(
                "com.example.cedric.timecapsule", Context.MODE_PRIVATE);

        username = prefs.getString("username", "Anonymous");

        database = FirebaseDatabase.getInstance();
        locationsRef = database.getReference("locations");
        usersRef = database.getReference("users");

        geoFire = new GeoFire(locationsRef);

        u = new Utils();

        placeTiles = new ArrayList<>();
        //u.addBearLandmarksToFirebase();

        addButtonListeners();
    }


    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)

                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);

            Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (lastKnownLocation == null) {
                lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }

            mMap.clear();
            if (lastKnownLocation != null) {
                LatLng userLocation = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
                mMap.moveCamera(CameraUpdateFactory.newLatLng(userLocation));
            }
            mMap.setMyLocationEnabled(true);
        }
    }

    public boolean withinRange(LatLng x, LatLng y) {
        return u.getDistance(x, y) <= u.getValidDistanceMeters();
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;


        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {

                if (SystemClock.elapsedRealtime() - mLastClickTime > 1000) {
                    if (withinRange(userLocation, marker.getPosition())) {

                        String title = marker.getTitle();
                        String address = marker.getSnippet();
                        String imageName = (String) marker.getTag();

                        if (title != null) {
                            boxDialogIntent = new Intent(MapsActivity.this, CommentDialog.class);
                            boxDialogIntent.putExtra("boxName", title);
                            boxDialogIntent.putExtra("address", address);
                            boxDialogIntent.putExtra("username", username);
                            boxDialogIntent.putExtra("imageName", imageName);
                            startActivity(boxDialogIntent);
                        }
                    } else {
                        Toast.makeText(MapsActivity.this, "You Are Too Far Away To Access This Box...", Toast.LENGTH_SHORT).show();
                    }
                }
                mLastClickTime = SystemClock.elapsedRealtime();
                return true;
            }
        });


        mMap.getUiSettings().setMapToolbarEnabled(false);

        boolean success = googleMap.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                        this, mapStyle));

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);


        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

                if (u.getDistance(lastLocation, userLocation) > u.getDistanceChangeToMoveCamera()) {
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(userLocation));
                }

                lastLocation = userLocation;

                userLocation = new LatLng(location.getLatitude(), location.getLongitude());

                u.setLocation(MapsActivity.this, location.getLatitude(), location.getLongitude());

//                mMap.clear();

                getNearbyMarkers(userLocation);


                Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());

                try {
                    List<Address> listAddress = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);

                    updateAddress(listAddress);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {

            }
        };

        if (Build.VERSION.SDK_INT < 23) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
        } else {

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            } else {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);

                Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (lastKnownLocation == null) {
                    lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                }
//
//                mMap.clear();
                if (lastKnownLocation != null) {
                    LatLng userLocation = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(userLocation));
                }
                mMap.setMyLocationEnabled(true);
            }
        }
    }

    public void updateAddress(List<Address> listAddress) {
        if (listAddress != null && listAddress.size() > 0) {
            Log.d("PlaceInfo", listAddress.get(0).toString());

            String address = "";

            if (listAddress.get(0).getSubThoroughfare() != null) {
                address += listAddress.get(0).getSubThoroughfare() + ", ";
            }

            if (listAddress.get(0).getThoroughfare() != null) {
                address += listAddress.get(0).getThoroughfare() + ", ";
            }
            if (listAddress.get(0).getLocality() != null) {

                address += listAddress.get(0).getLocality();
            }
//            if (listAddress.get(0).getPostalCode() != null){
//
//                address += listAddress.get(0).getPostalCode() + ", ";
//            }
//            if (listAddress.get(0).getCountryName() != null){
//
//                address += listAddress.get(0).getCountryName();
//            }

            if (address.length() > 0) {
                curAddress = address;
            }
        }
    }


    public void addButtonListeners() {

        addButton = findViewById(R.id.addButton);

        addButton.setOnClickListener(arg0 -> {
            final LovelyCustomDialog x = new LovelyCustomDialog(MapsActivity.this, R.style.EditTextTintTheme)
                    .setView(R.layout.customtest)
                    .setTopColorRes(R.color.lightGreen)
                    .setTitle("Create New Box")
                    .setTopTitleColor(R.color.black)
                    .setMessage(curAddress)
                    .setIcon(R.drawable.newbox);
            x.show();
            x.configureView((rootView) -> {
                final EditText inputField = rootView.findViewById(R.id.input_field_location);
                TextView confirmButton = rootView.findViewById(R.id.confirm_button_location);
                confirmButton.setOnClickListener(arg1 -> {
                    String text = inputField.getText().toString();
                    CheckBox checkBox = rootView.findViewById(R.id.private_checkbox);
                    if (userLocation != null && text.length() > 0) {
                        createBoxIfFree(userLocation, text, checkBox.isChecked());
                    } else {
                        if (userLocation == null) {
                            Toast.makeText(MapsActivity.this, "Unable to Create Box, Please Check That Your Location Is Turned On.", Toast.LENGTH_SHORT).show();
                        }
                    }
                    x.dismiss();
                });


                TextView negativeButton = rootView.findViewById(R.id.negative_button_location);
                negativeButton.setText("CANCEL");
                negativeButton.setOnClickListener(arg1 -> x.dismiss());
            });
        });
        boxesButton = findViewById(R.id.boxesButton);
        boxesButton.setOnClickListener(arg0 -> {

            nearbyDialogIntent = new Intent(MapsActivity.this, NearbyDialog.class);
            startActivity(nearbyDialogIntent);
        });


        messagesButton = findViewById(R.id.messaging);
        messagesButton.setOnClickListener(arg0 -> {
            Intent allMessagesIntent = new Intent(MapsActivity.this, ConversationsDialog.class);
            startActivity(allMessagesIntent);
        });


        logoutButton = findViewById(R.id.logout);

        logoutButton.setOnClickListener(arg0 -> new LovelyStandardDialog(MapsActivity.this, LovelyStandardDialog.ButtonLayout.HORIZONTAL)
                .setTopColorRes(R.color.indigo)
                .setButtonsColorRes(R.color.darkDeepOrange)
                .setIcon(R.drawable.ic_power_settings_white_24dp)
                .setTitle("Log Out")
                .setMessage("Do you want to log out?")
                .setPositiveButton(android.R.string.ok, v -> {
                    u.setUsername(MapsActivity.this, "");
                    Intent allMessagesIntent = new Intent(MapsActivity.this, StartActivity.class);
                    startActivity(allMessagesIntent);
                })
                .setNegativeButton(android.R.string.no, null)
                .show());

    }

    public void savePlaceTiles() {
        SharedPreferences.Editor editor = prefs.edit();
        Gson gson = new Gson();
        String jsonPlaceTiles = gson.toJson(placeTiles);
        editor.putString("placeTiles", jsonPlaceTiles).commit();
    }

    public void checkIfBoxIsPrivate(String key, String finalFileName, String finalTitle, String finalAddress, LatLng markerLatLng) {

        locationsRef.child(key).child("creator").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String creator = (String) dataSnapshot.getValue();
                if (creator != null) {
                    checkIfHaveAccess(key, finalFileName, finalTitle, finalAddress, markerLatLng);
                } else {
                    setMarker(key, finalFileName, finalTitle, finalAddress, markerLatLng);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    public void getPlaceTileDataAndAdd(String key, PlaceTile pt) {

        locationsRef.child(key).child("data").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String photos = (String) dataSnapshot.child("photos").getValue();
                String comments = (String) dataSnapshot.child("comments").getValue();
                String timestamp = (String) dataSnapshot.child("timestamp").getValue();

                if (photos != null) {
                    pt.numPhotos = photos;
                }
                if (comments != null) {
                    pt.numComments = comments;
                }

                if (timestamp != null) {
                    pt.timestamp = timestamp;
                }

                placeTiles.add(pt);
                savePlaceTiles();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }


    public void setMarker(String key, String finalFileName, String finalTitle, String finalAddress, LatLng markerLatLng) {
        DecimalFormat df = new DecimalFormat();
        df.setMaximumFractionDigits(2);

        String distance = df.format(u.getDistance(userLocation, markerLatLng) / 1000) + " KM";
        PlaceTile pt = new PlaceTile(finalFileName, finalTitle, distance, finalAddress, "0", "0", "0", key);
        getPlaceTileDataAndAdd(key, pt);


        MarkerOptions m = new MarkerOptions();
        m.position(markerLatLng).title(finalTitle);
        m.position(markerLatLng).snippet(finalAddress);
        m.icon(BitmapDescriptorFactory.fromResource(R.drawable.orange));

        Marker mark = mMap.addMarker(m);
        mark.setTag(finalFileName);
    }

    public void checkIfHaveAccess(String key, String finalFileName, String finalTitle, String finalAddress, LatLng markerLatLng) {

        locationsRef.child(key).child("users").child(username).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String myUsername = (String) dataSnapshot.getValue();
                if (myUsername != null) {
                    setMarker(key, finalFileName, finalTitle, finalAddress, markerLatLng);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }


    // TODO private boxes
    public void getNearbyMarkers(final LatLng loc) {

        if (lastLocation == null || u.getDistance(loc, lastLocation) >= u.getValidDistanceToRequestNewPinsKm()) {

            placeTiles = new ArrayList<>();

            savePlaceTiles();

            GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(loc.latitude, loc.longitude), u.getVisibleMarkerDistancekm());

            geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
                @Override
                public void onKeyEntered(String key, GeoLocation location) {
                    System.out.println(String.format("Key %s entered the search area at [%f,%f]", key, location.latitude, location.longitude));

                    LatLng markerLatLng = new LatLng(location.latitude, location.longitude);

                    String[] splitString = key.split("%");
                    String title = "";
                    String address = "";
                    String fileName = "oski_bear";

                    if (splitString.length > 1) {
                        title = splitString[0];
                        address = splitString[1];
                        fileName = splitString[2];
                    }

                    if (!title.equals(lastAdded)) {
                        String finalFileName = fileName;
                        String finalTitle = title;
                        String finalAddress = address;
                        checkIfBoxIsPrivate(key, finalFileName, finalTitle, finalAddress, markerLatLng);
                    }
                    lastAdded = title;
                }

                @Override
                public void onKeyExited(String key) {
                    System.out.println(String.format("Key %s is no longer in the search area", key));
                }

                @Override
                public void onKeyMoved(String key, GeoLocation location) {
                    System.out.println(String.format("Key %s moved within the search area to [%f,%f]", key, location.latitude, location.longitude));
                }

                @Override
                public void onGeoQueryReady() {
                    System.out.println("All initial data has been loaded and events have been fired!");
                }

                @Override
                public void onGeoQueryError(DatabaseError error) {
                    System.err.println("There was an error with this query: " + error);
                }
            });
        }
    }

    public void createBoxIfFree(LatLng loc, final String title, boolean checked) {
        GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(loc.latitude, loc.longitude), u.getValidDistanceFromMarkerForNewMarkerKm());
        locationMarked = false;
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                locationMarked = true;
            }

            @Override
            public void onKeyExited(String key) {
            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {
            }

            @Override
            public void onGeoQueryReady() {
                System.out.println("All initial data has been loaded and events have been fired!");
                if (locationMarked == false) {
                    MarkerOptions m = new MarkerOptions();
                    m.position(userLocation).title(title);
                    m.position(userLocation).snippet(curAddress);
                    m.icon(BitmapDescriptorFactory.fromResource(R.drawable.orange));


                    Marker mark = mMap.addMarker(m);
                    mark.setTag("oski_bear");

                    geoFire.setLocation(title + "%" + curAddress + "%oski_bear", new GeoLocation(userLocation.latitude, userLocation.longitude), new GeoFire.CompletionListener() {
                        @Override
                        public void onComplete(String key, DatabaseError error) {
                            if (error != null) {
                                System.err.println("There was an error saving the location to GeoFire: " + error);
                            } else {
                                System.out.println("Location saved on server successfully!");
                                if (checked) {
                                    locationsRef.child(key).child("users").child(username).setValue("1");
                                    locationsRef.child(key).child("creator").setValue(username);
                                }
                                String timeStamp = Long.toString(System.currentTimeMillis());
                                locationsRef.child(key).child("data").child("timestamp").setValue(timeStamp);

                                usersRef.child(username).child("boxes").child(key).setValue("1");


                            }
                        }
                    });
                    if (checked) {
                        LovelyTextInputDialog textInputDialog = new LovelyTextInputDialog(MapsActivity.this, R.style.EditTextTintTheme)
                                .setTopColorRes(R.color.lightGreen)
                                .setTitle("Sharing Settings")
                                .setTopTitleColor(R.color.black)
                                .setMessage("Enter a username to share this box with someone");
                        textInputDialog
                                .setNegativeButton("CLOSE", view -> {
                                    boxDialogIntent = new Intent(MapsActivity.this, CommentDialog.class);
                                    boxDialogIntent.putExtra("boxName", title);
                                    boxDialogIntent.putExtra("address", curAddress);
                                    boxDialogIntent.putExtra("username", username);
                                    boxDialogIntent.putExtra("imageName", "oski_bear");

                                    startActivity(boxDialogIntent);
                                    textInputDialog.dismiss();
                                })
                                .setHint("Username")
                                .configureView(rootView -> {
                                    TextView confirmButton = rootView.findViewById(R.id.ld_btn_confirm);
                                    EditText editText = rootView.findViewById(R.id.ld_text_input);
                                    confirmButton.setText("Share");
                                    confirmButton.setOnClickListener(view -> {
                                        String content = editText.getText().toString();
                                        if (!content.equals("")) {
                                            locationsRef.child("lowercaseUsers").child(content.toLowerCase()).addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {

                                                    String user = (String) dataSnapshot.getKey();

                                                    if (user != null) {
                                                        FirebaseDatabase.getInstance()
                                                                .getReference("locations")
                                                                .child(title + "%" + curAddress + "%oski_bear")
                                                                .child("users")
                                                                .child(content).setValue("1");
                                                        editText.setText("");
                                                        Toast.makeText(MapsActivity.this, "Box shared with user", Toast.LENGTH_SHORT).show();
                                                    } else {
                                                        Toast.makeText(MapsActivity.this, "User does not exist", Toast.LENGTH_SHORT).show();
                                                    }
                                                }

                                                @Override
                                                public void onCancelled(DatabaseError databaseError) {

                                                }
                                            });
                                        }
                                    });

                                })
                                .show();
                    }
                } else {
                    Toast.makeText(MapsActivity.this, "This Location Already Has A Box...", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {
                System.err.println("There was an error with this query: " + error);
            }
        });

    }

}