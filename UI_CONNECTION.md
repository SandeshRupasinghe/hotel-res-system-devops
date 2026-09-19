# Frontend to Database Connection

## Current MVC flow

```text
HTML / Thymeleaf form
        |
        v
Spring MVC Controller
        |
        v
Service layer
        |
        v
Spring JdbcTemplate
        |
        v
MySQL database
```

The old `FileHandler -> data/*.txt` persistence is no longer used.

## Example: Register

1. The user opens `/register`.
2. The form posts to `POST /register`.
3. `UserController` forces the role to `Customer` and calls `UserService.registerUser()`.
4. `UserService` inserts the account into `app_user` and the matching subtype row into `customer`.
5. The controller redirects to `/login` after a successful insert.

## Example: Login

1. The form posts email and password to `POST /login`.
2. `UserService.loginUser()` queries `app_user` through `JdbcTemplate`.
3. A successful login stores the `User` object in the HTTP session.

## Example: Book a room

1. A logged-in Customer opens `/booking`.
2. `BookingController` loads rooms through `RoomService`.
3. The form posts to `POST /booking/create`.
4. The controller takes the user ID from the authenticated session, not from submitted form data.
5. `BookingService` checks the dates, duplicate booking ID, room state, and overlapping reservations.
6. The service inserts into `booking` and `booking_room` in one transaction.
7. The MySQL trigger updates the room's availability status to `Booked`.

## Access levels

- Customer: browse rooms, create bookings, view own bookings, cancel own bookings.
- Admin: view users/rooms/all bookings and manage room records from `/admin`.
- Staff: view their booking page in read-only mode; staff-specific operational features are not implemented yet.

Admin write endpoints check the session role on every request. The public `/rooms` controller is read-only.

## Database files

- `database/schema.sql`: creates the relational schema, constraints, trigger, and procedure.
- `database/seed.sql`: loads sample hotel, users, rooms, and one booking.
- `database/setup.sql`: convenience file containing both schema and seed data for a fresh local setup.
