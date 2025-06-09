package com.projects.solace.common;

import com.projects.solace.models.GeocodingResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface GeocodingAPI {

    // Reverse geocoding endpoint
    @GET("geocode/json")
    Call<GeocodingResponse> getGeocoding(
            @Query("latlng") String latlng,  // Format: "latitude,longitude"
            @Query("key") String apiKey      // API key
    );
}
