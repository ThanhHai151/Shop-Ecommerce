package com.computershop.controller.web;

import com.computershop.service.impl.ProductServiceImpl;
import com.computershop.service.impl.CategoryServiceImpl;
import com.computershop.main.entities.Product;
import com.computershop.main.entities.Category;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Optional;

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
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            Model model) {
        try {
            List<Product> products;

            // Filter by category
            if (category != null && !category.isEmpty()) {
                products = productService.getProductsByCategoryName(category);
            } else {
                products = productService.getAllProducts();
            }

            // Sort products
            if (sort != null) {
                switch (sort) {
                    case "price-asc":
                        products = productService.getProductsByPriceAsc();
                        break;
                    case "price-desc":
                        products = productService.getProductsByPriceDesc();
                        break;
                    default:
                        // Default sorting
                        break;
                }
            }

            // Filter by price range
            if (minPrice != null && maxPrice != null) {
                products = products.stream()
                        .filter(p -> p.getPrice().doubleValue() >= minPrice && p.getPrice().doubleValue() <= maxPrice)
                        .toList();
            }

            model.addAttribute("products", products);
            model.addAttribute("categories", categoryService.getAllCategories());
            model.addAttribute("selectedCategory", category);
            model.addAttribute("selectedSort", sort);

            return "products";
        } catch (Exception e) {
            model.addAttribute("error", "Failed to load products: " + e.getMessage());
            model.addAttribute("products", List.of());
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
    @GetMapping("/product/{id}")
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
    public String search(@RequestParam("keyword") String keyword, Model model) {
        try {
            List<Product> products = productService.searchProducts(keyword);
            model.addAttribute("products", products);
            model.addAttribute("keyword", keyword);
            model.addAttribute("categories", categoryService.getAllCategories());

            return "products";
        } catch (Exception e) {
            model.addAttribute("error", "Search failed: " + e.getMessage());
            model.addAttribute("products", List.of());
            return "products";
        }
    }
}
