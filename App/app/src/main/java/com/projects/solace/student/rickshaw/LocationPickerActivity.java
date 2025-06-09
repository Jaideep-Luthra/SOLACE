package com.projects.solace.student.rickshaw;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.projects.solace.R;
import com.projects.solace.common.Constants;

import java.util.Arrays;

public class LocationPickerActivity extends AppCompatActivity implements OnMapReadyCallback {
    private final Context context = LocationPickerActivity.this;
    LatLng selectedLatLng;
    String selectedLocationName;
    String location;
    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private Button btnSelectLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_picker);


        initialize();
        listeners();
    }

    private void listeners() {

        btnSelectLocation.setOnClickListener(view -> {
            if (location.equals("pickup")) {
                Constants.pickupLatLng = selectedLatLng;
                Constants.pickupLocationName = selectedLocationName;
            } else {
                Constants.dropLatLng = selectedLatLng;
                Constants.dropLocationName = selectedLocationName;
            }
            onBackPressed();
        });
    }

    private void initialize() {

        Toolbar toolbar = findViewById(R.id.mToolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Show the back button
        getSupportActionBar().setHomeButtonEnabled(true); // This ensures the button is clickable

        location = getIntent().getStringExtra("location");
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        // Initialize Places SDK
        Places.initialize(getApplicationContext(), Constants.API_KEY);

        // Initialize map
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        btnSelectLocation = findViewById(R.id.btnSelectLocation);

        if (Constants.pickupLatLng != null && Constants.dropLatLng != null) {
            btnSelectLocation.setVisibility(View.VISIBLE);
        } else {
            btnSelectLocation.setVisibility(View.GONE);
        }
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle back button press
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();  // Navigate back to the previous activity
            return true;
        }
        if (item.getItemId() == R.id.action_search) {
            openAutocomplete(1);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void openAutocomplete(int requestCode) {
        // Launch the autocomplete intent
        Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN,
                Arrays.asList(com.google.android.libraries.places.api.model.Place.Field.LAT_LNG,
                        com.google.android.libraries.places.api.model.Place.Field.NAME))
                .build(this);
        startActivityForResult(intent, requestCode);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            com.google.android.libraries.places.api.model.Place place = Autocomplete.getPlaceFromIntent(data);

            if (requestCode == 1) {
                selectedLatLng = place.getLatLng();
                selectedLocationName = place.getName();
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(selectedLatLng, 15));
            }
            btnSelectLocation.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        googleMap.setOnCameraIdleListener(() -> {
            selectedLatLng = googleMap.getCameraPosition().target;
            btnSelectLocation.setVisibility(View.VISIBLE);
        });

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
            return;
        }
        fusedLocationClient.getLastLocation().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                Location location = task.getResult();
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();

                // Use the latitude and longitude here
                System.out.println("Latitude: " + latitude);
                System.out.println("Longitude: " + longitude);

                // Set default location
                LatLng defaultLocation = new LatLng(latitude, longitude);
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 15));
            } else {
                // Handle location not available
                System.out.println("Location not available");
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_location_picker, menu);
        return true;
    }

}