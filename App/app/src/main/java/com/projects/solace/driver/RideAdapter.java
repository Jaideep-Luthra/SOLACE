package com.projects.solace.driver;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.projects.solace.R;
import com.projects.solace.common.Constants;
import com.projects.solace.common.IotApiService;
import com.projects.solace.common.RetrofitClient;
import com.projects.solace.models.ApiResponse;
import com.projects.solace.models.BookingModel;

import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RideAdapter extends BaseAdapter {
    private final Context context;
    private final List<BookingModel> rideList;

    public RideAdapter(Context context, List<BookingModel> rideList) {
        this.context = context;
        this.rideList = rideList;
    }

    @Override
    public int getCount() {
        return rideList.size();
    }

    @Override
    public Object getItem(int position) {
        return rideList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(R.layout.ride_item, parent, false);
        }

        // Get the current ride object
        BookingModel ride = rideList.get(position);

        // Set data to the views
        TextView pickupLocation = convertView.findViewById(R.id.pickup_location);
        TextView dropLocation = convertView.findViewById(R.id.drop_location);
        TextView distance = convertView.findViewById(R.id.distance);
        TextView money = convertView.findViewById(R.id.money);
        TextView userName = convertView.findViewById(R.id.user_name);
        Button btnBooked = convertView.findViewById(R.id.btnBooked);
        pickupLocation.setText(ride.getPickup_location_name());
        dropLocation.setText(ride.getDrop_location_name());
        distance.setText(ride.getDistance());
        money.setText(ride.getMoney());
        userName.setText(ride.getUser_name());

        if (ride.getDriver_booked() == 1) {
            if (ride.getIs_ride_started() == 1) {
                btnBooked.setText("Complete ride");
            } else {
                btnBooked.setText("Start ride");
            }

        } else {
            btnBooked.setText("Book");
        }

        btnBooked.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (btnBooked.getText().toString().equals("Book")) {
                    bookRide(ride.getId());
                    btnBooked.setText("Start ride");
                } else if (btnBooked.getText().toString().equals("Start ride")) {
                    showOtpDialog(ride.getId(), ride.getOtp(), btnBooked);

                } else if (btnBooked.getText().toString().equals("Complete ride")) {
                    completeRide(ride.getId());

                }
            }
        });
        return convertView;
    }

    private void bookRide(String id) {

        IotApiService apiService = RetrofitClient.getIotApiService();
        HashMap<String, String> data = new HashMap<>();
        data.put("id", id);
        data.put("driver_email", Constants.email);
        data.put("driver_name", Constants.name);
        data.put("driver_booked", "1");
        apiService.bookRide(data).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.isSuccessful()) {
                    Log.d("API Response", response.body().toString());
                } else {
                    Log.d("API Error", "Error code: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                Log.d("API Failure", t.getMessage());
            }
        });
    }

    private void completeRide(String id) {
        IotApiService apiService = RetrofitClient.getIotApiService();
        HashMap<String, String> data = new HashMap<>();
        data.put("id", id);
        apiService.completeRide(data).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.isSuccessful()) {

                    Log.d("API Response", response.body().toString());

                } else {
                    Log.d("API Error", "Error code: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {

                Log.d("API Failure", t.getMessage());
            }
        });
    }

    private void showOtpDialog(String rideId, String rideOtp, Button btnBooked) {
        // Create an EditText for OTP input
        final EditText otpEditText = new EditText(context);
        otpEditText.setHint("Enter 4-digit OTP");
        otpEditText.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);


        // Show the dialog
        new AlertDialog.Builder(context)
                .setTitle("OTP Verification")
                .setMessage("Please enter your 4-digit OTP")
                .setView(otpEditText)
                .setPositiveButton("Verify", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String otp = otpEditText.getText().toString().trim();
                        if (isValidOtp(otp) && otp.equals(rideOtp)) {
                            Toast.makeText(context, "OTP Verified!", Toast.LENGTH_SHORT).show();
                            startRide(rideId, btnBooked);
                        } else {
                            Toast.makeText(context, "Invalid OTP! Please try again.", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton("Cancel", null)  // Optionally add a cancel button
                .show();
    }

    private void startRide(String id, Button btnBooked) {
        IotApiService apiService = RetrofitClient.getIotApiService();
        HashMap<String, String> data = new HashMap<>();
        data.put("id", id);
        apiService.startRide(data).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.isSuccessful()) {

                    btnBooked.setText("Complete ride");
                    Log.d("API Response", response.body().toString());

                } else {
                    Log.d("API Error", "Error code: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {

                Log.d("API Failure", t.getMessage());
            }
        });
    }

    private boolean isValidOtp(String otp) {
        // Validate OTP: should be 4 digits long
        return !TextUtils.isEmpty(otp) && otp.length() == 4 && TextUtils.isDigitsOnly(otp);
    }

}
