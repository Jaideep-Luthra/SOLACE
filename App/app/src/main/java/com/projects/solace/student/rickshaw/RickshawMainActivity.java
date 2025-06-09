package com.projects.solace.student.rickshaw;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.maps.model.LatLng;
import com.projects.solace.R;
import com.projects.solace.common.Constants;
import com.projects.solace.common.IotApiService;
import com.projects.solace.common.RetrofitClient;
import com.projects.solace.models.ApiResponse;
import com.projects.solace.models.BookingModel;
import com.projects.solace.models.ResponseModel;

import java.util.HashMap;
import java.util.Objects;
import java.util.Random;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RickshawMainActivity extends AppCompatActivity {
    private final Context context = RickshawMainActivity.this;
    private final Handler handler = new Handler();
    private AlertDialog dialog;
    private TextView tvSearch, tvEstimate, tvDriver, tvOTP, tvPickupLocation, tvDropLocation, tvDistance;
    private LinearLayout llPickupDrop, llOTP;
    private Button btnBookRickshaw;
    private String bookingId;
    private Runnable runnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rickshaw_main);

        initialize();
        listeners();

    }

    private void listeners() {
        tvSearch.setOnClickListener(view -> {
            Intent intent = new Intent(context, RickshawBookingActivity.class);
            startActivity(intent);
        });
        btnBookRickshaw.setOnClickListener(view -> showWaitingDialog());
    }

    private void showWaitingDialog() {
        bookRickshaw();
        // Inflate the custom layout for the dialog
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_waiting, null);

        // Build the dialog
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setView(dialogView);
        dialogBuilder.setCancelable(false); // Prevent dismissing by tapping outside

        // Create and show the dialog
        dialog = dialogBuilder.create();
        dialog.show();

        // Handle cancel button click
        Button cancelButton = dialogView.findViewById(R.id.btnCancel);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle cancel action here
                dialog.dismiss();
            }
        });
    }

    private void bookRickshaw() {
        IotApiService apiService = RetrofitClient.getIotApiService();

        HashMap<String, String> data = new HashMap<>();
        data.put("pickup_location", Constants.pickupLatLng.latitude + "," + Constants.pickupLatLng.longitude);
        data.put("pickup_location_name", Constants.pickupLocationName);
        data.put("drop_location", Constants.dropLatLng.latitude + "," + Constants.dropLatLng.longitude);
        data.put("drop_location_name", Constants.dropLocationName);
        data.put("distance", Constants.distance);
        data.put("money", Constants.amount);
        data.put("user_email", Constants.email);
        data.put("user_name", Constants.name);
        Random random = new Random();
        int randomNumber = 1000 + random.nextInt(9000); // Generates a 4-digit random number

        data.put("otp", String.valueOf(randomNumber));

        llOTP.setVisibility(View.VISIBLE);
        tvOTP.setText(String.valueOf(randomNumber));
        // Make the request asynchronously
        apiService.bookRickshaw(data).enqueue(new Callback<ResponseModel>() {
            @Override
            public void onResponse(Call<ResponseModel> call, Response<ResponseModel> response) {
                if (response.isSuccessful()) {

                    Log.d("API Response", response.body().toString());
                    bookingId = response.body().getId();

                    startPeriodicGetRequest();
                } else {
                    Log.d("API Error", "Error code: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ResponseModel> call, Throwable t) {
                Log.d("API Failure", t.getMessage());
            }
        });
    }

    private void initialize() {

        Toolbar toolbar = findViewById(R.id.mToolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Show the back button
        getSupportActionBar().setHomeButtonEnabled(true); // This ensures the button is clickable

        // Set up text views
        tvPickupLocation = findViewById(R.id.tvPickupLocation);
        tvDropLocation = findViewById(R.id.tvDropLocation);
        tvSearch = findViewById(R.id.tvSearch);
        tvDistance = findViewById(R.id.tvDistance);
        tvEstimate = findViewById(R.id.tvEstimate);
        tvDriver = findViewById(R.id.tvDriver);
        tvOTP = findViewById(R.id.tvOTP);

        btnBookRickshaw = findViewById(R.id.btnBookRickshaw);

        llPickupDrop = findViewById(R.id.llPickupDrop);
        llOTP = findViewById(R.id.llOTP);

        llPickupDrop.setVisibility(View.GONE);
        llOTP.setVisibility(View.GONE);
        btnBookRickshaw.setVisibility(View.GONE);
        pickupDropStuff();
    }

    private void pickupDropStuff() {
        if (Objects.equals(Constants.distance, "0 km")) {
            Toast.makeText(context, "We are not supporting mentioned route.", Toast.LENGTH_LONG).show();
        }
        if (tvSearch.getVisibility() == View.VISIBLE) {
            if (Constants.pickupLatLng != null && Constants.dropLatLng != null) {
                tvSearch.setVisibility(View.GONE);
                btnBookRickshaw.setVisibility(View.VISIBLE);
                llPickupDrop.setVisibility(View.VISIBLE);

                tvPickupLocation.setText(Constants.pickupLocationName);
                tvDropLocation.setText(Constants.dropLocationName);
                tvDistance.setText(Constants.distance);
                Constants.amount = String.valueOf(getAmount(Constants.distance));
                tvEstimate.setText(Constants.amount);
                if (Constants.driverName != null) {
                    tvDriver.setText(Constants.driverName + " accepted the request");
                }

            }
        }
    }

    private int getAmount(String distance) {
        double distanceInKm = Double.parseDouble(distance.split(" ")[0]);
        return (int) (distanceInKm * 25);
    }


    private void startPeriodicGetRequest() {
        IotApiService apiService = RetrofitClient.getIotApiService();
        runnable = new Runnable() {
            @Override
            public void run() {
                // Make the API call
                apiService.getBookingDataById(bookingId).enqueue(new Callback<BookingModel>() {
                    @Override
                    public void onResponse(Call<BookingModel> call, Response<BookingModel> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            Log.d("API Response", response.body().toString());
                            BookingModel bookingModel = response.body();
                            // Stop the periodic task
                            if (bookingModel.getDriver_booked() == 1) {
                                Constants.driverName = bookingModel.getDriver_name();
                                Constants.driverEmail = bookingModel.getDriver_email();
                                btnBookRickshaw.setVisibility(View.GONE);
                                tvDriver.setText("Driver: " + Constants.driverName + " accepted the request");
                                dialog.dismiss();
                            }
                            if (bookingModel.getIs_ride_started() == 1) {
                                tvDriver.setText("Driver: " + Constants.driverName + " ride is started");
                            }
                            if (bookingModel.getIs_completed() == 1) {

                                if (runnable != null) {
                                    handler.removeCallbacks(runnable);
                                }
                                clearState();

                                if (bookingModel.getIs_completed_shown_user() == 0) {
                                    showFeedbackDialog(bookingModel.getId());
                                }
                            }
                        } else {
                            Log.d("API Error", "Error code: " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<BookingModel> call, Throwable t) {
                        Log.d("API Failure", t.getMessage());
                    }
                });

                // Schedule the next execution after 10 seconds
                handler.postDelayed(this, 10000);
            }
        };

        // Start the first execution
        handler.post(runnable);
    }


    private void clearState() {

        llPickupDrop.setVisibility(View.GONE);
        btnBookRickshaw.setVisibility(View.GONE);
        Constants.pickupLatLng = null;
        Constants.pickupLocationName = null;
        Constants.dropLatLng = null;
        Constants.dropLocationName = null;
        Constants.distance = null;
        Constants.driverEmail = null;
        Constants.driverName = null;
        tvDriver.setText("");
        tvSearch.setVisibility(View.VISIBLE);
    }

    private void getLastRideData() {
        IotApiService apiService = RetrofitClient.getIotApiService();
        apiService.getLastBookingDataByEmail(Constants.email).enqueue(new Callback<BookingModel>() {
            @Override
            public void onResponse(Call<BookingModel> call, Response<BookingModel> response) {
                BookingModel bookingModel = response.body();
                assert bookingModel != null;
                if (bookingModel.getStatus() != null && bookingModel.getStatus().equals("error")) {
                    return;
                }
                if (bookingModel.getIs_completed() == 1) {
                    if (bookingModel.getIs_completed_shown_user() == 0) {
                        showFeedbackDialog(bookingModel.getId());
                    }
                    return;
                }
                tvSearch.setVisibility(View.GONE);
                Constants.pickupLatLng = getLatLngFromString(bookingModel.getPickup_location());
                Constants.dropLatLng = getLatLngFromString(bookingModel.getDrop_location());

                Constants.pickupLocationName = bookingModel.getPickup_location_name();
                Constants.dropLocationName = bookingModel.getDrop_location_name();

                Constants.distance = bookingModel.getDistance();
                Constants.amount = String.valueOf(getAmount(Constants.distance));
                Constants.driverName = bookingModel.getDriver_name();
                tvOTP.setText(bookingModel.getOtp());
                pickupDropStuff();
                btnBookRickshaw.setVisibility(View.GONE);

                if (bookingModel.getDriver_booked() == 1) {

                    startPeriodicGetRequest();
                    llOTP.setVisibility(View.VISIBLE);
                }
                if (bookingModel.getIs_ride_started() == 1) {
                    llOTP.setVisibility(View.GONE);
                    tvDriver.setText(Constants.driverName + " started the ride");
                }

            }


            @Override
            public void onFailure(Call<BookingModel> call, Throwable t) {

            }
        });
    }

    private void showFeedbackDialog(String id) {
        // Inflate the custom layout
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.dialog_feedback, null);

        // Initialize views in the custom layout
        RatingBar ratingBar = dialogView.findViewById(R.id.ratingBar);
        EditText etFeedbackComments = dialogView.findViewById(R.id.etFeedbackComments);
        Button btnSubmitFeedback = dialogView.findViewById(R.id.btnSubmitFeedback);

        // Create the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();

        // Set the button click listener
        btnSubmitFeedback.setOnClickListener(v -> {
            float rating = ratingBar.getRating();
            String comments = etFeedbackComments.getText().toString();

            // Handle the feedback submission
            submitFeedback(id, (int) rating, comments);

            // Dismiss the dialog
            dialog.dismiss();
        });

        // Show the dialog
        dialog.show();
    }

    // Function to handle feedback submission
    private void submitFeedback(String rideId, float rating, String comments) {
        HashMap<String, String> data = new HashMap<>();
        data.put("rating", String.valueOf(rating));
        data.put("comments", comments);
        data.put("id", rideId);
        IotApiService apiService = RetrofitClient.getIotApiService();
        apiService.rideFeedback(data).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {

                Toast.makeText(context, "Feedback submitted! Rating: " + rating, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {

            }
        });

    }

    private LatLng getLatLngFromString(String location) {
        return new LatLng(Double.parseDouble(location.split(",")[0]), Double.parseDouble(location.split(",")[0]));
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
        pickupDropStuff();
        getLastRideData();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (handler != null && runnable != null) {
            handler.removeCallbacks(runnable);
        }
    }
}