package com.hotel.reservation.service;

import com.hotel.reservation.model.Booking;
import com.hotel.reservation.model.Room;
import com.hotel.reservation.model.User;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminService {

    private final UserService userService;
    private final RoomService roomService;
    private final BookingService bookingService;

    public AdminService(UserService userService, RoomService roomService, BookingService bookingService) {
        this.userService = userService;
        this.roomService = roomService;
        this.bookingService = bookingService;
    }

    public List<User> getUsers() {
        return userService.getUsers();
    }

    public List<Room> getRooms() {
        return roomService.getRooms();
    }

    public List<Booking> getBookings() {
        return bookingService.getBookings();
    }

    public void deleteUser(String userId) {
        userService.deleteUser(userId);
    }

    public boolean addRoom(String roomId, String type, double price) {
        return roomService.addRoom(new Room(roomId, type, price, true, 1, null, 1));
    }

    public boolean updateRoomDetails(String roomId, double price, boolean available) {
        return roomService.updateRoomDetails(roomId, price, available);
    }

    public boolean deleteRoom(String roomId) {
        return roomService.deleteRoom(roomId);
    }
}
