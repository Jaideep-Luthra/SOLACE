package com.projects.solace.driver;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.projects.solace.R;
import com.projects.solace.common.IotApiService;
import com.projects.solace.common.RetrofitClient;
import com.projects.solace.login.LoginActivity;
import com.projects.solace.models.BookingModel;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DriverActivity extends AppCompatActivity {

    private final Context context = DriverActivity.this;
    private RideAdapter rideAdapter;
    private List<BookingModel> rideList;

    private ListView listView;
    private Handler handler;
    private Runnable refreshRunnable;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver);

        Toolbar toolbar = findViewById(R.id.mToolbar);
        setSupportActionBar(toolbar);
        listView = findViewById(R.id.listView);

        // Initialize the handler
        handler = new Handler(Looper.getMainLooper());

        // Define the Runnable to refresh the list every 10 seconds
        refreshRunnable = new Runnable() {
            @Override
            public void run() {
                refreshList();
                // Post the runnable again after 10 seconds (10000 milliseconds)
                handler.postDelayed(this, 5000);
            }
        };

        // Start the periodic refresh
        handler.post(refreshRunnable);
        IotApiService apiService = RetrofitClient.getIotApiService();
        apiService.getBookingDataByCompletedStatus().enqueue(new Callback<List<BookingModel>>() {
            @Override
            public void onResponse(Call<List<BookingModel>> call, Response<List<BookingModel>> response) {

                rideList = response.body();
                rideAdapter = new RideAdapter(context, rideList);
                listView.setAdapter(rideAdapter);
            }

            @Override
            public void onFailure(Call<List<BookingModel>> call, Throwable t) {

            }
        });
    }

    // Method to fetch the data from your API and update the ListView
    private void refreshList() {
        // Call your API to fetch the latest data
        IotApiService apiService = RetrofitClient.getIotApiService();
        apiService.getBookingDataByCompletedStatus().enqueue(new Callback<List<BookingModel>>() {
            @Override
            public void onResponse(Call<List<BookingModel>> call, Response<List<BookingModel>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<BookingModel> updatedRides = response.body();
                    // Update the adapter with the new data

                    if (rideList == null) {
                        rideList = new ArrayList<>();
                        rideAdapter = new RideAdapter(context, updatedRides);
                        listView.setAdapter(rideAdapter);
                    } else {
                        rideList.clear();  // Clear the old data
                        rideList.addAll(updatedRides);  // Add the new data
                        rideAdapter.notifyDataSetChanged();

                    }

                    Log.d("API Response", "List refreshed successfully");
                } else {
                    Log.d("API Error", "Error code: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<BookingModel>> call, Throwable t) {
                Log.d("API Failure", t.getMessage());
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Stop the handler when the activity is destroyed to prevent memory leaks
        if (handler != null) {
            handler.removeCallbacks(refreshRunnable);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.actionLogout) {
            SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("email", null);
            editor.putString("userType", null);
            editor.putBoolean("isLoggedIn", false);
            editor.apply();

            Intent intent = new Intent(context, LoginActivity.class);
            startActivity(intent);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}