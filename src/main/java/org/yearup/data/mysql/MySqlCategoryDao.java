package org.yearup.data.mysql;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.yearup.data.CategoryDao;
import org.yearup.models.Category;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Component
public class MySqlCategoryDao extends MySqlDaoBase implements CategoryDao
{
    public MySqlCategoryDao(DataSource dataSource)
    {
        super(dataSource);
    }


    // GET ALL CATEGORIES

    @Override
    public List<Category> getAllCategories()
    {
        List<Category> categories = new ArrayList<>();

        String sql = """
                SELECT category_id, name, description
                FROM categories
                ORDER BY category_id;
                """;

        try (Connection connection = getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery())
        {
            while (rs.next())
            {
                categories.add(mapRow(rs));
            }
        }
        catch (SQLException e)
        {
            throw new RuntimeException("Unable to retrieve categories.", e);
        }

        return categories;
    }


    // GET CATEGORY BY ID

    @Override
    public Category getById(int categoryId)
    {
        String sql = """
                SELECT category_id, name, description
                FROM categories
                WHERE category_id = ?;
                """;

        try (Connection connection = getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql))
        {
            stmt.setInt(1, categoryId);

            try (ResultSet rs = stmt.executeQuery())
            {
                if (rs.next())
                {
                    return mapRow(rs);
                }
                else
                {
                    throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "Category not found with id: " + categoryId);
                }
            }
        }
        catch (SQLException e)
        {
            throw new RuntimeException("Unable to retrieve category with id: " + categoryId, e);
        }
    }


    // CREATE CATEGORY

    @Override
    public Category create(Category category)
    {
        String sql = """
                INSERT INTO categories (name, description)
                VALUES (?, ?);
                """;

        try (Connection connection = getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS))
        {
            stmt.setString(1, category.getName());
            stmt.setString(2, category.getDescription());

            int rows = stmt.executeUpdate();

            if (rows == 0)
            {
                throw new RuntimeException("Failed to insert category — no rows affected.");
            }

            try (ResultSet keys = stmt.getGeneratedKeys())
            {
                if (keys.next())
                {
                    category.setCategoryId(keys.getInt(1));
                }
            }
        }
        catch (SQLException e)
        {
            throw new RuntimeException("Unable to create category.", e);
        }

        return category;
    }


    // UPDATE CATEGORY

    @Override
    public Category update(int categoryId, Category category)
    {
        String sql = """
                UPDATE categories
                SET name = ?, description = ?
                WHERE category_id = ?;
                """;

        try (Connection connection = getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql))
        {
            stmt.setString(1, category.getName());
            stmt.setString(2, category.getDescription());
            stmt.setInt(3, categoryId);

            int rows = stmt.executeUpdate();

            if (rows == 0)
            {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Cannot update — category not found with id: " + categoryId);
            }

            category.setCategoryId(categoryId);
            return category;
        }
        catch (SQLException e)
        {
            throw new RuntimeException("Unable to update category with id: " + categoryId, e);
        }
    }


    // DELETE CATEGORY

    @Override
    public void delete(int categoryId)
    {
        String sql = "DELETE FROM categories WHERE category_id = ?;";

        try (Connection connection = getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql))
        {
            stmt.setInt(1, categoryId);

            int rows = stmt.executeUpdate();

            if (rows == 0)
            {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Cannot delete — category not found with id: " + categoryId);
            }
        }
        catch (SQLException e)
        {
            throw new RuntimeException("Unable to delete category with id: " + categoryId, e);
        }
    }


    // MAP RESULTSET → CATEGORY OBJECT

    private Category mapRow(ResultSet rs) throws SQLException
    {
        Category category = new Category();

        category.setCategoryId(rs.getInt("category_id"));
        category.setName(rs.getString("name"));
        category.setDescription(rs.getString("description"));

        return category;
    }
}
