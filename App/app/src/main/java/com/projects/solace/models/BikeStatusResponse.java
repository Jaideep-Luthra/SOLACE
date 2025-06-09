package com.projects.solace.models;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@AllArgsConstructor
public class BikeStatusResponse {
    private String status;
    private String message;
    private List<BikeStatus> data;


    @Getter
    @Setter
    @ToString
    @AllArgsConstructor
    public static class BikeStatus {
        private String bike_id;
        private String user_id;
        private String status;
        private String is_lock;
        private String updated_at;
    }
}
