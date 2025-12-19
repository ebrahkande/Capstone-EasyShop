package org.yearup.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.yearup.data.CategoryDao;
import org.yearup.data.ProductDao;
import org.yearup.models.Category;
import org.yearup.models.Product;

import java.util.List;

@RestController
@RequestMapping("/categories")
@CrossOrigin
public class CategoriesController
{
    private final CategoryDao categoryDao;
    private final ProductDao productDao;

    @Autowired
    public CategoriesController(CategoryDao categoryDao, ProductDao productDao)
    {
        this.categoryDao = categoryDao;
        this.productDao = productDao;
    }


    // GET /categories
    // Returns all categories (public)

    @GetMapping
    @PreAuthorize("permitAll()")
    public List<Category> getAll()
    {
        return categoryDao.getAllCategories();
    }


    // GET /categories/{id}
    // Returns a single category by ID (public)

    @GetMapping("/{id}")
    @PreAuthorize("permitAll()")
    public Category getById(@PathVariable int id)
    {
        Category category = categoryDao.getById(id);

        if (category == null)
        {
            throw new RuntimeException("Category not found with id: " + id);
        }

        return category;
    }


    // GET /categories/{categoryId}/products
    // Returns all products belonging to a category

    @GetMapping("/{categoryId}/products")
    @PreAuthorize("permitAll()")
    public List<Product> getProductsByCategory(@PathVariable int categoryId)
    {
        return productDao.listByCategoryId(categoryId);
    }


    // POST /categories
    // ADMIN ONLY — Create a new category

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public Category addCategory(@RequestBody Category category)
    {
        return categoryDao.create(category);
    }


    // PUT /categories/{id}
    // ADMIN ONLY — Update an existing category

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Category updateCategory(@PathVariable int id, @RequestBody Category category)
    {
        // Check if category exists before updating
        Category existing = categoryDao.getById(id);

        if (existing == null)
        {
            throw new RuntimeException("Cannot update — category not found with id: " + id);
        }

        // Ensure the ID in the URL is used
        category.setCategoryId(id);

        return categoryDao.update(id, category);
    }


    // DELETE /categories/{id}
    // ADMIN ONLY — Delete a category

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCategory(@PathVariable int id)
    {
        Category existing = categoryDao.getById(id);

        if (existing == null)
        {
            throw new RuntimeException("Cannot delete — category not found with id: " + id);
        }

        categoryDao.delete(id);
    }
}

