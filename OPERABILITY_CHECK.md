# Operability Check

Checks performed on this refactor:

- Project structure inspected against the original text-file version.
- Java source compiled locally against minimal API stubs using Java 17 language rules to detect syntax/type mismatches inside project code.
- Controller routes checked against the Thymeleaf form actions.
- Model properties checked against the Thymeleaf expressions used by the pages.
- SQL table/foreign-key relationships checked against the JDBC queries in the service layer.
- Maven Wrapper files confirmed present, so a global Maven installation is not required.
- Spring Boot 4.0.6 and Maven 3.9.15 are real released versions.

Functional issues found and corrected during the audit:

- Non-admin POST requests could previously call admin room/user mutation endpoints.
- Public `/rooms` POST endpoints could modify database records without admin authorization.
- Any logged-in user could previously attempt to cancel another user's booking by posting its ID.
- Cancelling deleted the `booking_room` row, which made cancelled bookings disappear from booking history.
- An Admin or Staff account could submit the customer booking form and hit a foreign-key failure.
- Rooms with booking history could cause a foreign-key exception when hard deleted.
- Invalid date text could throw a parsing exception instead of returning a normal booking error.
- Re-running `schema.sql` could fail because `GetTotalRevenue` already existed.
- `UI_CONNECTION.md` still described the obsolete `FileHandler -> .txt` architecture.

Environment limitation of this audit:

The sandbox has Java 21 but does not have Maven or MySQL installed, and external Maven downloads are blocked. Therefore a true Spring Boot + live MySQL end-to-end launch could not be performed here. The final launch must still be verified on a machine with MySQL running and first-run Maven Wrapper network access.
