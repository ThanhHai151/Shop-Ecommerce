package com.computershop.main.controllers;

import com.computershop.main.entities.User;
import com.computershop.main.entities.Product;
import com.computershop.main.entities.Order;
import com.computershop.main.entities.OrderDetail;
import com.computershop.main.services.UserService;
import com.computershop.main.services.ProductService;
import com.computershop.main.services.OrderService;
import com.computershop.main.services.OrderDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Controller
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private UserService userService;
    
    @Autowired
    private ProductService productService;
    
    @Autowired
    private OrderService orderService;
    
    @Autowired
    private OrderDetailService orderDetailService;

    public static class CartItem {
        private Integer productId;
        private String productName;
        private BigDecimal price;
        private Integer quantity;
        private String imageUrl;
        
        public CartItem() {}
        
        public CartItem(Integer productId, String productName, BigDecimal price, Integer quantity, String imageUrl) {
            this.productId = productId;
            this.productName = productName;
            this.price = price;
            this.quantity = quantity;
            this.imageUrl = imageUrl;
        }
        
        public Integer getProductId() { return productId; }
        public void setProductId(Integer productId) { this.productId = productId; }
        
        public String getProductName() { return productName; }
        public void setProductName(String productName) { this.productName = productName; }
        
        public BigDecimal getPrice() { return price; }
        public void setPrice(BigDecimal price) { this.price = price; }
        
        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
        
        public String getImageUrl() { return imageUrl; }
        public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
        
        public BigDecimal getSubtotal() {
            return price.multiply(BigDecimal.valueOf(quantity));
        }
    }

    @SuppressWarnings("unchecked")
    private List<CartItem> getCartFromSession(HttpSession session) {
        Object cart = session.getAttribute("cart");
        if (cart instanceof List) {
            return (List<CartItem>) cart;
        }
        return new ArrayList<>();
    }

    private void saveCartToSession(HttpSession session, List<CartItem> cart) {
        session.setAttribute("cart", cart);
    }

    @GetMapping
    public String viewCart(HttpSession session, Model model) {
        List<CartItem> cartItems = getCartFromSession(session);
        
        BigDecimal total = cartItems.stream()
                .map(CartItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        model.addAttribute("cartItems", cartItems);
        model.addAttribute("cartTotal", total);
        model.addAttribute("cartSize", cartItems.size());
        
        return "cart/view"; 
    }

    @PostMapping("/update")
    @ResponseBody
    public Map<String, Object> updateCartItem(@RequestParam("productId") Integer productId,
                                             @RequestParam("quantity") Integer quantity,
                                             HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<CartItem> cart = getCartFromSession(session);
            
            if (quantity <= 0) {
                
                cart.removeIf(item -> item.getProductId().equals(productId));
            } else {
                
                Optional<Product> productOpt = productService.getProductById(productId);
                if (productOpt.isEmpty()) {
                    response.put("success", false);
                    response.put("message", "Không tìm thấy sản phẩm");
                    return response;
                }
                
                Product product = productOpt.get();
                if (quantity > product.getStockQuantity()) {
                    response.put("success", false);
                    response.put("message", "Không đủ hàng trong kho");
                    return response;
                }
                
                for (CartItem item : cart) {
                    if (item.getProductId().equals(productId)) {
                        item.setQuantity(quantity);
                        break;
                    }
                }
            }
            
            saveCartToSession(session, cart);
            
            BigDecimal total = cart.stream()
                    .map(CartItem::getSubtotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            response.put("success", true);
            response.put("cartTotal", total);
            response.put("cartSize", cart.stream().mapToInt(CartItem::getQuantity).sum());
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Đã xảy ra lỗi: " + e.getMessage());
        }
        
        return response;
    }

    @PostMapping("/remove")
    @ResponseBody
    public Map<String, Object> removeFromCart(@RequestParam("productId") Integer productId,
                                             HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<CartItem> cart = getCartFromSession(session);
            cart.removeIf(item -> item.getProductId().equals(productId));
            saveCartToSession(session, cart);
            
            response.put("success", true);
            response.put("message", "Đã xóa sản phẩm khỏi giỏ hàng");
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Đã xảy ra lỗi: " + e.getMessage());
        }
        
        return response;
    }

    @PostMapping("/clear")
    public String clearCart(HttpSession session, RedirectAttributes redirectAttributes) {
        session.removeAttribute("cart");
        redirectAttributes.addFlashAttribute("success", "Đã xóa toàn bộ giỏ hàng");
        return "redirect:/cart";
    }

    @GetMapping("/checkout")
    public String checkoutPage(HttpSession session, Model model) {
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }
        
        List<CartItem> cartItems = getCartFromSession(session);
        if (cartItems.isEmpty()) {
            return "redirect:/cart";
        }
        
        BigDecimal total = cartItems.stream()
                .map(CartItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        model.addAttribute("cartItems", cartItems);
        model.addAttribute("cartTotal", total);
        
        return "cart/checkout"; 
    }

    @PostMapping("/checkout")
    public String processCheckout(@RequestParam("shippingAddress") String shippingAddress,
                                @RequestParam("paymentMethod") String paymentMethod,
                                @RequestParam("notes") String notes,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }
        
        try {
            List<CartItem> cartItems = getCartFromSession(session);
            if (cartItems.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Giỏ hàng trống");
                return "redirect:/cart";
            }
            
            Optional<User> userOpt = userService.getUserById(userId);
            if (userOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy người dùng");
                return "redirect:/cart";
            }
            
            User user = userOpt.get();
            
            Order order = new Order();
            order.setUser(user);
            order.setOrderDate(LocalDateTime.now());
            
            Order savedOrder = orderService.createOrder(order);
            
            for (CartItem cartItem : cartItems) {
                Optional<Product> productOpt = productService.getProductById(cartItem.getProductId());
                if (productOpt.isPresent()) {
                    Product product = productOpt.get();
                    
                    if (product.getStockQuantity() < cartItem.getQuantity()) {
                        redirectAttributes.addFlashAttribute("error", 
                            "Sản phẩm " + product.getProductName() + " không đủ hàng");
                        return "redirect:/cart";
                    }
                    
                    OrderDetail orderDetail = new OrderDetail();
                    orderDetail.setOrder(savedOrder);
                    orderDetail.setProduct(product);
                    orderDetail.setQuantity(cartItem.getQuantity());
                    orderDetail.setPrice(cartItem.getPrice());
                    
                    orderDetailService.createOrderDetail(orderDetail);
                    
                    productService.updateStock(product.getProductId(), 
                        product.getStockQuantity() - cartItem.getQuantity());
                }
            }
            
            session.removeAttribute("cart");
            
            redirectAttributes.addFlashAttribute("success", 
                "Đặt hàng thành công! Mã đơn hàng: " + savedOrder.getOrderId());
            
            return "redirect:/user/orders/" + savedOrder.getOrderId();
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Đã xảy ra lỗi: " + e.getMessage());
            return "redirect:/cart/checkout";
        }
    }

    @GetMapping("/count")
    @ResponseBody
    public Map<String, Integer> getCartCount(HttpSession session) {
        List<CartItem> cart = getCartFromSession(session);
        int count = cart.stream().mapToInt(CartItem::getQuantity).sum();
        
        Map<String, Integer> response = new HashMap<>();
        response.put("count", count);
        return response;
    }
}