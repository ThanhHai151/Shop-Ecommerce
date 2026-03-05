package com.computershop.main.controllers;

import com.computershop.main.entities.Cart;
import com.computershop.main.entities.CartItem;
import com.computershop.main.entities.Product;
import com.computershop.main.entities.User;
import com.computershop.main.services.CartService;
import com.computershop.main.services.MomoPaymentService;
import com.computershop.main.services.OrderDetailService;
import com.computershop.main.services.OrderService;
import com.computershop.main.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/cart")
public class CartController {
    
    @Autowired
    private CartService cartService;
    
    @Autowired
    private UserService userService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderDetailService orderDetailService;

    @Autowired
    private MomoPaymentService momoPaymentService;

    public static class CartItemVM {
        private Integer productId;
        private String productName;
        private BigDecimal price;
        private Integer quantity;
        private String imageUrl;
        private BigDecimal subtotal;

        public CartItemVM() {}

        public CartItemVM(Integer productId, String productName, BigDecimal price, Integer quantity, String imageUrl, BigDecimal subtotal) {
            this.productId = productId;
            this.productName = productName;
            this.price = price;
            this.quantity = quantity;
            this.imageUrl = imageUrl;
            this.subtotal = subtotal;
        }

        public Integer getProductId() { return productId; }
        public String getProductName() { return productName; }
        public BigDecimal getPrice() { return price; }
        public Integer getQuantity() { return quantity; }
        public String getImageUrl() { return imageUrl; }
        public BigDecimal getSubtotal() { return subtotal; }

        public void setProductId(Integer productId) { this.productId = productId; }
        public void setProductName(String productName) { this.productName = productName; }
        public void setPrice(BigDecimal price) { this.price = price; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
        public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
        public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }
    }
    
    private Integer getUserIdFromSession(HttpSession session) {
        return (Integer) session.getAttribute("userId");
    }

    private void updateCartCountInSession(HttpSession session, Integer userId) {
        try {
            int count = cartService.getCartItemCount(userId);
            session.setAttribute("cartCount", count);
        } catch (Exception e) {
            session.setAttribute("cartCount", 0);
        }
    }

    @GetMapping({"", "/"})
    public String cartRoot() {
        return "redirect:/cart/view";
    }
    
    @GetMapping("/view")
    public String viewCart(HttpSession session, Model model) {
        Integer userId = getUserIdFromSession(session);
        
        if (userId == null) {
            return "redirect:/login";
        }
        
        try {
            List<CartItem> cartItems = cartService.getCartItems(userId);
            BigDecimal total = cartService.getCartTotal(userId);
            int itemCount = cartService.getCartItemCount(userId);

            List<CartItemVM> vmItems = cartItems.stream().map(item -> {
                Product p = item.getProduct();
                Integer pid = p != null ? p.getProductId() : null;
                String name = p != null ? p.getProductName() : "";
                BigDecimal price = p != null ? p.getPrice() : BigDecimal.ZERO;
                String imageUrl = (p != null && p.getImage() != null) ? p.getImage().getImageUrl() : "/Images/placeholder.svg";
                BigDecimal subtotal = item.getSubtotal();
                return new CartItemVM(pid, name, price, item.getQuantity(), imageUrl, subtotal);
            }).toList();

            model.addAttribute("cartItems", vmItems);
            model.addAttribute("cartTotal", total);
            model.addAttribute("cartItemCount", itemCount);

            updateCartCountInSession(session, userId);

            return "cart/view";

        } catch (Exception e) {
            model.addAttribute("error", "Đã xảy ra lỗi: " + e.getMessage());
            model.addAttribute("cartItems", List.of());
            model.addAttribute("cartTotal", BigDecimal.ZERO);
            model.addAttribute("cartItemCount", 0);
            session.setAttribute("cartCount", 0);
            return "cart/view";
        }
    }
    
    @PostMapping("/add")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> addToCart(@RequestParam("productId") Integer productId,
                          @RequestParam(value = "quantity", defaultValue = "1") Integer quantity,
                          HttpSession session) {
        Integer userId = getUserIdFromSession(session);
        
        if (userId == null) {
            return ResponseEntity.ok(Map.of("success", false, "message", "Vui lòng đăng nhập"));
        }
        
        try {
            // Đảm bảo cart tồn tại trước khi thêm item
            Optional<User> userOpt = userService.getUserById(userId);
            if (userOpt.isEmpty()) {
                return ResponseEntity.ok(Map.of("success", false, "message", "User không tồn tại"));
            }
            
            // Tạo cart nếu chưa có
            cartService.getOrCreateCart(userOpt.get());
            
            // Thêm sản phẩm vào giỏ hàng
            cartService.addToCart(userId, productId, quantity);
            int itemCount = cartService.getCartItemCount(userId);

            session.setAttribute("cartCount", itemCount);

            return ResponseEntity.ok(Map.of("success", true, "message", "Đã thêm vào giỏ hàng", "itemCount", itemCount));
            
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PostMapping("/update")
    @ResponseBody
    public Map<String, Object> updateCartItem(@RequestParam("productId") Integer productId,
                                              @RequestParam("quantity") Integer quantity,
                                              HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        Integer userId = getUserIdFromSession(session);

        if (userId == null) {
            response.put("success", false);
            response.put("message", "Vui lòng đăng nhập");
            return response;
        }

        try {
            cartService.updateCartItemQuantity(userId, productId, quantity);
            int itemCount = cartService.getCartItemCount(userId);
            session.setAttribute("cartCount", itemCount);
            response.put("success", true);
            response.put("itemCount", itemCount);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
        }

        return response;
    }

    @PostMapping("/remove")
    @ResponseBody
    public Map<String, Object> removeFromCart(@RequestParam("productId") Integer productId,
                                              HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        Integer userId = getUserIdFromSession(session);

        if (userId == null) {
            response.put("success", false);
            response.put("message", "Vui lòng đăng nhập");
            return response;
        }

        try {
            cartService.removeFromCart(userId, productId);
            int itemCount = cartService.getCartItemCount(userId);
            session.setAttribute("cartCount", itemCount);
            response.put("success", true);
            response.put("itemCount", itemCount);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
        }

        return response;
    }
    
    @PostMapping("/clear")
    public String clearCart(HttpSession session, RedirectAttributes redirectAttributes) {
        Integer userId = getUserIdFromSession(session);
        
        if (userId == null) {
            return "redirect:/login";
        }
        
        try {
            cartService.clearCart(userId);
            redirectAttributes.addFlashAttribute("success", "Đã xóa toàn bộ giỏ hàng");
            session.setAttribute("cartCount", 0);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }
        
        return "redirect:/cart/view";
    }

    @GetMapping("/checkout")
    public String checkoutPage(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        Integer userId = getUserIdFromSession(session);
        if (userId == null) {
            return "redirect:/login";
        }

        List<CartItem> cartItems = cartService.getCartItems(userId);
        if (cartItems == null || cartItems.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Giỏ hàng trống");
            return "redirect:/cart/view";
        }

        BigDecimal total = cartService.getCartTotal(userId);
        List<CartItemVM> vmItems = cartItems.stream().map(item -> {
            Product p = item.getProduct();
            Integer pid = p != null ? p.getProductId() : null;
            String name = p != null ? p.getProductName() : "";
            BigDecimal price = p != null ? p.getPrice() : BigDecimal.ZERO;
            String imageUrl = (p != null && p.getImage() != null) ? p.getImage().getImageUrl() : "/Images/placeholder.svg";
            BigDecimal subtotal = item.getSubtotal();
            return new CartItemVM(pid, name, price, item.getQuantity(), imageUrl, subtotal);
        }).toList();

        model.addAttribute("cartItems", vmItems);
        model.addAttribute("cartTotal", total);
        updateCartCountInSession(session, userId);

        return "cart/checkout";
    }

    @PostMapping("/checkout")
    @Transactional
    public String processCheckout(@RequestParam("shippingAddress") String shippingAddress,
                                  @RequestParam("paymentMethod") String paymentMethod,
                                  @RequestParam(value = "notes", required = false) String notes,
                                  HttpSession session,
                                  RedirectAttributes redirectAttributes) {
        Integer userId = getUserIdFromSession(session);
        if (userId == null) {
            return "redirect:/login";
        }

        try {
            List<CartItem> cartItems = cartService.getCartItems(userId);
            if (cartItems == null || cartItems.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Giỏ hàng trống");
                return "redirect:/cart/view";
            }

            Optional<User> userOpt = userService.getUserById(userId);
            if (userOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy người dùng");
                return "redirect:/cart/view";
            }

            User user = userOpt.get();

            com.computershop.main.entities.Order order = new com.computershop.main.entities.Order();
            order.setUser(user);
            order.setOrderDate(LocalDateTime.now());

            com.computershop.main.entities.Order savedOrder = orderService.createOrder(order);

            for (CartItem cartItem : cartItems) {
                Product product = cartItem.getProduct();
                if (product == null) {
                    continue;
                }

                if (product.getStockQuantity() < cartItem.getQuantity()) {
                    redirectAttributes.addFlashAttribute("error",
                            "Sản phẩm " + product.getProductName() + " không đủ hàng");
                    return "redirect:/cart/view";
                }

                orderDetailService.createOrderDetail(savedOrder, product, cartItem.getQuantity(), product.getPrice());
            }

            // Create MoMo payment transaction (PENDING) if chosen
            if (paymentMethod != null && "MOMO".equalsIgnoreCase(paymentMethod)) {
                BigDecimal total = cartService.getCartTotal(userId);
                momoPaymentService.createMomoTransaction(savedOrder, user, total, notes);
                redirectAttributes.addFlashAttribute("success",
                        "Đặt hàng thành công! Vui lòng thanh toán MoMo để hoàn tất đơn hàng. Mã đơn hàng: " + savedOrder.getOrderId());
            } else {
                redirectAttributes.addFlashAttribute("success",
                        "Đặt hàng thành công! Mã đơn hàng: " + savedOrder.getOrderId());
            }

            cartService.clearCart(userId);
            session.setAttribute("cartCount", 0);

            return "redirect:/user/orders/" + savedOrder.getOrderId();

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Đã xảy ra lỗi: " + e.getMessage());
            return "redirect:/cart/checkout";
        }
    }
    
    @GetMapping("/count")
    @ResponseBody
    public Map<String, Integer> getCartCount(HttpSession session) {
        Integer userId = getUserIdFromSession(session);
        
        if (userId == null) {
            return Map.of("count", 0);
        }
        
        try {
            int count = cartService.getCartItemCount(userId);
            session.setAttribute("cartCount", count);
            return Map.of("count", count);
        } catch (Exception e) {
            session.setAttribute("cartCount", 0);
            return Map.of("count", 0);
        }
    }
}
