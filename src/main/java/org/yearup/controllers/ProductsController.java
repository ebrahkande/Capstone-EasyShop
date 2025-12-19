package org.yearup.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.yearup.data.ProductDao;
import org.yearup.models.Product;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/products")
@CrossOrigin
public class ProductsController
{
    private final ProductDao productDao;

    @Autowired
    public ProductsController(ProductDao productDao)
    {
        this.productDao = productDao;
    }


    // GET /products
    // Search products using optional filters

    @GetMapping
    @PreAuthorize("permitAll()")
    public List<Product> search(@RequestParam(name = "cat", required = false) Integer categoryId,
                                @RequestParam(name = "minPrice", required = false) BigDecimal minPrice,
                                @RequestParam(name = "maxPrice", required = false) BigDecimal maxPrice,
                                @RequestParam(name = "subCategory", required = false) String subCategory)
    {
        return productDao.search(categoryId, minPrice, maxPrice, subCategory);
    }


    // GET /products/{id}
    // Get a single product by ID

    @GetMapping("/{id}")
    @PreAuthorize("permitAll()")
    public Product getById(@PathVariable int id)
    {
        Product product = productDao.getById(id);

        if (product == null)
        {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found.");
        }

        return product;
    }


    // POST /products
    // ADMIN ONLY — Create a new product

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public Product addProduct(@RequestBody Product product)
    {
        return productDao.create(product);
    }


    // PUT /products/{id}
    // ADMIN ONLY — Update an existing product

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Product updateProduct(@PathVariable int id, @RequestBody Product product)
    {
        // Ensure product exists before updating
        Product existing = productDao.getById(id);

        if (existing == null)
        {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found.");
        }

        // Force the ID from the URL into the object
        product.setProductId(id);

        // Perform update
        productDao.update(id, product);

        // Return updated product
        return productDao.getById(id);
    }


    // DELETE /products/{id}
    // ADMIN ONLY — Delete a product

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProduct(@PathVariable int id)
    {
        Product existing = productDao.getById(id);

        if (existing == null)
        {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found.");
        }

        productDao.delete(id);
    }
}

