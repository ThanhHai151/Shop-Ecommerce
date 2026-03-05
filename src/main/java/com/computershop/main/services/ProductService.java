package com.computershop.main.services;

import com.computershop.main.entities.Product;
import com.computershop.main.entities.Image;
import com.computershop.main.entities.Category;
import com.computershop.main.repositories.ProductRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class ProductService {
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private ImageService imageService;
    
    public List<Product> getAllProducts() {
        return productRepository.findAllWithCategoryAndImage();
    }
    
    public Optional<Product> getProductById(Integer productId) {
        return productRepository.findById(productId);
    }
    
    public List<Product> searchProductsByName(String keyword) {
        return productRepository.findByProductNameContainingIgnoreCase(keyword);
    }
    
    public List<Product> searchProductsByDescription(String keyword) {
        return productRepository.findByDescriptionContainingIgnoreCase(keyword);
    }
    
    public List<Product> searchProducts(String keyword) {
        return productRepository.searchProducts(keyword);
    }
    
    public List<Product> getProductsByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        return productRepository.findByPriceBetween(minPrice, maxPrice);
    }
    
    public List<Product> getProductsWithMinStock(Integer minStock) {
        return productRepository.findByStockQuantityGreaterThan(minStock);
    }
    
    public List<Product> getInStockProducts() {
        return productRepository.findInStockProducts();
    }
    
    public List<Product> getOutOfStockProducts() {
        return productRepository.findOutOfStockProducts();
    }
    
    public List<Product> getProductsByPriceAsc() {
        return productRepository.findAllByOrderByPriceAsc();
    }
    
    public List<Product> getProductsByPriceDesc() {
        return productRepository.findAllByOrderByPriceDesc();
    }
    
    public List<Product> getTopSellingProducts() {
        return productRepository.findTopSellingProducts();
    }
    
    public Product createProduct(Product product) {
        
        if (product.getImage() == null) {
            product.setImage(imageService.getPlaceholderImage());
        }
        
        return productRepository.save(product);
    }
    
    public Product createProduct(String productName, String description, 
                               BigDecimal price, Integer stockQuantity, String imageUrl) {
        Product product = new Product();
        product.setProductName(productName);
        product.setDescription(description);
        product.setPrice(price);
        product.setStockQuantity(stockQuantity);
        
        if (imageUrl != null && !imageUrl.trim().isEmpty()) {
            Image image = imageService.getOrCreateImageByUrl(imageUrl);
            product.setImage(image);
        } else {
            product.setImage(imageService.getPlaceholderImage());
        }
        
        return productRepository.save(product);
    }
    
    public Product updateProduct(Integer productId, Product productDetails) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));
        
        product.setProductName(productDetails.getProductName());
        product.setDescription(productDetails.getDescription());
        product.setPrice(productDetails.getPrice());
        product.setStockQuantity(productDetails.getStockQuantity());
        
        if (productDetails.getImage() != null) {
            product.setImage(productDetails.getImage());
        }
        
        return productRepository.save(product);
    }
    
    public Product updateProductStock(Integer productId, Integer newStock) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));
        
        product.setStockQuantity(newStock);
        return productRepository.save(product);
    }
    
    public Product decreaseStock(Integer productId, Integer quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));
        
        if (product.getStockQuantity() < quantity) {
            throw new RuntimeException("Insufficient stock. Available: " + product.getStockQuantity() + ", Requested: " + quantity);
        }
        
        product.setStockQuantity(product.getStockQuantity() - quantity);
        return productRepository.save(product);
    }
    
    public Product increaseStock(Integer productId, Integer quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));
        
        product.setStockQuantity(product.getStockQuantity() + quantity);
        return productRepository.save(product);
    }
    
    public void deleteProduct(Integer productId) {
        if (!productRepository.existsById(productId)) {
            throw new RuntimeException("Product not found with id: " + productId);
        }
        productRepository.deleteById(productId);
    }
    
    public boolean isInStock(Integer productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));
        return product.getStockQuantity() > 0;
    }
    
    public boolean hasSufficientStock(Integer productId, Integer requestedQuantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));
        return product.getStockQuantity() >= requestedQuantity;
    }
    
    public long countInStockProducts() {
        return productRepository.countInStockProducts();
    }
    
    public List<Product> getFeaturedProducts(int limit) {
        List<Product> allFeatured = productRepository.findFeaturedProductsWithDetails();
        return allFeatured.stream().limit(limit).toList();
    }
    
    public long getTotalProducts() {
        return productRepository.count();
    }
    
    public List<Category> getAllCategories() {
        return productRepository.findDistinctCategories();
    }
    
    public List<String> getAllCategoryNames() {
        return getAllCategories().stream()
                .map(Category::getCategoryName)
                .toList();
    }
    
    public List<Product> getProductsByCategory(Category category) {
        return productRepository.findByCategory(category);
    }
    
    public List<Product> getProductsByCategoryName(String categoryName) {
        return productRepository.findDistinctCategories().stream()
                .filter(cat -> cat.getCategoryName().equals(categoryName))
                .findFirst()
                .map(this::getProductsByCategory)
                .orElse(List.of());
    }
    
    public List<Product> getProductsByCategoryId(Integer categoryId) {
        return productRepository.findByCategoryCategoryId(categoryId);
    }
    
    public List<Product> filterProductsByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        return productRepository.findByPriceBetween(minPrice, maxPrice);
    }
    
    public List<Product> getProductsSortedByPriceAsc() {
        return productRepository.findAllByOrderByPriceAsc();
    }
    
    public List<Product> getProductsSortedByPriceDesc() {
        return productRepository.findAllByOrderByPriceDesc();
    }
    
    public List<Product> getLowStockProducts(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return productRepository.findLowStockProducts(pageable);
    }
    
    public void updateStock(Integer productId, int newStock) {
        Optional<Product> productOpt = productRepository.findById(productId);
        if (productOpt.isPresent()) {
            Product product = productOpt.get();
            product.setStockQuantity(newStock);
            productRepository.save(product);
        }
    }
    
}