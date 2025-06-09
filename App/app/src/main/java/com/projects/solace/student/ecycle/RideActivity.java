package com.projects.solace.student.ecycle;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.projects.solace.R;
import com.projects.solace.common.Constants;
import com.projects.solace.common.IotApiService;
import com.projects.solace.common.RetrofitClient;
import com.projects.solace.models.ApiResponse;
import com.projects.solace.models.BikeStatusResponse;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RideActivity extends AppCompatActivity {

    TextView tvDateTime, tvMoney, tvSlot, tvDuration;
    Button btnSlot;

    LinearLayout llRide;
    String money, slot, duration;
    private int bikeId;

    private Handler handler = new Handler();
    private Runnable runnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ride);
        Toolbar toolbar = findViewById(R.id.mToolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Show the back button
        getSupportActionBar().setHomeButtonEnabled(true); // This ensures the button is clickable

        findId();
        listeners();
        // Define the task
        runnable = new Runnable() {
            @Override
            public void run() {
                getDataFromServer();
                // Schedule the task again after 10 seconds
                handler.postDelayed(this, 10000);
            }
        };

        // Start the task
        handler.post(runnable);

    }

    private void listeners() {
        btnSlot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (btnSlot.getText().toString().contains("Pay")) {
                    sendDataToServer(bikeId, "0");
                } else {
                    sendDataToServer(bikeId, "1");
                }
            }
        });
    }

    private void sendDataToServer(int bikeId, String number) {
        IotApiService iotApiService = RetrofitClient.getIotApiService();
        HashMap<String, String> data = new HashMap<>();
        data.put("user_id", Constants.email);
        data.put("bike_id", String.valueOf(bikeId));
        data.put("status", number);

        iotApiService.bikeRide(data).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (Objects.equals(number, "0")) {
                    Toast.makeText(getApplicationContext(), "Ride completed", Toast.LENGTH_SHORT).show();
                    RideActivity.super.onBackPressed();
                } else {
                    Toast.makeText(getApplicationContext(), "Ride started", Toast.LENGTH_SHORT).show();

                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {

            }
        });
    }


    private void findId() {
        money = getString(R.string.amount);
        slot = getString(R.string.slot);
        duration = getString(R.string.duration);
        tvDuration = findViewById(R.id.tvDuration);
        tvDateTime = findViewById(R.id.tvDateTime);
        tvSlot = findViewById(R.id.tvSlot);
        tvMoney = findViewById(R.id.tvMoney);
        btnSlot = findViewById(R.id.btnSlot);
        llRide = findViewById(R.id.llRide);
        Intent intent = getIntent();
        if (intent != null) {
            bikeId = intent.getIntExtra("bike_id", 0);
        }
        tvSlot.setText(String.format(slot, bikeId));
        tvMoney.setText(String.format(money, "0"));
    }

    private void getDataFromServer() {
        IotApiService iotApiService = RetrofitClient.getIotApiService();
        iotApiService.getBikeRide("1").enqueue(new Callback<BikeStatusResponse>() {
            @Override
            public void onResponse(Call<BikeStatusResponse> call, Response<BikeStatusResponse> response) {
                BikeStatusResponse bikeStatusResponse = response.body();
                if (bikeStatusResponse.getStatus().equals("success")) {
                    BikeStatusResponse.BikeStatus bikeStatus = bikeStatusResponse.getData().get(0);
                    calculate(bikeStatus);
                }
            }

            @Override
            public void onFailure(Call<BikeStatusResponse> call, Throwable t) {

            }
        });
    }


    private void calculate(BikeStatusResponse.BikeStatus bikeStatus) {
        String value = bikeStatus.getStatus();
        String timeStamp = bikeStatus.getUpdated_at();
        if (value.equals("0")) {
            tvMoney.setText(String.format(money, 0));
            btnSlot.setText("Ride");
            llRide.setVisibility(View.GONE);
            return;
        }
        tvDateTime.setText(timeStamp);
        llRide.setVisibility(View.VISIBLE);

        tvMoney.setText(String.format(money, calculate(timeStamp)));
        tvDuration.setText(String.format(duration, calculateDuration(timeStamp)));
        btnSlot.setText(getString(R.string.pay));

        btnSlot.setEnabled(bikeStatus.getIs_lock().equals("0"));

    }

    private long calculateDuration(String timeStamp) {
        long diffInMinutes = -1;
        try {
            SimpleDateFormat format = new SimpleDateFormat("MMMMM d, yyyy, h:mm a");
            Date startDate = format.parse(timeStamp);
            Date endDate = new Date(); // Set end date
            diffInMinutes = TimeUnit.MILLISECONDS.toMinutes(endDate.getTime() - startDate.getTime());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return diffInMinutes;
    }

    private long calculate(String timeStamp) {
        try {
            SimpleDateFormat format = new SimpleDateFormat("MMMMM d, yyyy, h:mm a");
            Date startDate = format.parse(timeStamp);
            Date endDate = new Date(); // Set end date
            System.out.println(startDate);
            System.out.println(endDate);
            long diffInMinutes = TimeUnit.MILLISECONDS.toMinutes(endDate.getTime() - startDate.getTime());
            System.out.println(diffInMinutes);
            long sum = 50;
            if (diffInMinutes > 10) {
                sum += 10 * 20;
                diffInMinutes -= 10;
                if (diffInMinutes > 50) {
                    sum += 50 * 10;
                    diffInMinutes -= 50;

                    sum += diffInMinutes * 5;
                } else {
                    sum += diffInMinutes * 10;
                }
            } else {
                sum += diffInMinutes * 20;

            }

            return sum;

        } catch (ParseException e) {
            e.printStackTrace();
        }
        return -1;
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
    protected void onDestroy() {
        super.onDestroy();
        // Stop the handler when activity is destroyed
        if (handler != null && runnable != null) {
            handler.removeCallbacks(runnable);
        }
    }
}