package com.computershop.main.controllers;

import com.computershop.main.entities.Product;

import com.computershop.main.services.ProductService;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    @GetMapping
    public String productsPage(@RequestParam(value = "category", required = false) String category,
                              @RequestParam(value = "search", required = false) String search,
                              @RequestParam(value = "q", required = false) String q,
                              @RequestParam(value = "minPrice", required = false) BigDecimal minPrice,
                              @RequestParam(value = "maxPrice", required = false) BigDecimal maxPrice,
                              @RequestParam(value = "sort", required = false, defaultValue = "name") String sort,
                              @RequestParam(value = "page", required = false, defaultValue = "0") int page,
                              @RequestParam(value = "size", required = false, defaultValue = "12") int size,
                              Model model) {
        
        try {
            List<Product> products;
            
            String searchQuery = search != null ? search : q; 
            if (searchQuery != null && !searchQuery.trim().isEmpty()) {
                products = productService.searchProductsByName(searchQuery.trim());
            } else if (category != null && !category.trim().isEmpty()) {
                products = productService.getProductsByCategoryName(category);
            } else {
                products = productService.getAllProducts();
            }
            
            if (minPrice != null || maxPrice != null) {
                BigDecimal min = minPrice != null ? minPrice : BigDecimal.ZERO;
                BigDecimal max = maxPrice != null ? maxPrice : BigDecimal.valueOf(Double.MAX_VALUE);
                products = products.stream()
                    .filter(p -> p.getPrice().compareTo(min) >= 0 && p.getPrice().compareTo(max) <= 0)
                    .toList();
            }
            
            switch (sort) {
                case "price-asc":
                    products = products.stream()
                        .sorted((p1, p2) -> p1.getPrice().compareTo(p2.getPrice()))
                        .toList();
                    break;
                case "price-desc":
                    products = products.stream()
                        .sorted((p1, p2) -> p2.getPrice().compareTo(p1.getPrice()))
                        .toList();
                    break;
                case "popular":
                    
                    products = products.stream()
                        .sorted((p1, p2) -> Integer.compare(p2.getStockQuantity(), p1.getStockQuantity()))
                        .toList();
                    break;
                case "name":
                default:
                    products = products.stream()
                        .sorted((p1, p2) -> p1.getProductName().compareToIgnoreCase(p2.getProductName()))
                        .toList();
                    break;
            }
            
            model.addAttribute("products", products);
            model.addAttribute("categories", productService.getAllCategoryNames());
            model.addAttribute("totalProducts", products.size());
            
            model.addAttribute("selectedCategory", category);
            model.addAttribute("searchQuery", searchQuery);
            model.addAttribute("minPrice", minPrice);
            model.addAttribute("maxPrice", maxPrice);
            model.addAttribute("currentSort", sort);
            
        } catch (Exception e) {
            model.addAttribute("products", List.of());
            model.addAttribute("categories", List.of());
            model.addAttribute("totalProducts", 0);
            model.addAttribute("error", "Đã xảy ra lỗi khi tải sản phẩm: " + e.getMessage());
        }
        
        return "products";
    }
    
    @GetMapping("/{id}")
    public String productDetail(@PathVariable("id") Integer productId, Model model) {
        try {
            Optional<Product> productOpt = productService.getProductById(productId);
            
            if (productOpt.isPresent()) {
                Product product = productOpt.get();
                model.addAttribute("product", product);
                
                List<Product> relatedProducts = List.of();
                if (product.getCategory() != null) {
                    relatedProducts = productService.getProductsByCategory(product.getCategory());
                    relatedProducts.removeIf(p -> p.getProductId().equals(productId)); 
                    if (relatedProducts.size() > 4) {
                        relatedProducts = relatedProducts.subList(0, 4); 
                    }
                }
                model.addAttribute("relatedProducts", relatedProducts);
                
                model.addAttribute("inStock", product.getStockQuantity() > 0);
                model.addAttribute("lowStock", product.getStockQuantity() > 0 && product.getStockQuantity() < 10);
                
                return "product-detail"; 
            } else {
                model.addAttribute("error", "Không tìm thấy sản phẩm");
                return "redirect:/products";
            }
            
        } catch (Exception e) {
            model.addAttribute("error", "Đã xảy ra lỗi: " + e.getMessage());
            return "redirect:/products";
        }
    }

    @GetMapping("/search")
    public String searchProducts(@RequestParam("q") String query, Model model) {
        return "redirect:/products?search=" + query;
    }

    @GetMapping("/category/{category}")
    public String productsByCategory(@PathVariable("category") String category, Model model) {
        try {
            List<Product> products = productService.getProductsByCategoryName(category);
            
            model.addAttribute("products", products);
            model.addAttribute("selectedCategory", category);
            model.addAttribute("totalProducts", products.size());
            model.addAttribute("categories", productService.getAllCategoryNames());
            
            return "products";
            
        } catch (Exception e) {
            model.addAttribute("error", "Đã xảy ra lỗi: " + e.getMessage());
            return "redirect:/products";
        }
    }

    @PostMapping("/{id}/add-to-cart")
    @ResponseBody
    public String addToCart(@PathVariable("id") Integer productId,
                           @RequestParam("quantity") Integer quantity,
                           HttpSession session) {
        try {
            
            Integer userId = (Integer) session.getAttribute("userId");
            if (userId == null) {
                return "error:Vui lòng đăng nhập để thêm sản phẩm vào giỏ hàng";
            }
            
            Optional<Product> productOpt = productService.getProductById(productId);
            if (productOpt.isEmpty()) {
                return "error:Không tìm thấy sản phẩm";
            }
            
            Product product = productOpt.get();
            if (product.getStockQuantity() < quantity) {
                return "error:Không đủ hàng trong kho";
            }
            
            return "success:Đã thêm sản phẩm vào giỏ hàng";
            
        } catch (Exception e) {
            return "error:Đã xảy ra lỗi: " + e.getMessage();
        }
    }
}