package com.computershop.main.controllers;

import com.computershop.main.entities.Product;
import com.computershop.main.services.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class HomeController {

    @Autowired
    private ProductService productService;

    @GetMapping("/")
    public String home(Model model) {
        try {
            
            List<Product> featuredProducts = productService.getFeaturedProducts(6);
            model.addAttribute("products", featuredProducts);
            
            model.addAttribute("totalProducts", productService.getTotalProducts());
            model.addAttribute("categories", productService.getAllCategoryNames());
            
        } catch (Exception e) {
            
            model.addAttribute("products", List.of());
            model.addAttribute("totalProducts", 0L);
            model.addAttribute("categories", List.of());
        }
        
        return "home";
    }

    @GetMapping("/about")
    public String about() {
        return "about";
    }

    @GetMapping("/categories")
    public String categoriesRedirect() {
        return "redirect:/products";
    }

    @GetMapping("/profile")
    public String profileRedirect() {
        return "redirect:/user/profile";
    }

    @GetMapping("/orders")
    public String ordersRedirect() {
        return "redirect:/user/orders";
    }
}
