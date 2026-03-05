package com.computershop.main.controllers;

import com.computershop.main.entities.User;
import com.computershop.main.entities.Product;
import com.computershop.main.entities.Category;
import com.computershop.main.entities.Order;
import com.computershop.main.services.UserService;
import com.computershop.main.services.ProductService;
import com.computershop.main.services.CategoryService;
import com.computershop.main.services.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UserService userService;
    
    @Autowired
    private ProductService productService;
    
    @Autowired
    private CategoryService categoryService;
    
    @Autowired
    private OrderService orderService;

    private boolean isAdmin(HttpSession session) {
        String role = (String) session.getAttribute("role");
        return "admin".equals(role);
    }

    @GetMapping
    public String adminRoot(HttpSession session) {
        if (!isAdmin(session)) {
            return "redirect:/login";
        }
        return "redirect:/admin/dashboard";
    }

    @GetMapping("/dashboard")
    public String adminDashboard(HttpSession session, Model model) {
        if (!isAdmin(session)) {
            return "redirect:/login";
        }
        
        try {
            
            model.addAttribute("totalUsers", userService.getTotalUsers());
            model.addAttribute("totalProducts", productService.getTotalProducts());
            model.addAttribute("totalCategories", categoryService.getAllCategories().size());
            model.addAttribute("totalOrders", orderService.getTotalOrders());
            model.addAttribute("totalRevenue", orderService.getTotalRevenue());
            
            model.addAttribute("recentOrders", orderService.getRecentOrders(10));
            model.addAttribute("lowStockProducts", productService.getLowStockProducts(10));
            model.addAttribute("recentUsers", userService.getRecentUsers(5));
            
            return "admin/dashboard"; 
            
        } catch (Exception e) {
            model.addAttribute("error", "Đã xảy ra lỗi: " + e.getMessage());
            return "admin/dashboard";
        }
    }

    @GetMapping("/users")
    public String manageUsers(HttpSession session, Model model) {
        if (!isAdmin(session)) {
            return "redirect:/login";
        }
        
        try {
            List<User> users = userService.getAllUsers();
            
            long adminCount = users.stream()
                .filter(u -> u.getRole() != null && "admin".equalsIgnoreCase(u.getRole().getRoleName()))
                .count();
            long customerCount = users.stream()
                .filter(u -> u.getRole() != null && "customer".equalsIgnoreCase(u.getRole().getRoleName()))
                .count();
            
            model.addAttribute("users", users);
            model.addAttribute("adminCount", adminCount);
            model.addAttribute("customerCount", customerCount);
            return "admin/users"; 
            
        } catch (Exception e) {
            model.addAttribute("error", "Đã xảy ra lỗi: " + e.getMessage());
            model.addAttribute("users", List.of());
            model.addAttribute("adminCount", 0);
            model.addAttribute("customerCount", 0);
            return "admin/users";
        }
    }

    @PostMapping("/users/{id}/toggle-status")
    public String toggleUserStatus(@PathVariable("id") Integer userId,
                                  HttpSession session,
                                  RedirectAttributes redirectAttributes) {
        if (!isAdmin(session)) {
            return "redirect:/login";
        }
        
        try {
            userService.toggleUserStatus(userId);
            redirectAttributes.addFlashAttribute("success", "Trạng thái người dùng đã được cập nhật");
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Đã xảy ra lỗi: " + e.getMessage());
        }
        
        return "redirect:/admin/users";
    }

    @GetMapping("/categories")
    public String manageCategories(HttpSession session, Model model) {
        if (!isAdmin(session)) {
            return "redirect:/login";
        }
        
        try {
            List<Category> categories = categoryService.getAllCategoriesOrderedByName();
            model.addAttribute("categories", categories);
            model.addAttribute("newCategory", new Category());
            return "admin/categories"; 
            
        } catch (Exception e) {
            model.addAttribute("error", "Đã xảy ra lỗi: " + e.getMessage());
            model.addAttribute("categories", List.of());
            return "admin/categories";
        }
    }

    @PostMapping("/categories/create")
    public String createCategory(@ModelAttribute("newCategory") Category category,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {
        if (!isAdmin(session)) {
            return "redirect:/login";
        }
        
        try {
            categoryService.createCategory(category);
            redirectAttributes.addFlashAttribute("success", "Danh mục đã được tạo thành công");
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Đã xảy ra lỗi: " + e.getMessage());
        }
        
        return "redirect:/admin/categories";
    }

    @PostMapping("/categories/{id}/update")
    public String updateCategory(@PathVariable("id") Integer categoryId,
                               @ModelAttribute("category") Category category,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {
        if (!isAdmin(session)) {
            return "redirect:/login";
        }
        
        try {
            categoryService.updateCategory(categoryId, category);
            redirectAttributes.addFlashAttribute("success", "Danh mục đã được cập nhật thành công");
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Đã xảy ra lỗi: " + e.getMessage());
        }
        
        return "redirect:/admin/categories";
    }

    @PostMapping("/categories/{id}/delete")
    public String deleteCategory(@PathVariable("id") Integer categoryId,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {
        if (!isAdmin(session)) {
            return "redirect:/login";
        }
        
        try {
            categoryService.deleteCategory(categoryId);
            redirectAttributes.addFlashAttribute("success", "Danh mục đã được xóa thành công");
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Đã xảy ra lỗi: " + e.getMessage());
        }
        
        return "redirect:/admin/categories";
    }

    @GetMapping("/products")
    public String manageProducts(HttpSession session, Model model) {
        if (!isAdmin(session)) {
            return "redirect:/login";
        }
        
        try {
            List<Product> products = productService.getAllProducts();
            List<Category> categories = categoryService.getAllCategoriesOrderedByName();
            
            long inStockCount = products.stream().filter(p -> p.getStockQuantity() > 0).count();
            long lowStockCount = products.stream().filter(p -> p.getStockQuantity() > 0 && p.getStockQuantity() <= 5).count();
            long outOfStockCount = products.stream().filter(p -> p.getStockQuantity() == 0).count();
            
            model.addAttribute("products", products);
            model.addAttribute("categories", categories);
            model.addAttribute("newProduct", new Product());
            model.addAttribute("inStockCount", inStockCount);
            model.addAttribute("lowStockCount", lowStockCount);
            model.addAttribute("outOfStockCount", outOfStockCount);
            
            return "admin/products";
            
        } catch (Exception e) {
            model.addAttribute("error", "Đã xảy ra lỗi: " + e.getMessage());
            model.addAttribute("products", List.of());
            model.addAttribute("categories", List.of());
            model.addAttribute("inStockCount", 0);
            model.addAttribute("lowStockCount", 0);
            model.addAttribute("outOfStockCount", 0);
            return "admin/products";
        }
    }

    @GetMapping("/products/add")
    public String addProductPage(HttpSession session, Model model) {
        if (!isAdmin(session)) {
            return "redirect:/login";
        }
        
        try {
            List<Category> categories = categoryService.getAllCategoriesOrderedByName();
            
            model.addAttribute("categories", categories);
            model.addAttribute("newProduct", new Product());
            
            return "admin/add-product";
            
        } catch (Exception e) {
            model.addAttribute("error", "Đã xảy ra lỗi: " + e.getMessage());
            model.addAttribute("categories", List.of());
            return "admin/add-product";
        }
    }

    @PostMapping("/products/create")
    public String createProduct(@ModelAttribute("newProduct") Product product,
                              @RequestParam("categoryId") Integer categoryId,
                              HttpSession session,
                              RedirectAttributes redirectAttributes) {
        if (!isAdmin(session)) {
            return "redirect:/login";
        }
        
        try {
            
            if (product.getProductName() == null || product.getProductName().trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Tên sản phẩm không được để trống");
                return "redirect:/admin/products";
            }
            
            if (product.getPrice() == null || product.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
                redirectAttributes.addFlashAttribute("error", "Giá sản phẩm phải lớn hơn 0");
                return "redirect:/admin/products";
            }
            
            Optional<Category> categoryOpt = categoryService.getCategoryById(categoryId);
            if (categoryOpt.isPresent()) {
                product.setCategory(categoryOpt.get());
            }
            
            productService.createProduct(product);
            redirectAttributes.addFlashAttribute("success", "Sản phẩm đã được tạo thành công");
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Đã xảy ra lỗi: " + e.getMessage());
        }
        
        return "redirect:/admin/products";
    }

    @GetMapping("/products/{id}/edit")
    public String editProductPage(@PathVariable("id") Integer productId,
                                HttpSession session,
                                Model model) {
        if (!isAdmin(session)) {
            return "redirect:/login";
        }
        
        try {
            Optional<Product> productOpt = productService.getProductById(productId);
            if (productOpt.isEmpty()) {
                return "redirect:/admin/products";
            }
            
            List<Category> categories = categoryService.getAllCategoriesOrderedByName();
            
            model.addAttribute("product", productOpt.get());
            model.addAttribute("categories", categories);
            
            return "admin/edit-product"; 
            
        } catch (Exception e) {
            model.addAttribute("error", "Đã xảy ra lỗi: " + e.getMessage());
            return "redirect:/admin/products";
        }
    }

    @PostMapping("/products/{id}/update")
    public String updateProduct(@PathVariable("id") Integer productId,
                              @ModelAttribute("product") Product product,
                              @RequestParam("categoryId") Integer categoryId,
                              HttpSession session,
                              RedirectAttributes redirectAttributes) {
        if (!isAdmin(session)) {
            return "redirect:/login";
        }
        
        try {
            
            Optional<Category> categoryOpt = categoryService.getCategoryById(categoryId);
            if (categoryOpt.isPresent()) {
                product.setCategory(categoryOpt.get());
            }
            
            productService.updateProduct(productId, product);
            redirectAttributes.addFlashAttribute("success", "Sản phẩm đã được cập nhật thành công");
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Đã xảy ra lỗi: " + e.getMessage());
        }
        
        return "redirect:/admin/products";
    }

    @PostMapping("/products/{id}/delete")
    public String deleteProduct(@PathVariable("id") Integer productId,
                              HttpSession session,
                              RedirectAttributes redirectAttributes) {
        if (!isAdmin(session)) {
            return "redirect:/login";
        }
        
        try {
            productService.deleteProduct(productId);
            redirectAttributes.addFlashAttribute("success", "Sản phẩm đã được xóa thành công");
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Đã xảy ra lỗi: " + e.getMessage());
        }
        
        return "redirect:/admin/products";
    }

    @GetMapping("/orders")
    public String manageOrders(HttpSession session, Model model) {
        if (!isAdmin(session)) {
            return "redirect:/login";
        }
        
        try {
            List<Order> orders = orderService.getAllOrders();
            
            long pendingCount = orders.stream()
                .filter(o -> o.getStatus() == null || "pending".equalsIgnoreCase(o.getStatus()))
                .count();
            long shippingCount = orders.stream()
                .filter(o -> "shipping".equalsIgnoreCase(o.getStatus()))
                .count();
            long completedCount = orders.stream()
                .filter(o -> "completed".equalsIgnoreCase(o.getStatus()))
                .count();
            
            model.addAttribute("orders", orders);
            model.addAttribute("pendingCount", pendingCount);
            model.addAttribute("shippingCount", shippingCount);
            model.addAttribute("completedCount", completedCount);
            return "admin/orders"; 
            
        } catch (Exception e) {
            model.addAttribute("error", "Đã xảy ra lỗi: " + e.getMessage());
            model.addAttribute("orders", List.of());
            model.addAttribute("pendingCount", 0);
            model.addAttribute("shippingCount", 0);
            model.addAttribute("completedCount", 0);
            return "admin/orders";
        }
    }

    @GetMapping("/orders/{id}")
    public String orderDetail(@PathVariable("id") Integer orderId,
                            HttpSession session,
                            Model model) {
        if (!isAdmin(session)) {
            return "redirect:/login";
        }
        
        try {
            Optional<Order> orderOpt = orderService.getOrderById(orderId);
            if (orderOpt.isPresent()) {
                model.addAttribute("order", orderOpt.get());
                return "admin/order-detail"; 
            } else {
                return "redirect:/admin/orders";
            }
            
        } catch (Exception e) {
            model.addAttribute("error", "Đã xảy ra lỗi: " + e.getMessage());
            return "redirect:/admin/orders";
        }
    }

    @PostMapping("/orders/{id}/status")
    public String updateOrderStatus(@PathVariable("id") Integer orderId,
                                  @RequestParam("status") String status,
                                  HttpSession session,
                                  RedirectAttributes redirectAttributes) {
        if (!isAdmin(session)) {
            return "redirect:/login";
        }
        
        try {
            Optional<Order> orderOpt = orderService.getOrderById(orderId);
            if (orderOpt.isPresent()) {
                Order order = orderOpt.get();
                order.setStatus(status);
                orderService.updateOrder(orderId, order);
                redirectAttributes.addFlashAttribute("success", "Trạng thái đơn hàng đã được cập nhật thành công");
            } else {
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy đơn hàng");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Đã xảy ra lỗi: " + e.getMessage());
        }
        
        return "redirect:/admin/orders/" + orderId;
    }
}