package com.projects.solace.student.rickshaw;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

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
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.projects.solace.R;
import com.projects.solace.common.Constants;
import com.projects.solace.common.DirectionsAPI;
import com.projects.solace.common.GeocodingAPI;
import com.projects.solace.common.RetrofitClient;
import com.projects.solace.models.DirectionsResponse;
import com.projects.solace.models.GeocodingResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RickshawBookingActivity extends AppCompatActivity implements OnMapReadyCallback {


    private final Context context = RickshawBookingActivity.this;
    private GoogleMap mMap;
    private TextView tvPickupLocation, tvDropLocation;
    private Button btnBookRickshaw;

    private FusedLocationProviderClient fusedLocationProviderClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rickshaw_booking);

        initialize();
        listeners();
    }

    private void listeners() {
        // Set listeners for location selection
        tvPickupLocation.setOnClickListener(v -> {
            Intent intent = new Intent(this, LocationPickerActivity.class);
            intent.putExtra("location", "pickup");
            startActivity(intent);
        }); // 1 for pickup
        tvDropLocation.setOnClickListener(v -> {
            Intent intent = new Intent(this, LocationPickerActivity.class);
            intent.putExtra("location", "drop");
            startActivity(intent);
        });  // 2 for drop
        btnBookRickshaw.setOnClickListener(view -> {
            onBackPressed();  // Navigate back to the previous activity
        });
    }

    private void initialize() {

        Toolbar toolbar = findViewById(R.id.mToolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Show the back button
        getSupportActionBar().setHomeButtonEnabled(true); // This ensures the button is clickable
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // Set up text views
        tvPickupLocation = findViewById(R.id.tvPickupLocation);
        tvDropLocation = findViewById(R.id.tvDropLocation);
        btnBookRickshaw = findViewById(R.id.btnBookRickshaw);

        if (Constants.pickupLatLng != null && Constants.dropLatLng != null) {
            btnBookRickshaw.setVisibility(View.VISIBLE);
        } else {
            btnBookRickshaw.setVisibility(View.GONE);
        }
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        if (Constants.pickupLatLng != null && Constants.dropLatLng != null) {
            tvDropLocation.setText(Constants.dropLocationName);
            tvPickupLocation.setText(Constants.pickupLocationName);
            mMap.clear();
            mMap.addMarker(new MarkerOptions().position(Constants.pickupLatLng).title("Pickup Location"));
            mMap.addMarker(new MarkerOptions().position(Constants.dropLatLng).title("Drop Location"));
            fetchDirections();
            btnBookRickshaw.setVisibility(View.VISIBLE);
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
            return;
        }
        fusedLocationProviderClient.getLastLocation().addOnCompleteListener(task -> {
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


    private void fetchDirections() {
        // Create Retrofit instance
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://maps.googleapis.com/maps/api/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        DirectionsAPI directionsAPI = retrofit.create(DirectionsAPI.class);

        String origin = Constants.pickupLatLng.latitude + "," + Constants.pickupLatLng.longitude;
        String destination = Constants.dropLatLng.latitude + "," + Constants.dropLatLng.longitude;
        directionsAPI.getDirections(origin, destination, Constants.API_KEY).enqueue(new Callback<DirectionsResponse>() {
            @Override
            public void onResponse(Call<DirectionsResponse> call, Response<DirectionsResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Constants.distance = response.body().getRouteDistance();
                    drawRoute(response.body().getRoutePoints());
                } else {
                    Toast.makeText(context, "Failed to fetch directions", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<DirectionsResponse> call, Throwable t) {
                Toast.makeText(context, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void drawRoute(List<LatLng> points) {
        // Draw polyline
        mMap.addPolyline(new PolylineOptions()
                .addAll(points)
                .width(10)
                .color(android.graphics.Color.BLUE)
        );

        // Adjust the camera
        if (!points.isEmpty()) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(points.get(0), 15));
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle back button press
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();  // Navigate back to the previous activity
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (Constants.pickupLatLng != null) {
            fetchLocationName("pickup");
        }
        if (Constants.dropLatLng != null) {
            fetchLocationName("drop");
        }
        if (Constants.pickupLatLng != null && Constants.dropLatLng != null) {

            tvDropLocation.setText(Constants.dropLocationName);
            tvPickupLocation.setText(Constants.pickupLocationName);
            mMap.clear();
            mMap.addMarker(new MarkerOptions().position(Constants.pickupLatLng).title("Pickup Location"));
            mMap.addMarker(new MarkerOptions().position(Constants.dropLatLng).title("Drop Location"));
            fetchDirections();
            btnBookRickshaw.setVisibility(View.VISIBLE);
        }
    }


    public void fetchLocationName(String location) {
        // Create Retrofit instance and API interface
        GeocodingAPI api = RetrofitClient.getGeocodingAPIService();

        String latlng = "";
        if (location.equals("pickup")) {
            latlng = Constants.pickupLatLng.latitude + "," + Constants.pickupLatLng.longitude;
        } else if (location.equals("drop")) {
            latlng = Constants.dropLatLng.latitude + "," + Constants.dropLatLng.longitude;
        }
        // Format latlng

        // Make the API call
        Call<GeocodingResponse> call = api.getGeocoding(latlng, Constants.API_KEY);
        call.enqueue(new Callback<GeocodingResponse>() {
            @Override
            public void onResponse(Call<GeocodingResponse> call, Response<GeocodingResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Parse the response to get location name
                    String locationName = response.body().getResults().get(0).getFormattedAddress();
                    Log.d("LocationName", locationName);
                    if (location.equals("pickup")) {
                        Constants.pickupLocationName = locationName;
                        tvPickupLocation.setText(locationName);
                    } else if (location.equals("drop")) {
                        Constants.dropLocationName = locationName;
                        tvDropLocation.setText(locationName);
                    }
                    // You can update UI or pass this location name as needed
                } else {
                    Log.e("GeocodingError", "Failed to fetch location name: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<GeocodingResponse> call, Throwable t) {
                Log.e("GeocodingError", "API call failed: " + t.getMessage());
            }
        });
    }

}
