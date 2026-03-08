package com.computershop.controller.web;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.computershop.main.entities.Order;
import com.computershop.main.entities.User;
import com.computershop.service.impl.OrderServiceImpl;
import com.computershop.service.impl.UserServiceImpl;

import jakarta.servlet.http.HttpSession;

/**
 * Controller for user-related pages.
 * Handles profile, orders, etc.
 */
@Controller
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserServiceImpl userService;

    @Autowired
    private OrderServiceImpl orderService;

    /**
     * Displays the user profile page.
     */
    @GetMapping("/profile")
    public String profile(HttpSession session, Model model) {
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }

        try {
            User user = userService.getUserById(userId).orElse(null);
            if (user != null) {
                model.addAttribute("user", user);
            } else {
                model.addAttribute("error", "Không tìm thấy thông tin tài khoản");
            }
            return "user/profile";
        } catch (Exception e) {
            model.addAttribute("error", "Không thể tải thông tin tài khoản: " + e.getMessage());
            return "user/profile";
        }
    }

    /**
     * Updates user profile.
     */
    @PostMapping("/profile/update")
    public String updateProfile(
            HttpSession session,
            @RequestParam String email,
            Model model) {

        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }

        try {
            User user = userService.getUserById(userId).orElse(null);
            if (user != null) {
                user.setEmail(email);
                userService.updateUser(userId, user);
                model.addAttribute("success", "Cập nhật thông tin thành công!");
            }
        } catch (Exception e) {
            model.addAttribute("error", "Cập nhật thất bại: " + e.getMessage());
        }

        return "redirect:/user/profile";
    }

    /**
     * Displays the user orders history page.
     */
    @GetMapping("/orders")
    public String orders(HttpSession session, Model model) {
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }

        try {
            List<Order> orders = orderService.getOrdersWithDetailsForUser(userId);
            model.addAttribute("orders", orders);
        } catch (Exception e) {
            model.addAttribute("error", "Không thể tải đơn hàng: " + e.getMessage());
            model.addAttribute("orders", List.of());
        }

        return "user/orders";
    }

    /**
     * Displays order detail page for the logged-in user.
     */
    @GetMapping("/orders/{id}")
    @Transactional(readOnly = true)
    public String orderDetail(@PathVariable("id") Integer orderId,
                              HttpSession session,
                              Model model) {
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }

        try {
            var orderOpt = orderService.getOrderById(orderId);
            if (orderOpt.isPresent()) {
                Order order = orderOpt.get();
                // Security: ensure order belongs to the logged-in user
                if (!order.getUser().getUserId().equals(userId)) {
                    return "redirect:/user/orders";
                }
                model.addAttribute("order", order);
                return "user/order-detail";
            } else {
                model.addAttribute("error", "Không tìm thấy đơn hàng");
                return "redirect:/user/orders";
            }
        } catch (Exception e) {
            model.addAttribute("error", "Không thể tải chi tiết đơn hàng: " + e.getMessage());
            return "redirect:/user/orders";
        }
    }

    /**
     * Displays all products the user has ever ordered.
     */
    @GetMapping("/ordered-products")
    @Transactional(readOnly = true)
    public String orderedProducts(HttpSession session, Model model) {
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }

        try {
            List<Object[]> orderedProducts = orderService.getOrderedProductsByUserId(userId);
            model.addAttribute("orderedProducts", orderedProducts);
            model.addAttribute("totalProducts", orderedProducts.size());
        } catch (Exception e) {
            model.addAttribute("error", "Không thể tải sản phẩm đã mua: " + e.getMessage());
            model.addAttribute("orderedProducts", List.of());
            model.addAttribute("totalProducts", 0);
        }

        return "user/ordered-products";
    }

    /**
     * Displays the user dashboard page.
     */
    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }

        try {
            List<Order> recentOrders = orderService.getRecentOrdersByUserId(userId, 5);
            model.addAttribute("recentOrders", recentOrders);
        } catch (Exception e) {
            model.addAttribute("recentOrders", List.of());
        }

        model.addAttribute("userId", userId);
        return "user/dashboard";
    }
}
