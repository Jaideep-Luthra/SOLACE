package com.projects.solace.common;

import com.google.android.gms.maps.model.LatLng;

public class Constants {
    public static final String API_KEY = "AIzaSyA-Wq9y8nWdWMkkR3YhBnfSgUL-73g-KPg";
    public static LatLng pickupLatLng, dropLatLng;
    public static String dropLocationName, pickupLocationName;
    public static String distance;
    public static String name, email;
    public static String amount;
    public static String driverEmail;
    public static String driverName;

    // Method to get the enum value from a string
    public static UserType getUserTypeFromString(String userTypeString) {
        try {
            // Convert the string to the corresponding enum constant
            return UserType.valueOf(userTypeString.toUpperCase()); // Ensures case insensitivity
        } catch (IllegalArgumentException e) {
            // Handle the case where the string does not match any enum constants
            return null; // or throw an exception, depending on your use case
        }
    }

    public enum UserType {
        DRIVER, STUDENT
    }

}
