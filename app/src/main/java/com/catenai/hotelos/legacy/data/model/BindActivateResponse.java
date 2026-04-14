package com.catenai.hotelos.legacy.data.model;

public class BindActivateResponse {
    private int code;
    private String status;
    private BindData data;

    public BindActivateResponse() {
    }

    public BindActivateResponse(int code, String status, BindData data) {
        this.code = code;
        this.status = status;
        this.data = data;
    }

    public int getCode() {
        return code;
    }

    public String getStatus() {
        return status;
    }

    public BindData getData() {
        return data;
    }

    public static class BindData {
        private String token;
        private String hotelId;
        private String hotelName;
        private String deviceId;
        private String name;

        public BindData() {
        }

        public BindData(String token, String hotelId, String hotelName, String deviceId, String name) {
            this.token = token;
            this.hotelId = hotelId;
            this.hotelName = hotelName;
            this.deviceId = deviceId;
            this.name = name;
        }

        public String getToken() {
            return token;
        }

        public String getHotelId() {
            return hotelId;
        }

        public String getHotelName() {
            return hotelName;
        }

        public String getDeviceId() {
            return deviceId;
        }

        public String getName() {
            return name;
        }
    }
}
