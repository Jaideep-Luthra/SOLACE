package com.projects.solace.common;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    public static Retrofit getRetrofitInstance(String baseUrl) {
        // Create a logging interceptor
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY); // Log full request and response bodies

        // Build the OkHttpClient
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor) // Add the logging interceptor
                .build();
        return new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }


    public static IotApiService getIotApiService() {
        return getRetrofitInstance("https://codingprojects.help/").create(IotApiService.class);
    }

    public static GeocodingAPI getGeocodingAPIService() {
        return getRetrofitInstance("https://maps.googleapis.com/maps/api/").create(GeocodingAPI.class);
    }

}

