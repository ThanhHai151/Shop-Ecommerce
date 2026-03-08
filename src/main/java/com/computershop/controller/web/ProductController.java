package com.computershop.controller.web;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import com.computershop.main.entities.Category;
import com.computershop.main.entities.Product;
import com.computershop.service.impl.CategoryServiceImpl;
import com.computershop.service.impl.ProductServiceImpl;

/**
 * Controller for product-related public routes.
 * Handles displaying products, product details, search, and filtering.
 */
@Controller
public class ProductController {

    @Autowired
    private ProductServiceImpl productService;

    @Autowired
    private CategoryServiceImpl categoryService;

    /**
     * Displays the products listing page.
     *
     * @param category filter by category (optional)
     * @param sort sorting option (optional)
     * @param minPrice minimum price filter (optional)
     * @param maxPrice maximum price filter (optional)
     * @param model the model
     * @return products listing view
     */
    @GetMapping("/products")
    public String products(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            Model model) {
        try {
            List<Product> products;

            // Search by keyword takes priority
            if (search != null && !search.trim().isEmpty()) {
                products = productService.searchProducts(search.trim());
            } else if (category != null && !category.isEmpty()) {
                products = productService.getProductsByCategoryName(category);
            } else {
                products = productService.getAllProducts();
            }

            // Filter by price range (always applies after fetching)
            if (minPrice != null || maxPrice != null) {
                final double min = (minPrice != null) ? minPrice : 0;
                final double max = (maxPrice != null) ? maxPrice : Double.MAX_VALUE;
                products = products.stream()
                        .filter(p -> p.getPrice().doubleValue() >= min && p.getPrice().doubleValue() <= max)
                        .toList();
            }

            // Sort products
            if (sort != null) {
                products = switch (sort) {
                    case "price-asc" -> products.stream()
                            .sorted(java.util.Comparator.comparing(p -> p.getPrice()))
                            .toList();
                    case "price-desc" -> products.stream()
                            .sorted(java.util.Comparator.comparing((Product p) -> p.getPrice()).reversed())
                            .toList();
                    case "popular" -> products; // keep DB order (most sold first when no filter)
                    default -> products.stream()
                            .sorted(java.util.Comparator.comparing(p -> p.getProductName()))
                            .toList();
                };
            }

            model.addAttribute("products", products);
            model.addAttribute("totalProducts", products.size());
            model.addAttribute("categories", categoryService.getAllCategories());
            model.addAttribute("selectedCategory", category);
            model.addAttribute("currentSort", sort != null ? sort : "name");
            model.addAttribute("searchQuery", search);
            model.addAttribute("minPrice", minPrice);
            model.addAttribute("maxPrice", maxPrice);

            return "products";
        } catch (Exception e) {
            model.addAttribute("error", "Failed to load products: " + e.getMessage());
            model.addAttribute("products", List.of());
            model.addAttribute("totalProducts", 0);
            model.addAttribute("categories", categoryService.getAllCategories());
            return "products";
        }
    }

    /**
     * Displays product detail page.
     *
     * @param id the product ID
     * @param model the model
     * @return product detail view or redirect to products page
     */
    @GetMapping({"/product/{id}", "/products/{id}"})
    public String productDetail(@PathVariable("id") Integer id, Model model) {
        try {
            Optional<Product> productOpt = productService.getProductById(id);

            if (productOpt.isPresent()) {
                model.addAttribute("product", productOpt.get());

                // Get related products from same category
                Category category = productOpt.get().getCategory();
                if (category != null) {
                    List<Product> relatedProducts = productService.getProductsByCategory(category);
                    // Exclude current product
                    relatedProducts = relatedProducts.stream()
                            .filter(p -> !p.getProductId().equals(id))
                            .limit(4)
                            .toList();
                    model.addAttribute("relatedProducts", relatedProducts);
                }

                return "product-detail";
            } else {
                return "redirect:/products";
            }
        } catch (Exception e) {
            model.addAttribute("error", "Failed to load product: " + e.getMessage());
            return "redirect:/products";
        }
    }

    /**
     * Handles product search.
     *
     * @param keyword the search keyword
     * @param model the model
     * @return search results view
     */
    @GetMapping("/search")
    public String search(@RequestParam("q") String q, Model model) {
        try {
            List<Product> products = productService.searchProducts(q);
            model.addAttribute("products", products);
            model.addAttribute("totalProducts", products.size());
            model.addAttribute("searchQuery", q);
            model.addAttribute("categories", categoryService.getAllCategories());
            model.addAttribute("currentSort", "name");

            return "products";
        } catch (Exception e) {
            model.addAttribute("error", "Search failed: " + e.getMessage());
            model.addAttribute("products", List.of());
            model.addAttribute("totalProducts", 0);
            model.addAttribute("categories", categoryService.getAllCategories());
            return "products";
        }
    }
}
