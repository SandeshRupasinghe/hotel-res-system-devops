package com.hotel.reservation.controller;

import com.hotel.reservation.model.User;
import com.hotel.reservation.service.AdminService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/admin")
    public String adminPage(HttpSession session, Model model) {
        if (!isAdmin(session)) {
            return "redirect:/login";
        }

        model.addAttribute("users", adminService.getUsers());
        model.addAttribute("rooms", adminService.getRooms());
        model.addAttribute("bookings", adminService.getBookings());
        return "admin";
    }

    @PostMapping("/admin/users/delete")
    public String deleteUser(@RequestParam String userId,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {
        User currentUser = SessionHelper.getLoggedInUser(session);
        if (currentUser == null || !"Admin".equals(currentUser.getRole())) {
            return "redirect:/login";
        }

        if (currentUser.getUserId().equals(userId)) {
            redirectAttributes.addFlashAttribute("error", "You cannot delete the account you are currently using.");
            return "redirect:/admin";
        }

        adminService.deleteUser(userId);
        return "redirect:/admin";
    }

    @PostMapping("/admin/rooms/add")
    public String addRoom(@RequestParam String roomId,
                          @RequestParam String type,
                          @RequestParam double price,
                          HttpSession session,
                          RedirectAttributes redirectAttributes) {
        if (!isAdmin(session)) {
            return "redirect:/login";
        }

        if (!adminService.addRoom(roomId, type, price)) {
            redirectAttributes.addFlashAttribute("error", "Room could not be added. Check the ID and price.");
        }
        return "redirect:/admin";
    }

    @PostMapping("/admin/rooms/update")
    public String updateRoom(@RequestParam String roomId,
                             @RequestParam double price,
                             @RequestParam boolean available,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {
        if (!isAdmin(session)) {
            return "redirect:/login";
        }

        if (!adminService.updateRoomDetails(roomId, price, available)) {
            redirectAttributes.addFlashAttribute("error", "Room could not be updated. Price must be greater than zero.");
        }
        return "redirect:/admin";
    }

    @PostMapping("/admin/rooms/delete")
    public String deleteRoom(@RequestParam String roomId,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {
        if (!isAdmin(session)) {
            return "redirect:/login";
        }

        if (!adminService.deleteRoom(roomId)) {
            redirectAttributes.addFlashAttribute(
                    "error",
                    "Room could not be deleted because it does not exist or has booking history. Mark it unavailable instead."
            );
        }
        return "redirect:/admin";
    }

    private boolean isAdmin(HttpSession session) {
        User user = SessionHelper.getLoggedInUser(session);
        return user != null && "Admin".equals(user.getRole());
    }
}
