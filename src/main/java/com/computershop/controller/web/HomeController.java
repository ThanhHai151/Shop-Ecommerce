package com.computershop.controller.web;

import com.computershop.service.impl.ProductServiceImpl;
import com.computershop.service.impl.CategoryServiceImpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.HttpSession;
import java.util.List;

/**
 * Controller for the home page and public routes.
 * Handles displaying the home page, about page, etc.
 */
@Controller
public class HomeController {

    @Autowired
    private ProductServiceImpl productService;

    @Autowired
    private CategoryServiceImpl categoryService;

    /**
     * Displays the home page with featured products.
     *
     * @param model the model
     * @param session the HTTP session
     * @return home page view
     */
    @GetMapping({"/", "/home"})
    public String home(Model model, HttpSession session) {
        try {
            // Get featured products (limit to 8)
            model.addAttribute("featuredProducts", productService.getFeaturedProducts(8));

            // Get all categories
            model.addAttribute("categories", categoryService.getAllCategories());

            // Get cart item count for session
            Integer userId = (Integer) session.getAttribute("userId");
            if (userId != null) {
                // CartService cartService;
                // model.addAttribute("cartItemCount", cartService.getCartItemCount(userId));
            }

            return "home";
        } catch (Exception e) {
            model.addAttribute("error", "Failed to load home page: " + e.getMessage());
            return "home";
        }
    }

    /**
     * Displays the about page.
     *
     * @return about page view
     */
    @GetMapping("/about")
    public String about() {
        return "about";
    }
}
