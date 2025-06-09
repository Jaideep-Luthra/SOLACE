package com.projects.solace.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@AllArgsConstructor
@ToString
public class BookingModel {
    private String id;
    private String pickup_location;
    private String pickup_location_name;
    private String drop_location;
    private String drop_location_name;
    private String distance;
    private String money;
    private String user_email;
    private String user_name;
    private String driver_email;
    private String driver_name;
    private int driver_booked;
    private int is_completed;
    private int is_ride_started;
    private int is_completed_shown_user;
    private String created_at;
    private String otp;
    private String status;

}
