package com.hotel.reservation.service;

import com.hotel.reservation.model.Room;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class RoomService {

    private final JdbcTemplate jdbcTemplate;

    public RoomService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional
    public boolean addRoom(Room room) {
        if (room == null || room.getRoomId() == null || room.getRoomId().isBlank()
                || room.getType() == null || room.getType().isBlank() || room.getPrice() <= 0) {
            return false;
        }

        int hotelId = room.getHotelId() <= 0 ? 1 : room.getHotelId();
        int capacity = room.getCapacity() <= 0 ? 1 : room.getCapacity();

        try {
            int rows = jdbcTemplate.update(
                    "INSERT INTO room (room_id, type, price, capacity, facilities, availability_status, hotel_id) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?)",
                    room.getRoomId(),
                    room.getType(),
                    room.getPrice(),
                    capacity,
                    room.getFacilities(),
                    room.isAvailable() ? "Available" : "Unavailable",
                    hotelId
            );
            return rows == 1;
        } catch (DataIntegrityViolationException ex) {
            return false;
        }
    }

    public ArrayList<Room> getRooms() {
        return new ArrayList<>(queryRooms(
                "SELECT room_id, type, price, capacity, facilities, availability_status, hotel_id FROM room ORDER BY room_id"
        ));
    }

    public ArrayList<Room> getAvailableRooms() {
        return new ArrayList<>(queryRooms(
                "SELECT room_id, type, price, capacity, facilities, availability_status, hotel_id " +
                        "FROM room WHERE availability_status = 'Available' ORDER BY room_id"
        ));
    }

    public Room getRoom(String roomId) {
        if (roomId == null || roomId.isBlank()) {
            return null;
        }

        List<Room> rooms = jdbcTemplate.query(
                "SELECT room_id, type, price, capacity, facilities, availability_status, hotel_id " +
                        "FROM room WHERE room_id = ?",
                this::mapRoom,
                roomId
        );
        return rooms.isEmpty() ? null : rooms.get(0);
    }

    @Transactional
    public boolean updateRoomDetails(String roomId, double price, boolean available) {
        if (roomId == null || roomId.isBlank() || price <= 0) {
            return false;
        }

        int rows = jdbcTemplate.update(
                "UPDATE room SET price = ?, availability_status = ? WHERE room_id = ?",
                price,
                available ? "Available" : "Unavailable",
                roomId
        );
        return rows > 0;
    }

    @Transactional
    public boolean deleteRoom(String roomId) {
        if (roomId == null || roomId.isBlank()) {
            return false;
        }

        Integer bookingLinks = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM booking_room WHERE room_id = ?",
                Integer.class,
                roomId
        );
        if (bookingLinks != null && bookingLinks > 0) {
            return false;
        }

        try {
            return jdbcTemplate.update("DELETE FROM room WHERE room_id = ?", roomId) > 0;
        } catch (DataIntegrityViolationException ex) {
            return false;
        }
    }

    private List<Room> queryRooms(String sql) {
        return jdbcTemplate.query(sql, this::mapRoom);
    }

    private Room mapRoom(java.sql.ResultSet rs, int rowNum) throws java.sql.SQLException {
        return new Room(
                rs.getString("room_id"),
                rs.getString("type"),
                rs.getDouble("price"),
                "Available".equalsIgnoreCase(rs.getString("availability_status")),
                rs.getInt("capacity"),
                rs.getString("facilities"),
                rs.getInt("hotel_id")
        );
    }
}
