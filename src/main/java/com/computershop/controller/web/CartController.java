package com.computershop.controller.web;

import com.computershop.service.impl.CartServiceImpl;
import com.computershop.service.impl.ProductServiceImpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import java.math.BigDecimal;

/**
 * Controller for cart-related operations.
 * Handles viewing cart, adding items, updating quantities, and removing items.
 */
@Controller
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private CartServiceImpl cartService;

    @Autowired
    private ProductServiceImpl productService;

    /**
     * Displays the shopping cart page.
     *
     * @param session the HTTP session
     * @param model the model
     * @return cart view
     */
    @GetMapping("/view")
    public String viewCart(HttpSession session, Model model) {
        Integer userId = (Integer) session.getAttribute("userId");

        if (userId == null) {
            return "redirect:/login";
        }

        try {
            var cartItems = cartService.getCartItemsSafe(userId);
            double total = cartService.getCartTotal(userId);

            model.addAttribute("cartItems", cartItems);
            model.addAttribute("cartTotal", total);

            return "cart/view";
        } catch (Exception e) {
            model.addAttribute("error", "Failed to load cart: " + e.getMessage());
            return "cart/view";
        }
    }

    /**
     * Adds a product to the cart.
     *
     * @param productId the product ID
     * @param quantity the quantity to add
     * @param session the HTTP session
     * @param redirectAttributes redirect attributes for flash messages
     * @return redirect to cart or product page
     */
    @PostMapping("/add")
    public String addToCart(
            @RequestParam Integer productId,
            @RequestParam(defaultValue = "1") Integer quantity,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        Integer userId = (Integer) session.getAttribute("userId");

        if (userId == null) {
            return "redirect:/login";
        }

        try {
            // Check if product exists
            var productOpt = productService.getProductById(productId);
            if (productOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Product not found");
                return "redirect:/products";
            }

            cartService.addToCart(userId, productId, quantity);
            redirectAttributes.addFlashAttribute("success", "Product added to cart");

            return "redirect:/cart/view";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/product/" + productId;
        }
    }

    /**
     * Updates the quantity of a cart item.
     *
     * @param cartItemId the cart item ID
     * @param quantity the new quantity
     * @param session the HTTP session
     * @param redirectAttributes redirect attributes
     * @return redirect to cart view
     */
    @PostMapping("/update")
    public String updateCartItem(
            @RequestParam Integer cartItemId,
            @RequestParam Integer quantity,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        Integer userId = (Integer) session.getAttribute("userId");

        if (userId == null) {
            return "redirect:/login";
        }

        try {
            if (quantity <= 0) {
                cartService.removeFromCart(cartItemId);
                redirectAttributes.addFlashAttribute("success", "Item removed from cart");
            } else {
                cartService.updateCartItemQuantity(cartItemId, quantity);
                redirectAttributes.addFlashAttribute("success", "Cart updated");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/cart/view";
    }

    /**
     * Removes an item from the cart.
     *
     * @param cartItemId the cart item ID
     * @param session the HTTP session
     * @param redirectAttributes redirect attributes
     * @return redirect to cart view
     */
    @PostMapping("/remove")
    public String removeFromCart(
            @RequestParam Integer cartItemId,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        Integer userId = (Integer) session.getAttribute("userId");

        if (userId == null) {
            return "redirect:/login";
        }

        try {
            cartService.removeFromCart(cartItemId);
            redirectAttributes.addFlashAttribute("success", "Item removed from cart");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/cart/view";
    }

    /**
     * Clears all items from the cart.
     *
     * @param session the HTTP session
     * @param redirectAttributes redirect attributes
     * @return redirect to cart view
     */
    @PostMapping("/clear")
    public String clearCart(
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        Integer userId = (Integer) session.getAttribute("userId");

        if (userId == null) {
            return "redirect:/login";
        }

        try {
            cartService.clearCart(userId);
            redirectAttributes.addFlashAttribute("success", "Cart cleared");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/cart/view";
    }

    /**
     * Displays the checkout page.
     *
     * @param session the HTTP session
     * @param model the model
     * @return checkout view or redirect to cart
     */
    @GetMapping("/checkout")
    public String checkout(HttpSession session, Model model) {
        Integer userId = (Integer) session.getAttribute("userId");

        if (userId == null) {
            return "redirect:/login";
        }

        try {
            var cartItems = cartService.getCartItemsSafe(userId);
            double total = cartService.getCartTotal(userId);

            if (cartItems.isEmpty()) {
                return "redirect:/cart/view";
            }

            model.addAttribute("cartItems", cartItems);
            model.addAttribute("cartTotal", total);

            return "cart/checkout";
        } catch (Exception e) {
            model.addAttribute("error", "Failed to load checkout: " + e.getMessage());
            return "redirect:/cart/view";
        }
    }
}
