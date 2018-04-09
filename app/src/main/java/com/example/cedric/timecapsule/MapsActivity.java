package com.example.cedric.timecapsule;

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
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;
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
    private ImageButton boxesButton;
    private String curAddress = "";
    private String username = "DEFAULT";
    private int mapStyle;
    private FirebaseDatabase database;
    private DatabaseReference myRef;
    private GeoFire geoFire;
    private Utils u;
    private ArrayList<PlaceTile> placeTiles;
    private String lastAdded = "";
    private GoogleMap mMap;
    private boolean locationMarked = false;
    private Intent boxDialogIntent;
    private Intent nearbyDialogIntent;
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
        myRef = database.getReference("locations");
        geoFire = new GeoFire(myRef);

        u = new Utils();

        placeTiles = new ArrayList<>();
        AddBearLandmarksToFirebase();

        addButtonListeners();
    }

    public void AddBearLandmarksToFirebase() {

        String jsonString = u.loadJSONFromAsset("bear_statues.json", this);
        Place[] landmarks = new Gson().fromJson(jsonString, Place[].class);

        for (Place lm : landmarks) {
            String[] coordinatesSplit = lm.getCoordinates().split(",");
            Double lat = Double.parseDouble(coordinatesSplit[0]);
            Double lon = Double.parseDouble(coordinatesSplit[1]);

            geoFire.setLocation(lm.getLandmark_name() + "%University Of California, Berkeley%" + lm.getFilename(), new GeoLocation(lat, lon), new GeoFire.CompletionListener() {
                @Override
                public void onComplete(String key, DatabaseError error) {
                    if (error != null) {
                        System.err.println("There was an error saving the location to GeoFire: " + error);
                    } else {
                        System.out.println("Location saved on server successfully!");
                    }
                }
            });
        }
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)

            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

            Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

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

                        if (title != null) {
                            boxDialogIntent = new Intent(MapsActivity.this, boxDialog.class);
                            boxDialogIntent.putExtra("boxName", title);
                            boxDialogIntent.putExtra("address", address);
                            boxDialogIntent.putExtra("username", username);
                            startActivity(boxDialogIntent);

                        }
                    } else {
                        Toast.makeText(MapsActivity.this, "You Are Too Far Away To Access This Landmark...", Toast.LENGTH_SHORT).show();
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

                lastLocation = userLocation;
                userLocation = new LatLng(location.getLatitude(), location.getLongitude());
//                mMap.clear();

                getNearbyMarkers(userLocation);

                mMap.moveCamera(CameraUpdateFactory.newLatLng(userLocation));

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
        } else {

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            } else {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

                Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
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

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {

                new LovelyTextInputDialog(MapsActivity.this, R.style.EditTextTintTheme)
                        .setTopColorRes(R.color.lightGreen)
//                        .setTitle("Create Memory Box")
                        .setTitle("Create New Landmark")
                        .setConfirmButtonColor(R.color.black)
                        .setNegativeButtonColor(R.color.black)
                        .setTopTitleColor(R.color.black)
//                        .setHint("Enter Box Name...")
                        .setHint("Enter Landmark Name...")
                        .setMessage(curAddress)
//                        .setIcon(R.drawable.newbox)
                        .setIcon(R.drawable.ic_add_blue_24dp)

                        .setConfirmButton(android.R.string.ok, new LovelyTextInputDialog.OnTextInputConfirmListener() {
                            @Override
                            public void onTextInputConfirmed(String text) {

                                if (userLocation != null && text.length() > 0) {
                                    createBoxIfFree(userLocation, text);
                                } else {
                                    if (userLocation == null) {
                                        Toast.makeText(MapsActivity.this, "Unable to Create Landmark, Please Check That Your Location Is Turned On.", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }
                        })
                        .setNegativeButton("CANCEL", null)
                        .show();
            }
        });
        boxesButton = findViewById(R.id.boxesButton);
        boxesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {

                nearbyDialogIntent = new Intent(MapsActivity.this, NearbyDialog.class);
                startActivity(nearbyDialogIntent);

            }
        });

    }

    public void savePlaceTiles() {
        SharedPreferences.Editor editor = prefs.edit();
        Gson gson = new Gson();
        String jsonPlaceTiles = gson.toJson(placeTiles);
        editor.putString("placeTiles", jsonPlaceTiles).commit();
    }

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
                    String fileName = "";

                    if (splitString.length > 1) {
                        title = splitString[0];
                        address = splitString[1];
                        fileName = splitString[2];
                    }

                    if (!title.equals(lastAdded)) {
                        DecimalFormat df = new DecimalFormat();
                        df.setMaximumFractionDigits(2);

                        String distance = df.format(u.getDistance(userLocation, markerLatLng) / 1000) + " KM";
                        PlaceTile pt = new PlaceTile(fileName, title, distance, address);
                        placeTiles.add(pt);


                        savePlaceTiles();


                        MarkerOptions m = new MarkerOptions();
                        m.position(markerLatLng).title(title);
                        m.position(markerLatLng).snippet(address);
                        mMap.addMarker(m);
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


    public void createBoxIfFree(LatLng loc, final String title) {
        GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(loc.latitude, loc.longitude), .1);
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
                    mMap.addMarker(m);

                    geoFire.setLocation(title + "%" + curAddress + "%oski_bear", new GeoLocation(userLocation.latitude, userLocation.longitude), new GeoFire.CompletionListener() {
                        @Override
                        public void onComplete(String key, DatabaseError error) {
                            if (error != null) {
                                System.err.println("There was an error saving the location to GeoFire: " + error);
                            } else {
                                System.out.println("Location saved on server successfully!");
                            }
                        }
                    });

                    boxDialogIntent = new Intent(MapsActivity.this, boxDialog.class);
                    boxDialogIntent.putExtra("boxName", title);
                    boxDialogIntent.putExtra("address", curAddress);
                    boxDialogIntent.putExtra("username", username);

                    startActivity(boxDialogIntent);
                } else {
                    Toast.makeText(MapsActivity.this, "This Location Already Has A Landmark...", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {
                System.err.println("There was an error with this query: " + error);
            }
        });

    }

}