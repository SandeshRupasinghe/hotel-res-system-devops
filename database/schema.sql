CREATE DATABASE IF NOT EXISTS hotel_reservation;
USE hotel_reservation;

DROP PROCEDURE IF EXISTS GetTotalRevenue;
DROP TRIGGER IF EXISTS trg_after_booking_room_insert;

DROP TABLE IF EXISTS payment;
DROP TABLE IF EXISTS booking_room;
DROP TABLE IF EXISTS booking;
DROP TABLE IF EXISTS room;
DROP TABLE IF EXISTS admin;
DROP TABLE IF EXISTS staff;
DROP TABLE IF EXISTS customer;
DROP TABLE IF EXISTS hotel;
DROP TABLE IF EXISTS app_user;

CREATE TABLE app_user (
    user_id VARCHAR(50) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    phone VARCHAR(20),
    role VARCHAR(20) NOT NULL,
    CONSTRAINT chk_user_role CHECK (role IN ('Customer', 'Staff', 'Admin'))
);

CREATE TABLE customer (
    user_id VARCHAR(50) PRIMARY KEY,
    CONSTRAINT fk_customer_user
        FOREIGN KEY (user_id) REFERENCES app_user(user_id)
        ON DELETE CASCADE
);

CREATE TABLE staff (
    user_id VARCHAR(50) PRIMARY KEY,
    CONSTRAINT fk_staff_user
        FOREIGN KEY (user_id) REFERENCES app_user(user_id)
        ON DELETE CASCADE
);

CREATE TABLE admin (
    user_id VARCHAR(50) PRIMARY KEY,
    CONSTRAINT fk_admin_user
        FOREIGN KEY (user_id) REFERENCES app_user(user_id)
        ON DELETE CASCADE
);

CREATE TABLE hotel (
    hotel_id INT PRIMARY KEY,
    location VARCHAR(100) NOT NULL,
    contact_details VARCHAR(200),
    description TEXT
);

CREATE TABLE room (
    room_id VARCHAR(50) PRIMARY KEY,
    type VARCHAR(50) NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    capacity INT NOT NULL DEFAULT 1,
    facilities TEXT,
    availability_status VARCHAR(20) NOT NULL DEFAULT 'Available',
    hotel_id INT NOT NULL,
    CONSTRAINT chk_room_price CHECK (price > 0),
    CONSTRAINT chk_room_capacity CHECK (capacity > 0),
    CONSTRAINT chk_room_status CHECK (availability_status IN ('Available', 'Booked', 'Unavailable')),
    CONSTRAINT fk_room_hotel
        FOREIGN KEY (hotel_id) REFERENCES hotel(hotel_id)
);

CREATE TABLE booking (
    booking_id VARCHAR(50) PRIMARY KEY,
    check_in_date DATE NOT NULL,
    check_out_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'Confirmed',
    booking_date DATE NOT NULL,
    user_id VARCHAR(50) NOT NULL,
    CONSTRAINT chk_booking_dates CHECK (check_in_date < check_out_date),
    CONSTRAINT chk_booking_status CHECK (status IN ('Pending', 'Confirmed', 'Cancelled')),
    CONSTRAINT fk_booking_customer
        FOREIGN KEY (user_id) REFERENCES customer(user_id)
        ON DELETE CASCADE
);

CREATE TABLE booking_room (
    booking_id VARCHAR(50) NOT NULL,
    room_id VARCHAR(50) NOT NULL,
    PRIMARY KEY (booking_id, room_id),
    CONSTRAINT fk_booking_room_booking
        FOREIGN KEY (booking_id) REFERENCES booking(booking_id)
        ON DELETE CASCADE,
    CONSTRAINT fk_booking_room_room
        FOREIGN KEY (room_id) REFERENCES room(room_id)
);

CREATE TABLE payment (
    payment_id INT PRIMARY KEY AUTO_INCREMENT,
    payment_date DATE NOT NULL,
    payment_method VARCHAR(20),
    payment_status VARCHAR(20),
    booking_id VARCHAR(50) NOT NULL,
    CONSTRAINT chk_payment_status CHECK (payment_status IN ('Pending', 'Completed', 'Failed')),
    CONSTRAINT fk_payment_booking
        FOREIGN KEY (booking_id) REFERENCES booking(booking_id)
        ON DELETE CASCADE
);

DELIMITER //
CREATE TRIGGER trg_after_booking_room_insert
AFTER INSERT ON booking_room
FOR EACH ROW
BEGIN
    UPDATE room
    SET availability_status = 'Booked'
    WHERE room_id = NEW.room_id;
END //
DELIMITER ;

DELIMITER //
CREATE PROCEDURE GetTotalRevenue()
BEGIN
    SELECT h.location, SUM(r.price) AS revenue
    FROM hotel h
    JOIN room r ON h.hotel_id = r.hotel_id
    JOIN booking_room br ON r.room_id = br.room_id
    JOIN booking b ON br.booking_id = b.booking_id
    WHERE b.status <> 'Cancelled'
    GROUP BY h.location;
END //
DELIMITER ;
