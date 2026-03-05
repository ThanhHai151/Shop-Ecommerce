package com.computershop.main.services;

import com.computershop.main.entities.Category;
import com.computershop.main.repositories.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CategoryService {
    
    @Autowired
    private CategoryRepository categoryRepository;
    
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }
    
    public List<Category> getAllCategoriesOrderedByName() {
        return categoryRepository.findAllByOrderByCategoryNameAsc();
    }
    
    public Optional<Category> getCategoryById(Integer categoryId) {
        return categoryRepository.findById(categoryId);
    }
    
    public Optional<Category> getCategoryByName(String categoryName) {
        return categoryRepository.findByCategoryName(categoryName);
    }
    
    public Optional<Category> getCategoryByNameIgnoreCase(String categoryName) {
        return categoryRepository.findByCategoryNameIgnoreCase(categoryName);
    }
    
    public Category createCategory(Category category) {
        if (categoryRepository.existsByCategoryName(category.getCategoryName())) {
            throw new RuntimeException("Category with name '" + category.getCategoryName() + "' already exists");
        }
        return categoryRepository.save(category);
    }
    
    public Category updateCategory(Integer categoryId, Category category) {
        Optional<Category> existingCategoryOpt = categoryRepository.findById(categoryId);
        if (existingCategoryOpt.isEmpty()) {
            throw new RuntimeException("Category not found with id: " + categoryId);
        }
        
        Category existingCategory = existingCategoryOpt.get();
        
        if (!existingCategory.getCategoryName().equals(category.getCategoryName()) &&
            categoryRepository.existsByCategoryName(category.getCategoryName())) {
            throw new RuntimeException("Category name '" + category.getCategoryName() + "' is already taken");
        }
        
        existingCategory.setCategoryName(category.getCategoryName());
        existingCategory.setDescription(category.getDescription());
        
        return categoryRepository.save(existingCategory);
    }
    
    public void deleteCategory(Integer categoryId) {
        Optional<Category> categoryOpt = categoryRepository.findById(categoryId);
        if (categoryOpt.isEmpty()) {
            throw new RuntimeException("Category not found with id: " + categoryId);
        }
        
        Category category = categoryOpt.get();
        
        if (category.getProducts() != null && !category.getProducts().isEmpty()) {
            throw new RuntimeException("Cannot delete category that has products. Please move or delete all products first.");
        }
        
        categoryRepository.deleteById(categoryId);
    }
    
    public List<String> getAllCategoryNames() {
        return categoryRepository.findAllCategoryNames();
    }
    
    public List<Object[]> countProductsInCategories() {
        return categoryRepository.countProductsInCategories();
    }
    
    public boolean categoryExists(String categoryName) {
        return categoryRepository.existsByCategoryName(categoryName);
    }
}