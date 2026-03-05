package com.computershop.main.controllers;

import com.computershop.main.entities.User;
import com.computershop.main.entities.Order;
import com.computershop.main.services.UserService;
import com.computershop.main.services.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;
    
    @Autowired
    private OrderService orderService;

    @GetMapping("/profile")
    public String profilePage(HttpSession session, Model model) {
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }
        
        try {
            Optional<User> userOpt = userService.getUserById(userId);
            if (userOpt.isPresent()) {
                model.addAttribute("user", userOpt.get());
                return "user/profile"; 
            } else {
                session.invalidate();
                return "redirect:/login";
            }
        } catch (Exception e) {
            model.addAttribute("error", "Đã xảy ra lỗi: " + e.getMessage());
            return "user/profile";
        }
    }

    @PostMapping("/profile/update")
    public String updateProfile(@ModelAttribute("user") User user,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }
        
        try {
            
            Optional<User> currentUserOpt = userService.getUserById(userId);
            if (currentUserOpt.isEmpty()) {
                return "redirect:/login";
            }
            
            User currentUser = currentUserOpt.get();
            
            currentUser.setUsername(user.getUsername());
            currentUser.setEmail(user.getEmail());
            
            userService.updateUser(currentUser);
            
            session.setAttribute("username", currentUser.getUsername());
            
            redirectAttributes.addFlashAttribute("success", "Cập nhật thông tin thành công!");
            return "redirect:/user/profile";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Đã xảy ra lỗi: " + e.getMessage());
            return "redirect:/user/profile";
        }
    }

    @GetMapping("/orders")
    public String ordersPage(HttpSession session, Model model) {
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }
        
        try {
            List<Order> orders = orderService.getOrdersByUserId(userId);
            model.addAttribute("orders", orders);
            return "user/orders"; 
            
        } catch (Exception e) {
            model.addAttribute("error", "Đã xảy ra lỗi khi tải danh sách đơn hàng: " + e.getMessage());
            model.addAttribute("orders", List.of());
            return "user/orders";
        }
    }

    @GetMapping("/orders/{orderId}")
    public String orderDetail(@PathVariable("orderId") Integer orderId,
                             HttpSession session,
                             Model model) {
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }
        
        try {
            Optional<Order> orderOpt = orderService.getOrderById(orderId);
            
            if (orderOpt.isPresent()) {
                Order order = orderOpt.get();
                
                if (!order.getUser().getUserId().equals(userId)) {
                    model.addAttribute("error", "Bạn không có quyền xem đơn hàng này");
                    return "redirect:/user/orders";
                }
                
                model.addAttribute("order", order);
                return "user/order-detail"; 
                
            } else {
                model.addAttribute("error", "Không tìm thấy đơn hàng");
                return "redirect:/user/orders";
            }
            
        } catch (Exception e) {
            model.addAttribute("error", "Đã xảy ra lỗi: " + e.getMessage());
            return "redirect:/user/orders";
        }
    }

    @PostMapping("/change-password")
    public String changePassword(@RequestParam("currentPassword") String currentPassword,
                                @RequestParam("newPassword") String newPassword,
                                @RequestParam("confirmPassword") String confirmPassword,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }
        
        try {
            
            if (newPassword == null || newPassword.length() < 6) {
                redirectAttributes.addFlashAttribute("error", "Mật khẩu mới phải có ít nhất 6 ký tự");
                return "redirect:/user/profile";
            }
            
            if (!newPassword.equals(confirmPassword)) {
                redirectAttributes.addFlashAttribute("error", "Mật khẩu xác nhận không khớp");
                return "redirect:/user/profile";
            }
            
            Optional<User> userOpt = userService.getUserById(userId);
            if (userOpt.isEmpty()) {
                return "redirect:/login";
            }
            
            User user = userOpt.get();
            if (!userService.verifyPassword(currentPassword, user.getPasswordHash())) {
                redirectAttributes.addFlashAttribute("error", "Mật khẩu hiện tại không đúng");
                return "redirect:/user/profile";
            }
            
            userService.changePassword(userId, newPassword);
            
            redirectAttributes.addFlashAttribute("success", "Đổi mật khẩu thành công!");
            return "redirect:/user/profile";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Đã xảy ra lỗi: " + e.getMessage());
            return "redirect:/user/profile";
        }
    }

    @GetMapping("/dashboard")
    public String userDashboard(HttpSession session, Model model) {
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }
        
        try {
            
            Optional<User> userOpt = userService.getUserById(userId);
            if (userOpt.isEmpty()) {
                return "redirect:/login";
            }
            
            model.addAttribute("user", userOpt.get());
            
            List<Order> recentOrders = orderService.getRecentOrdersByUserId(userId, 5);
            model.addAttribute("recentOrders", recentOrders);
            
            model.addAttribute("totalOrders", orderService.countOrdersByUserId(userId));
            model.addAttribute("totalSpent", orderService.getTotalSpentByUserId(userId));
            
            return "user/dashboard"; 
            
        } catch (Exception e) {
            model.addAttribute("error", "Đã xảy ra lỗi: " + e.getMessage());
            return "user/dashboard";
        }
    }
}