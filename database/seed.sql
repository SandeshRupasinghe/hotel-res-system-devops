USE hotel_reservation;

INSERT INTO hotel (hotel_id, location, contact_details, description) VALUES
(1, 'Colombo', '0112345678', 'Main hotel used by the original web application');

INSERT INTO app_user (user_id, name, email, password, role) VALUES
('admin', 'admin', 'admintest@gmail.com', 'admin', 'Admin'),
('testID', 'testName', 'test@gmail.com', 'test', 'Customer');

INSERT INTO admin (user_id) VALUES ('admin');
INSERT INTO customer (user_id) VALUES ('testID');

INSERT INTO room (room_id, type, price, capacity, facilities, availability_status, hotel_id) VALUES
('R001', 'Single Room', 100.00, 1, NULL, 'Available', 1),
('R002', 'Double Room', 150.00, 2, NULL, 'Available', 1),
('R003', 'Twin Room', 75.00, 2, NULL, 'Booked', 1),
('R004', 'Family Suite', 120.00, 4, NULL, 'Available', 1),
('R006', 'Triple Room / Quad Room', 10000.00, 4, NULL, 'Available', 1),
('R005', 'Luxury room', 10000.00, 2, NULL, 'Available', 1),
('r008', 'oop', 1000.00, 1, NULL, 'Available', 1);

-- Only bookings belonging to an existing customer are migrated from the text files.
INSERT INTO booking (booking_id, check_in_date, check_out_date, status, booking_date, user_id) VALUES
('B001', '2026-06-23', '2026-06-24', 'Confirmed', '2026-06-23', 'testID');
INSERT INTO booking_room (booking_id, room_id) VALUES ('B001', 'R003');
