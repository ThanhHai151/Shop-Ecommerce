package com.computershop.main.controllers;

import com.computershop.main.entities.User;
import com.computershop.main.entities.Role;
import com.computershop.main.entities.PasswordResetToken;
import com.computershop.main.services.UserService;
import com.computershop.main.services.RoleService;
import com.computershop.main.services.PasswordResetTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.Optional;

@Controller
public class AuthController {

    @Autowired
    private UserService userService;
    
    @Autowired
    private RoleService roleService;
    
    @Autowired
    private PasswordResetTokenService passwordResetTokenService;

    @GetMapping("/login")
    public String loginPage(@RequestParam(value = "error", required = false) String error,
                           @RequestParam(value = "logout", required = false) String logout,
                           Model model) {
        if (error != null) {
            model.addAttribute("error", "Tên đăng nhập hoặc mật khẩu không đúng.");
        }
        if (logout != null) {
            model.addAttribute("message", "Bạn đã đăng xuất thành công.");
        }
        return "login";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute("user") User user,
                              @RequestParam("confirmPassword") String confirmPassword,
                              @RequestParam(value = "agree", required = false) String agree,
                              BindingResult bindingResult,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        
        if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
            model.addAttribute("error", "Tên đăng nhập không được để trống.");
            return "register";
        }
        
        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            model.addAttribute("error", "Email không được để trống.");
            return "register";
        }
        
        if (user.getPassword() == null || user.getPassword().length() < 6) {
            model.addAttribute("error", "Mật khẩu phải có ít nhất 6 ký tự.");
            return "register";
        }
        
        if (!user.getPassword().equals(confirmPassword)) {
            model.addAttribute("error", "Mật khẩu xác nhận không khớp.");
            return "register";
        }
        
        if (agree == null) {
            model.addAttribute("error", "Bạn phải đồng ý với điều khoản sử dụng.");
            return "register";
        }

        try {
            
            if (userService.findByUsername(user.getUsername()).isPresent()) {
                model.addAttribute("error", "Tên đăng nhập đã tồn tại.");
                return "register";
            }
            
            if (userService.findByEmail(user.getEmail()).isPresent()) {
                model.addAttribute("error", "Email đã được sử dụng.");
                return "register";
            }
            
            Optional<Role> userRole = roleService.findByRoleName("USER");
            if (userRole.isPresent()) {
                user.setRole(userRole.get());
            } else {
                
                Role newUserRole = new Role();
                newUserRole.setRoleId(2); 
                newUserRole.setRoleName("USER");
                roleService.createRole(newUserRole);
                user.setRole(newUserRole);
            }
            
            userService.registerUser(user);
            
            redirectAttributes.addFlashAttribute("success", 
                "Đăng ký thành công! Bạn có thể đăng nhập ngay bây giờ.");
            return "redirect:/login";
            
        } catch (Exception e) {
            model.addAttribute("error", "Đã xảy ra lỗi: " + e.getMessage());
            return "register";
        }
    }

    @PostMapping("/login")
    public String loginUser(@RequestParam("username") String username,
                           @RequestParam("password") String password,
                           @RequestParam(value = "remember-me", required = false) String rememberMe,
                           HttpServletRequest request,
                           Model model) {
        
        try {
            
            Optional<User> userOpt = userService.validateLogin(username, password);
            
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                
                HttpSession session = request.getSession();
                session.setAttribute("currentUser", user);
                session.setAttribute("userId", user.getUserId());
                session.setAttribute("username", user.getUsername());
                session.setAttribute("role", user.getRole().getRoleName());
                
                String roleName = user.getRole().getRoleName();
                if ("admin".equals(roleName)) {
                    return "redirect:/admin/dashboard";
                } else {
                    return "redirect:/";
                }
            } else {
                model.addAttribute("error", "Tên đăng nhập hoặc mật khẩu không đúng.");
                return "login";
            }
            
        } catch (Exception e) {
            model.addAttribute("error", "Đã xảy ra lỗi đăng nhập: " + e.getMessage());
            return "login";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpServletRequest request, RedirectAttributes redirectAttributes) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        redirectAttributes.addFlashAttribute("message", "Bạn đã đăng xuất thành công.");
        return "redirect:/login";
    }

    @GetMapping("/forgot-password")
    public String forgotPasswordPage() {
        return "forgot-password"; 
    }
    
    @PostMapping("/forgot-password")
    public String processForgotPassword(@RequestParam("email") String email,
                                       Model model,
                                       RedirectAttributes redirectAttributes) {
        try {
            Optional<User> userOpt = userService.findByEmail(email);
            
            if (userOpt.isEmpty()) {
                model.addAttribute("error", "Không tìm thấy tài khoản với email này.");
                return "forgot-password";
            }
            
            User user = userOpt.get();
            PasswordResetToken resetToken = passwordResetTokenService.createToken(user);
            
            // Trong thực tế, bạn nên gửi email với link reset
            // Tạm thời hiển thị link trực tiếp
            String resetLink = "/reset-password?token=" + resetToken.getToken();
            
            model.addAttribute("success", 
                "Đã tạo link đặt lại mật khẩu. Vui lòng truy cập: " + resetLink);
            model.addAttribute("resetLink", resetLink);
            
            return "forgot-password";
            
        } catch (Exception e) {
            model.addAttribute("error", "Đã xảy ra lỗi: " + e.getMessage());
            return "forgot-password";
        }
    }
    
    @GetMapping("/reset-password")
    public String resetPasswordPage(@RequestParam("token") String token, Model model) {
        
        if (!passwordResetTokenService.validateToken(token)) {
            model.addAttribute("error", "Link đặt lại mật khẩu không hợp lệ hoặc đã hết hạn.");
            return "forgot-password";
        }
        
        model.addAttribute("token", token);
        return "reset-password";
    }
    
    @PostMapping("/reset-password")
    public String processResetPassword(@RequestParam("token") String token,
                                      @RequestParam("newPassword") String newPassword,
                                      @RequestParam("confirmPassword") String confirmPassword,
                                      Model model,
                                      RedirectAttributes redirectAttributes) {
        
        try {
            if (!passwordResetTokenService.validateToken(token)) {
                model.addAttribute("error", "Link đặt lại mật khẩu không hợp lệ hoặc đã hết hạn.");
                return "reset-password";
            }
            
            if (newPassword == null || newPassword.length() < 6) {
                model.addAttribute("error", "Mật khẩu phải có ít nhất 6 ký tự.");
                model.addAttribute("token", token);
                return "reset-password";
            }
            
            if (!newPassword.equals(confirmPassword)) {
                model.addAttribute("error", "Mật khẩu xác nhận không khớp.");
                model.addAttribute("token", token);
                return "reset-password";
            }
            
            Optional<PasswordResetToken> tokenOpt = passwordResetTokenService.getToken(token);
            if (tokenOpt.isEmpty()) {
                model.addAttribute("error", "Token không hợp lệ.");
                return "reset-password";
            }
            
            User user = tokenOpt.get().getUser();
            userService.changePassword(user.getUserId(), newPassword);
            
            passwordResetTokenService.markTokenAsUsed(token);
            
            redirectAttributes.addFlashAttribute("success", 
                "Đặt lại mật khẩu thành công! Bạn có thể đăng nhập ngay bây giờ.");
            return "redirect:/login";
            
        } catch (Exception e) {
            model.addAttribute("error", "Đã xảy ra lỗi: " + e.getMessage());
            model.addAttribute("token", token);
            return "reset-password";
        }
    }
}