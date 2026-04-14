package com.catenai.hotelos.legacy.data.model;

public class DeviceSessionResponse {
    private boolean success;
    private SessionData data;

    public DeviceSessionResponse() {
    }

    public DeviceSessionResponse(boolean success, SessionData data) {
        this.success = success;
        this.data = data;
    }

    public boolean isSuccess() {
        return success;
    }

    public SessionData getData() {
        return data;
    }

    public static class SessionData {
        private String deviceId;
        private String name;
        private String hotelId;
        private String hotelName;
        private String bindStatus;
        private String expiresAt;

        public SessionData() {
        }

        public SessionData(
                String deviceId,
                String name,
                String hotelId,
                String hotelName,
                String bindStatus,
                String expiresAt
        ) {
            this.deviceId = deviceId;
            this.name = name;
            this.hotelId = hotelId;
            this.hotelName = hotelName;
            this.bindStatus = bindStatus;
            this.expiresAt = expiresAt;
        }

        public String getDeviceId() {
            return deviceId;
        }

        public String getName() {
            return name;
        }

        public String getHotelId() {
            return hotelId;
        }

        public String getHotelName() {
            return hotelName;
        }

        public String getBindStatus() {
            return bindStatus;
        }

        public String getExpiresAt() {
            return expiresAt;
        }
    }
}
