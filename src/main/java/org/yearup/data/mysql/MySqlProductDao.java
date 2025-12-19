package org.yearup.data.mysql;

import org.springframework.stereotype.Component;
import org.yearup.data.ProductDao;
import org.yearup.models.Product;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Component
public class MySqlProductDao extends MySqlDaoBase implements ProductDao
{
    public MySqlProductDao(DataSource dataSource)
    {
        super(dataSource);
    }


    // SEARCH PRODUCTS (FIXED)

    @Override
    public List<Product> search(Integer categoryId,
                                BigDecimal minPrice,
                                BigDecimal maxPrice,
                                String subCategory)
    {
        List<Product> products = new ArrayList<>();

        // Start with a base query that is always true
        StringBuilder sql = new StringBuilder("""
                SELECT product_id, name, price, category_id, description,
                       subcategory, image_url, stock, featured
                FROM products
                WHERE 1 = 1
                """);

        List<Object> params = new ArrayList<>();

        // Apply filters only if provided
        if (categoryId != null)
        {
            sql.append(" AND category_id = ?");
            params.add(categoryId);
        }

        if (minPrice != null)
        {
            sql.append(" AND price >= ?");
            params.add(minPrice);
        }

        if (maxPrice != null)
        {
            sql.append(" AND price <= ?");
            params.add(maxPrice);
        }

        if (subCategory != null && !subCategory.isBlank())
        {
            sql.append(" AND subcategory LIKE ?");
            params.add("%" + subCategory + "%");
        }

        try (Connection connection = getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql.toString()))
        {
            // Bind parameters in order
            for (int i = 0; i < params.size(); i++)
            {
                stmt.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = stmt.executeQuery())
            {
                while (rs.next())
                {
                    products.add(mapRow(rs));
                }
            }
        }
        catch (SQLException e)
        {
            throw new RuntimeException("Error searching products", e);
        }

        return products;
    }


    // LIST PRODUCTS BY CATEGORY

    @Override
    public List<Product> listByCategoryId(int categoryId)
    {
        List<Product> products = new ArrayList<>();

        String sql = """
                SELECT product_id, name, price, category_id, description,
                       subcategory, image_url, stock, featured
                FROM products
                WHERE category_id = ?
                """;

        try (Connection connection = getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql))
        {
            stmt.setInt(1, categoryId);

            try (ResultSet rs = stmt.executeQuery())
            {
                while (rs.next())
                {
                    products.add(mapRow(rs));
                }
            }
        }
        catch (SQLException e)
        {
            throw new RuntimeException("Error listing products by category", e);
        }

        return products;
    }


    // GET PRODUCT BY ID

    @Override
    public Product getById(int productId)
    {
        String sql = """
                SELECT product_id, name, price, category_id, description,
                       subcategory, image_url, stock, featured
                FROM products
                WHERE product_id = ?
                """;

        try (Connection connection = getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql))
        {
            stmt.setInt(1, productId);

            try (ResultSet rs = stmt.executeQuery())
            {
                if (rs.next())
                {
                    return mapRow(rs);
                }
            }
        }
        catch (SQLException e)
        {
            throw new RuntimeException("Error retrieving product", e);
        }

        return null;
    }


    // CREATE PRODUCT

    @Override
    public Product create(Product product)
    {
        String sql = """
                INSERT INTO products (name, price, category_id, description,
                                      subcategory, image_url, stock, featured)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection connection = getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS))
        {
            stmt.setString(1, product.getName());
            stmt.setBigDecimal(2, product.getPrice());
            stmt.setInt(3, product.getCategoryId());
            stmt.setString(4, product.getDescription());
            stmt.setString(5, product.getSubCategory());
            stmt.setString(6, product.getImageUrl());
            stmt.setInt(7, product.getStock());
            stmt.setBoolean(8, product.isFeatured());

            int rows = stmt.executeUpdate();

            if (rows > 0)
            {
                try (ResultSet keys = stmt.getGeneratedKeys())
                {
                    if (keys.next())
                    {
                        int newId = keys.getInt(1);
                        return getById(newId);
                    }
                }
            }
        }
        catch (SQLException e)
        {
            throw new RuntimeException("Error creating product", e);
        }

        return null;
    }


    // UPDATE PRODUCT (FIXED)

    @Override
    public void update(int productId, Product product)
    {
        String sql = """
                UPDATE products
                SET name = ?,
                    price = ?,
                    category_id = ?,
                    description = ?,
                    subcategory = ?,
                    image_url = ?,
                    stock = ?,
                    featured = ?
                WHERE product_id = ?
                """;

        try (Connection connection = getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql))
        {
            stmt.setString(1, product.getName());
            stmt.setBigDecimal(2, product.getPrice());
            stmt.setInt(3, product.getCategoryId());
            stmt.setString(4, product.getDescription());
            stmt.setString(5, product.getSubCategory());
            stmt.setString(6, product.getImageUrl());
            stmt.setInt(7, product.getStock());
            stmt.setBoolean(8, product.isFeatured());
            stmt.setInt(9, productId);

            int rows = stmt.executeUpdate();

            if (rows == 0)
            {
                throw new RuntimeException("Update failed — product not found: " + productId);
            }
        }
        catch (SQLException e)
        {
            throw new RuntimeException("Error updating product", e);
        }
    }


    // DELETE PRODUCT

    @Override
    public void delete(int productId)
    {
        String sql = "DELETE FROM products WHERE product_id = ?";

        try (Connection connection = getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql))
        {
            stmt.setInt(1, productId);
            stmt.executeUpdate();
        }
        catch (SQLException e)
        {
            throw new RuntimeException("Error deleting product", e);
        }
    }


    // MAP RESULTSET → PRODUCT OBJECT

    protected static Product mapRow(ResultSet row) throws SQLException
    {
        return new Product(
                row.getInt("product_id"),
                row.getString("name"),
                row.getBigDecimal("price"),
                row.getInt("category_id"),
                row.getString("description"),
                row.getString("subcategory"),
                row.getInt("stock"),
                row.getBoolean("featured"),
                row.getString("image_url")
        );
    }
}
