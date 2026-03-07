package com.computershop.controller.web;

import com.computershop.service.impl.UserServiceImpl;
import com.computershop.main.entities.User;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;

/**
 * Controller for authentication-related operations.
 * Handles login, logout, registration, password reset, etc.
 */
@Controller
public class AuthController {

    @Autowired
    private UserServiceImpl userService;

    /**
     * Displays the login page.
     *
     * @return login view
     */
    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    /**
     * Processes user login.
     *
     * @param usernameOrEmail username or email
     * @param password password
     * @param session HTTP session
     * @param redirectAttributes redirect attributes
     * @return redirect to home or back to login with error
     */
    @PostMapping("/login")
    public String login(
            @RequestParam String usernameOrEmail,
            @RequestParam String password,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        try {
            var userOpt = userService.authenticate(usernameOrEmail, password);

            if (userOpt.isPresent()) {
                User user = userOpt.get();
                session.setAttribute("userId", user.getUserId());
                session.setAttribute("username", user.getUsername());
                session.setAttribute("role", user.getRole().getRoleName());

                redirectAttributes.addFlashAttribute("success", "Welcome, " + user.getUsername() + "!");
                return "redirect:/";
            } else {
                redirectAttributes.addFlashAttribute("error", "Invalid username or password");
                return "redirect:/login";
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Login failed: " + e.getMessage());
            return "redirect:/login";
        }
    }

    /**
     * Processes user logout.
     *
     * @param session HTTP session
     * @return redirect to home
     */
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }

    /**
     * Displays the registration page.
     *
     * @return registration view
     */
    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    /**
     * Processes user registration.
     *
     * @param username username
     * @param email email
     * @param password password
     * @param confirmPassword confirm password
     * @param redirectAttributes redirect attributes
     * @return redirect to login or back to register with error
     */
    @PostMapping("/register")
    public String register(
            @RequestParam String username,
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam String confirmPassword,
            RedirectAttributes redirectAttributes) {

        try {
            // Validate passwords match
            if (!password.equals(confirmPassword)) {
                redirectAttributes.addFlashAttribute("error", "Passwords do not match");
                return "redirect:/register";
            }

            // Check if username exists
            if (userService.existsByUsername(username)) {
                redirectAttributes.addFlashAttribute("error", "Username already exists");
                return "redirect:/register";
            }

            // Check if email exists
            if (userService.existsByEmail(email)) {
                redirectAttributes.addFlashAttribute("error", "Email already exists");
                return "redirect:/register";
            }

            // Create user
            User user = new User();
            user.setUsername(username);
            user.setEmail(email);
            user.setPassword(password);

            userService.registerUser(user);

            redirectAttributes.addFlashAttribute("success", "Registration successful! Please login.");
            return "redirect:/login";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Registration failed: " + e.getMessage());
            return "redirect:/register";
        }
    }

    /**
     * Displays the forgot password page.
     *
     * @return forgot password view
     */
    @GetMapping("/forgot-password")
    public String forgotPasswordPage() {
        return "forgot-password";
    }

    /**
     * Displays the reset password page.
     *
     * @param token password reset token
     * @param model model
     * @return reset password view or redirect to login
     */
    @GetMapping("/reset-password")
    public String resetPasswordPage(@RequestParam String token, Model model) {
        model.addAttribute("token", token);
        return "reset-password";
    }
}
