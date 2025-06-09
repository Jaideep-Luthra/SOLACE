package com.projects.solace.models;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class DirectionsResponse {

    @SerializedName("routes")
    private List<Route> routes;

    public List<LatLng> getRoutePoints() {
        if (routes != null && !routes.isEmpty()) {
            String polyline = routes.get(0).overviewPolyline.points;
            return decodePolyline(polyline);
        }
        return new ArrayList<>();
    }

    public String getRouteDistance() {
        if (routes != null && !routes.isEmpty()) {
            return routes.get(0).getLegs().get(0).getDistance().getText();
        }
        return "0 km";
    }

    // Polyline decoding method
    private List<LatLng> decodePolyline(String encoded) {
        List<LatLng> polyline = new ArrayList<>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            polyline.add(new LatLng(lat / 1E5, lng / 1E5));
        }
        return polyline;
    }

    public static class Route {
        @SerializedName("overview_polyline")
        OverviewPolyline overviewPolyline;
        @SerializedName("legs")
        private List<Leg> legs;

        public List<Leg> getLegs() {
            return legs;
        }
    }

    public static class OverviewPolyline {
        @SerializedName("points")
        String points;
    }

    public class Leg {
        @SerializedName("distance")
        private Distance distance;

        public Distance getDistance() {
            return distance;
        }
    }

    public class Distance {
        @SerializedName("text")
        private String text;

        public String getText() {
            return text;
        }
    }

}

