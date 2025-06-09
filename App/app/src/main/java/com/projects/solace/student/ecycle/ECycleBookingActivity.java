package com.projects.solace.student.ecycle;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.button.MaterialButton;
import com.projects.solace.R;
import com.projects.solace.common.Constants;
import com.projects.solace.common.IotApiService;
import com.projects.solace.common.RetrofitClient;
import com.projects.solace.models.BikeStatusResponse;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ECycleBookingActivity extends AppCompatActivity {
    private final Context context = ECycleBookingActivity.this;
    Handler handler = new Handler();
    Runnable runnable;
    int delay = 5000;
    private MaterialButton btnBike1;
    private TextView tvBike1Status;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ecycle_booking);
        Toolbar toolbar = findViewById(R.id.mToolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Show the back button
        getSupportActionBar().setHomeButtonEnabled(true); // This ensures the button is clickable

        findId();
        listeners();
        getDataFromServer();
    }

    private void listeners() {
        btnBike1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, RideActivity.class);
                intent.putExtra("bike_id", 1);
                startActivity(intent);
            }
        });
    }

    private void findId() {
        tvBike1Status = findViewById(R.id.tvBike1Status);
        btnBike1 = findViewById(R.id.btnBike1);
        btnBike1.setEnabled(true);
    }

    private void getDataFromServer() {
        IotApiService iotApiService = RetrofitClient.getIotApiService();
        iotApiService.getBikeRide("1").enqueue(new Callback<BikeStatusResponse>() {
            @Override
            public void onResponse(Call<BikeStatusResponse> call, Response<BikeStatusResponse> response) {
                BikeStatusResponse bikeStatusResponse = response.body();
                if (bikeStatusResponse.getStatus().equals("success")) {
                    BikeStatusResponse.BikeStatus bikeStatus = bikeStatusResponse.getData().get(0);
                    if (bikeStatus.getUser_id().equals(Constants.email)) {
                        btnBike1.setEnabled(true);
                        if (bikeStatus.getStatus().equals("1")) {
                            long money = calculate(bikeStatus.getUpdated_at());
                            btnBike1.setText("Pay ₹" + money);
                            tvBike1Status.setText("Bike in use");
                        } else {
                            btnBike1.setText("Ride");
                            tvBike1Status.setText("");
                        }
                    } else {
                        if (bikeStatus.getStatus().equals("1")) {
                            btnBike1.setEnabled(false);
                            tvBike1Status.setText("Bike not available");
                        }else{
                            btnBike1.setText("Ride");
                            tvBike1Status.setText("");
                            btnBike1.setEnabled(true);
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<BikeStatusResponse> call, Throwable t) {

            }
        });
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
    protected void onResume() {
        handler.postDelayed(runnable = new Runnable() {
            public void run() {
                handler.postDelayed(runnable, delay);
                getDataFromServer();
            }
        }, delay);
        super.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
        getDataFromServer();

    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(runnable); //stop handler when activity not visible super.onPause();
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
}