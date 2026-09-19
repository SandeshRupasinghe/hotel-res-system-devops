# Hotel Reservation System - MySQL Database Version

This version replaces the original text-file persistence (`data/users.txt`, `data/rooms.txt`, and `data/bookings.txt`) with MySQL and Spring JDBC.

## What changed

- Replaced `FileHandler` and `.txt` persistence with `JdbcTemplate` queries and updates.
- Added MySQL JDBC support.
- Added `HOTEL`, `CUSTOMER`, `STAFF`, `ADMIN`, `ROOM`, `BOOKING`, `BOOKING_ROOM`, and `PAYMENT` database structures.
- Kept string IDs such as `R001`, `B001`, and `testID` so the original website data remains compatible.
- Added check-in/check-out dates and the `BOOKING_ROOM` relationship.
- Added the SQL trigger and revenue stored procedure from the database design.
- Added transaction handling around multi-step booking/cancellation operations.
- Admin write routes now verify the logged-in Admin session.
- Customer cancellation is restricted to the customer's own booking.
- Cancelled bookings remain in booking history instead of disappearing.
- Rooms with booking history are protected from accidental hard deletion.
- The old public room-modification routes were removed; `/rooms` is read-only.

## You do NOT need Maven installed

The repository already contains the Maven Wrapper:

- Windows: `mvnw.cmd`
- macOS/Linux: `./mvnw`

The wrapper downloads the required Maven distribution automatically on its first run. Internet access is therefore required the first time unless Maven/dependencies are already cached.

## What you need locally

- JDK 17 or newer
- MySQL 8.x
- Internet access on the first Maven Wrapper run

Spring Boot 4.0.x uses Java 17 as its baseline, so Java 17+ is the correct target for this project.

## Fast Windows setup

1. Install JDK 17+.
2. Install MySQL Server (MySQL Workbench is optional but convenient).
3. Open `database/setup.sql` in MySQL Workbench and run the whole file once.
4. If your MySQL root account has a password, open PowerShell in the project folder and run:

   `$env:DB_PASSWORD="your_password"`

   If you use another account:

   `$env:DB_USER="your_username"`

5. Start the project either by double-clicking `RUN_WINDOWS.bat`, or from PowerShell:

   `./mvnw.cmd spring-boot:run`

6. Open `http://localhost:8080`.

## Default database connection

`src/main/resources/application.properties` defaults to:

- Database: `hotel_reservation`
- Host: `localhost:3306`
- Username: `root`
- Password: blank

Override with environment variables `DB_URL`, `DB_USER`, and `DB_PASSWORD`.

## Seed login accounts

Admin:

- Email: `admintest@gmail.com`
- Password: `admin`

Customer:

- Email: `test@gmail.com`
- Password: `test`

## Important portfolio note

Passwords are still stored as plain text to remain close to the original coursework authentication design. Before presenting this as production-ready software, the next security improvement should be password hashing (for example BCrypt or Argon2) and a proper authentication/authorization layer such as Spring Security.
