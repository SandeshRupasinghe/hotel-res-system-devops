package com.hotel.reservation.controller;

import com.hotel.reservation.model.Booking;
import com.hotel.reservation.model.User;
import com.hotel.reservation.service.BookingService;
import com.hotel.reservation.service.RoomService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class BookingController {

    private final BookingService bookingService;
    private final RoomService roomService;

    public BookingController(BookingService bookingService, RoomService roomService) {
        this.bookingService = bookingService;
        this.roomService = roomService;
    }

    @GetMapping("/booking")
    public String bookingPage(HttpSession session, Model model) {
        User user = SessionHelper.getLoggedInUser(session);
        if (user == null) {
            return "redirect:/login";
        }

        boolean customer = "Customer".equals(user.getRole());
        model.addAttribute("canCreateBooking", customer);
        model.addAttribute("availableRooms", customer ? roomService.getAvailableRooms() : java.util.List.of());
        model.addAttribute("currentUserId", user.getUserId());

        if ("Admin".equals(user.getRole())) {
            model.addAttribute("bookings", bookingService.getBookings());
        } else {
            model.addAttribute("bookings", bookingService.getBookingsByUser(user.getUserId()));
        }

        return "bookings";
    }

    @PostMapping("/booking/create")
    public String createBooking(@ModelAttribute Booking booking,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        User user = SessionHelper.getLoggedInUser(session);
        if (user == null) {
            return "redirect:/login";
        }

        if (!"Customer".equals(user.getRole())) {
            redirectAttributes.addFlashAttribute("error", "Only customer accounts can create bookings.");
            return "redirect:/booking";
        }

        booking.setUserId(user.getUserId());

        boolean created = bookingService.createBooking(booking);
        if (!created) {
            redirectAttributes.addFlashAttribute(
                    "error",
                    "Booking could not be created. Check the dates, booking ID, and room availability."
            );
        }

        return "redirect:/booking";
    }

    @PostMapping("/booking/cancel")
    public String cancelBooking(@RequestParam String bookingId,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        User user = SessionHelper.getLoggedInUser(session);
        if (user == null) {
            return "redirect:/login";
        }

        boolean cancelled = bookingService.cancelBooking(
                bookingId,
                user.getUserId(),
                "Admin".equals(user.getRole())
        );

        if (!cancelled) {
            redirectAttributes.addFlashAttribute("error", "Booking could not be cancelled.");
        }
        return "redirect:/booking";
    }
}
