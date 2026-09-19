package com.hotel.reservation.service;

import com.hotel.reservation.model.Booking;
import com.hotel.reservation.model.Room;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

@Service
public class BookingService {

    private final JdbcTemplate jdbcTemplate;
    private final RoomService roomService;

    public BookingService(JdbcTemplate jdbcTemplate, RoomService roomService) {
        this.jdbcTemplate = jdbcTemplate;
        this.roomService = roomService;
    }

    @Transactional
    public boolean createBooking(Booking booking) {
        if (booking == null
                || isBlank(booking.getBookingId())
                || isBlank(booking.getUserId())
                || isBlank(booking.getRoomId())
                || isBlank(booking.getCheckInDate())
                || isBlank(booking.getCheckOutDate())) {
            return false;
        }

        Room room = roomService.getRoom(booking.getRoomId());
        if (room == null || !room.isAvailable()) {
            return false;
        }

        final LocalDate checkIn;
        final LocalDate checkOut;
        try {
            checkIn = LocalDate.parse(booking.getCheckInDate());
            checkOut = LocalDate.parse(booking.getCheckOutDate());
        } catch (DateTimeParseException ex) {
            return false;
        }

        if (!checkIn.isBefore(checkOut)) {
            return false;
        }

        Integer duplicateId = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM booking WHERE booking_id = ?",
                Integer.class,
                booking.getBookingId()
        );
        if (duplicateId != null && duplicateId > 0) {
            return false;
        }

        Integer overlap = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM booking b " +
                        "JOIN booking_room br ON b.booking_id = br.booking_id " +
                        "WHERE br.room_id = ? AND b.status <> 'Cancelled' " +
                        "AND b.check_in_date < ? AND b.check_out_date > ?",
                Integer.class,
                booking.getRoomId(),
                booking.getCheckOutDate(),
                booking.getCheckInDate()
        );
        if (overlap != null && overlap > 0) {
            return false;
        }

        booking.setStatus("Confirmed");
        booking.setBookingDate(LocalDate.now().toString());

        jdbcTemplate.update(
                "INSERT INTO booking (booking_id, check_in_date, check_out_date, status, booking_date, user_id) " +
                        "VALUES (?, ?, ?, ?, ?, ?)",
                booking.getBookingId(),
                booking.getCheckInDate(),
                booking.getCheckOutDate(),
                booking.getStatus(),
                booking.getBookingDate(),
                booking.getUserId()
        );

        jdbcTemplate.update(
                "INSERT INTO booking_room (booking_id, room_id) VALUES (?, ?)",
                booking.getBookingId(),
                booking.getRoomId()
        );
        return true;
    }

    public ArrayList<Booking> getBookings() {
        return new ArrayList<>(queryBookings(
                "SELECT b.booking_id, b.user_id, br.room_id, b.check_in_date, b.check_out_date, " +
                        "b.status, b.booking_date " +
                        "FROM booking b JOIN booking_room br ON b.booking_id = br.booking_id " +
                        "ORDER BY b.booking_date DESC, b.booking_id"
        ));
    }

    public ArrayList<Booking> getBookingsByUser(String userId) {
        if (isBlank(userId)) {
            return new ArrayList<>();
        }

        return new ArrayList<>(jdbcTemplate.query(
                "SELECT b.booking_id, b.user_id, br.room_id, b.check_in_date, b.check_out_date, " +
                        "b.status, b.booking_date " +
                        "FROM booking b JOIN booking_room br ON b.booking_id = br.booking_id " +
                        "WHERE b.user_id = ? ORDER BY b.booking_date DESC, b.booking_id",
                this::mapBooking,
                userId
        ));
    }

    @Transactional
    public boolean cancelBooking(String bookingId, String requesterUserId, boolean admin) {
        if (isBlank(bookingId) || isBlank(requesterUserId)) {
            return false;
        }

        List<Booking> matches = jdbcTemplate.query(
                "SELECT b.booking_id, b.user_id, br.room_id, b.check_in_date, b.check_out_date, " +
                        "b.status, b.booking_date " +
                        "FROM booking b JOIN booking_room br ON b.booking_id = br.booking_id " +
                        "WHERE b.booking_id = ?",
                this::mapBooking,
                bookingId
        );

        if (matches.isEmpty()) {
            return false;
        }

        Booking booking = matches.get(0);
        if (!admin && !requesterUserId.equals(booking.getUserId())) {
            return false;
        }
        if ("Cancelled".equalsIgnoreCase(booking.getStatus())) {
            return true;
        }

        int rows = jdbcTemplate.update(
                "UPDATE booking SET status = 'Cancelled' WHERE booking_id = ?",
                bookingId
        );
        if (rows == 0) {
            return false;
        }

        Integer otherActiveBookings = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM booking b " +
                        "JOIN booking_room br ON b.booking_id = br.booking_id " +
                        "WHERE br.room_id = ? AND b.booking_id <> ? AND b.status <> 'Cancelled'",
                Integer.class,
                booking.getRoomId(),
                bookingId
        );

        if (otherActiveBookings == null || otherActiveBookings == 0) {
            jdbcTemplate.update(
                    "UPDATE room SET availability_status = 'Available' " +
                            "WHERE room_id = ? AND availability_status = 'Booked'",
                    booking.getRoomId()
            );
        }

        return true;
    }

    private List<Booking> queryBookings(String sql) {
        return jdbcTemplate.query(sql, this::mapBooking);
    }

    private Booking mapBooking(java.sql.ResultSet rs, int rowNum) throws java.sql.SQLException {
        return new Booking(
                rs.getString("booking_id"),
                rs.getString("user_id"),
                rs.getString("room_id"),
                rs.getDate("check_in_date").toLocalDate().toString(),
                rs.getDate("check_out_date").toLocalDate().toString(),
                rs.getString("status"),
                rs.getDate("booking_date").toLocalDate().toString()
        );
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
