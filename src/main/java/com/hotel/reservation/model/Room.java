package com.hotel.reservation.model;

public class Room {

    private String roomId;
    private String type;
    private double price;
    private boolean available;
    private int capacity;
    private String facilities;
    private int hotelId;

    public Room(String roomId, String type, double price, boolean available) {
        this(roomId, type, price, available, 1, null, 1);
    }

    public Room(String roomId, String type, double price, boolean available,
                int capacity, String facilities, int hotelId) {
        this.roomId = roomId;
        this.type = type;
        this.price = price;
        this.available = available;
        this.capacity = capacity;
        this.facilities = facilities;
        this.hotelId = hotelId;
    }

    public Room() {
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public String getFacilities() {
        return facilities;
    }

    public void setFacilities(String facilities) {
        this.facilities = facilities;
    }

    public int getHotelId() {
        return hotelId;
    }

    public void setHotelId(int hotelId) {
        this.hotelId = hotelId;
    }
}
