package com.computershop.controller.web;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.computershop.main.entities.CartItem;
import com.computershop.main.entities.Order;
import com.computershop.main.entities.OrderDetail;
import com.computershop.main.entities.Product;
import com.computershop.service.impl.CartServiceImpl;
import com.computershop.service.impl.MomoService;
import com.computershop.service.impl.OrderDetailServiceImpl;
import com.computershop.service.impl.OrderServiceImpl;
import com.computershop.service.impl.ProductServiceImpl;
import com.computershop.service.impl.UserServiceImpl;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private CartServiceImpl cartService;

    @Autowired
    private ProductServiceImpl productService;

    @Autowired
    private OrderServiceImpl orderService;

    @Autowired
    private OrderDetailServiceImpl orderDetailService;

    @Autowired
    private UserServiceImpl userService;

    @Autowired
    private MomoService momoService;

    // ─── Xem giỏ hàng ────────────────────────────────────────────────────────

    @GetMapping("/view")
    public String viewCart(HttpSession session, Model model) {
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) return "redirect:/login";

        try {
            var cartItems = cartService.getCartItemsSafe(userId);
            double total  = cartService.getCartTotal(userId);
            model.addAttribute("cartItems", cartItems);
            model.addAttribute("cartTotal", total);
            return "cart/view";
        } catch (Exception e) {
            model.addAttribute("error", "Không thể tải giỏ hàng: " + e.getMessage());
            return "cart/view";
        }
    }

    // ─── Thêm vào giỏ (AJAX / JSON) ──────────────────────────────────────────

    @PostMapping("/add-ajax")
    @ResponseBody
    public Map<String, Object> addToCartAjax(
            @RequestParam Integer productId,
            @RequestParam(defaultValue = "1") Integer quantity,
            HttpSession session) {

        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            return Map.of("success", false, "message", "Vui lòng đăng nhập để thêm vào giỏ hàng.");
        }

        try {
            var productOpt = productService.getProductById(productId);
            if (productOpt.isEmpty()) {
                return Map.of("success", false, "message", "Sản phẩm không tồn tại.");
            }
            cartService.addToCart(userId, productId, quantity);
            int count = cartService.getCartItemCount(userId);
            session.setAttribute("cartCount", count);
            return Map.of("success", true, "message", "Đã thêm vào giỏ hàng!", "count", count);
        } catch (Exception e) {
            return Map.of("success", false, "message", e.getMessage() != null ? e.getMessage() : "Có lỗi xảy ra.");
        }
    }

    // ─── Thêm vào giỏ ────────────────────────────────────────────────────────

    @PostMapping("/add")
    public String addToCart(
            @RequestParam Integer productId,
            @RequestParam(defaultValue = "1") Integer quantity,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) return "redirect:/login";

        try {
            var productOpt = productService.getProductById(productId);
            if (productOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Sản phẩm không tồn tại");
                return "redirect:/products";
            }
            cartService.addToCart(userId, productId, quantity);
            redirectAttributes.addFlashAttribute("success", "Đã thêm vào giỏ hàng");
            return "redirect:/cart/view";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/product/" + productId;
        }
    }

    // ─── Cập nhật số lượng ────────────────────────────────────────────────────

    @PostMapping("/update")
    public String updateCartItem(
            @RequestParam Integer cartItemId,
            @RequestParam Integer quantity,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) return "redirect:/login";

        try {
            if (quantity <= 0) {
                cartService.removeFromCart(cartItemId);
                redirectAttributes.addFlashAttribute("success", "Đã xoá sản phẩm");
            } else {
                cartService.updateCartItemQuantity(cartItemId, quantity);
                redirectAttributes.addFlashAttribute("success", "Đã cập nhật giỏ hàng");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/cart/view";
    }

    // ─── Xoá 1 sản phẩm ──────────────────────────────────────────────────────

    @PostMapping("/remove")
    public String removeFromCart(
            @RequestParam Integer cartItemId,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) return "redirect:/login";

        try {
            cartService.removeFromCart(cartItemId);
            redirectAttributes.addFlashAttribute("success", "Đã xoá sản phẩm");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/cart/view";
    }

    // ─── Xoá toàn bộ giỏ hàng ────────────────────────────────────────────────

    @PostMapping("/clear")
    public String clearCart(HttpSession session, RedirectAttributes redirectAttributes) {
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) return "redirect:/login";

        try {
            cartService.clearCart(userId);
            redirectAttributes.addFlashAttribute("success", "Đã xoá giỏ hàng");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/cart/view";
    }

    // ─── Số lượng sản phẩm trong giỏ (API) ───────────────────────────────────

    @GetMapping("/count")
    @ResponseBody
    public Map<String, Object> getCartCount(HttpSession session) {
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) return Map.of("count", 0);

        try {
            return Map.of("count", cartService.getCartItemCount(userId));
        } catch (Exception ignored) {
            return Map.of("count", 0);
        }
    }

    // ─── Trang Checkout (GET) ─────────────────────────────────────────────────

    @GetMapping("/checkout")
    public String checkout(HttpSession session, Model model) {
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) return "redirect:/login";

        try {
            List<CartItem> cartItems = cartService.getCartItemsSafe(userId);
            double total             = cartService.getCartTotal(userId);

            if (cartItems.isEmpty()) return "redirect:/cart/view";

            model.addAttribute("cartItems", cartItems);
            model.addAttribute("cartTotal", total);
            return "cart/checkout";
        } catch (Exception e) {
            model.addAttribute("error", "Không thể tải trang thanh toán: " + e.getMessage());
            return "redirect:/cart/view";
        }
    }

    // ─── Xử lý đặt hàng (POST) ───────────────────────────────────────────────

    @PostMapping("/checkout")
    public String processCheckout(
            @RequestParam String shippingAddress,
            @RequestParam(required = false) String notes,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) return "redirect:/login";

        try {
            List<CartItem> cartItems = cartService.getCartItemsSafe(userId);
            if (cartItems.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Giỏ hàng trống");
                return "redirect:/cart/view";
            }

            // 1. Lấy User
            var userOpt = userService.getUserById(userId);
            if (userOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy người dùng");
                return "redirect:/cart/view";
            }

            // 2. Tạo Order
            Order order = new Order();
            order.setUser(userOpt.get());
            order.setOrderDate(LocalDateTime.now());
            order.setStatus("pending_payment");
            order.setShippingAddress(shippingAddress);
            if (notes != null && !notes.isBlank()) order.setNotes(notes);
            Order savedOrder = orderService.createOrder(order);

            // 3. Tạo OrderDetail
            for (CartItem item : cartItems) {
                Product product = item.getProduct();
                OrderDetail detail = new OrderDetail(
                    savedOrder,
                    product,
                    item.getQuantity(),
                    BigDecimal.valueOf(product.getPrice().doubleValue())
                );
                orderDetailService.createOrderDetail(detail);
            }

            // 4. Xoá giỏ hàng
            cartService.clearCart(userId);

            // 5. Redirect sang trang hiển thị QR
            return "redirect:/cart/payment/" + savedOrder.getOrderId();

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Đặt hàng thất bại: " + e.getMessage());
            return "redirect:/cart/checkout";
        }
    }

    // ─── Trang thanh toán QR VietQR ───────────────────────────────────────────

    @GetMapping("/payment/{orderId}")
    public String paymentPage(@PathVariable Integer orderId,
                              HttpSession session,
                              Model model) {
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) return "redirect:/login";

        try {
            var orderOpt = orderService.getOrderById(orderId);
            if (orderOpt.isEmpty() || !orderOpt.get().getUser().getUserId().equals(userId)) {
                return "redirect:/orders";
            }

            Order order  = orderOpt.get();
            long amount  = Math.round(order.getTotalAmount());
            String note  = "Thanh toan don hang #" + orderId;

            // Sinh QR VietQR Quick Link — ảnh QR từ img.vietqr.io, đúng chuẩn NAPAS
            String qrImageUrl = momoService.buildVietQRQuickLink(amount, note);

            model.addAttribute("order",         order);
            model.addAttribute("qrImageUrl",    qrImageUrl);
            model.addAttribute("accountNumber", momoService.getAccountNumber());
            model.addAttribute("accountName",   momoService.getAccountName());
            model.addAttribute("bankName",      momoService.getBankName());
            model.addAttribute("amount",        amount);
            model.addAttribute("note",          note);

            return "cart/payment";

        } catch (Exception e) {
            model.addAttribute("error", "Không thể tạo QR thanh toán: " + e.getMessage());
            return "redirect:/orders";
        }
    }
}
