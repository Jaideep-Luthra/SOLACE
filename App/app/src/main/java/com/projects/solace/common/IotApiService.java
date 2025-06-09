package com.projects.solace.common;

import com.projects.solace.models.ApiResponse;
import com.projects.solace.models.BikeStatusResponse;
import com.projects.solace.models.BookingModel;
import com.projects.solace.models.DataModifiedModel;
import com.projects.solace.models.ResponseModel;

import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface IotApiService {

    @GET("/api/get_values.php")
    Call<Integer> get_field(@Query("id") int id, @Query("field") int field);

    @GET("/api/get_values.php")
    Call<DataModifiedModel> get_fields(@Query("id") int id, @Query("os") String os);

    @GET("/api/set.php")
    Call<ResponseModel> set_fields(@Query("id") int id,
                                   @Query("field") int field,
                                   @Query("value") String value);

    @POST("/solace/saveData.php")
    Call<ResponseModel> saveFcmToken(@Body HashMap<String, String> data);

    @POST("/solace/bookRickshaw.php")
    Call<ResponseModel> bookRickshaw(@Body HashMap<String, String> data);

    @POST("/solace/getBookingDataById.php")
    Call<BookingModel> getBookingDataById(@Query("id") String id);

    @POST("/solace/getLastBookingDataByEmail.php")
    Call<BookingModel> getLastBookingDataByEmail(@Query("user_email") String userEmail);

    @POST("/solace/getBookingDataByCompletedStatus.php")
    Call<List<BookingModel>> getBookingDataByCompletedStatus();

    @POST("/solace/rideFeedback.php")
    Call<ApiResponse> rideFeedback(@Body HashMap<String, String> data);

    @POST("/solace/completeRide.php")
    Call<ApiResponse> completeRide(@Body HashMap<String, String> data);

    @POST("/solace/startRide.php")
    Call<ApiResponse> startRide(@Body HashMap<String, String> data);

    @POST("/solace/bookRide.php")
    Call<ApiResponse> bookRide(@Body HashMap<String, String> data);


    @POST("/solace/bikeRide.php")
    Call<ApiResponse> bikeRide(@Body HashMap<String, String> data);

    @GET("/solace/bikeRide.php")
    Call<BikeStatusResponse> getBikeRide(@Query("bike_id") String bikeId);
}