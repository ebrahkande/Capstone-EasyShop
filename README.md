🛒 E-Commerce API - Backend Capstone
This project is a Spring Boot REST API for an e-commerce platform. I took on the role of a backend developer to enhance an existing codebase by implementing new administrative features and fixing critical logic bugs in the product management system.

🚀 Features Implemented
Phase 1: Categories Management
I implemented the CategoriesController to provide full CRUD (Create, Read, Update, Delete) functionality for product categories.

Public Access: Anyone can view all categories or a specific category by ID.

Admin Access: Secured using Spring Security, only users with the ADMIN role can create, update, or delete categories.

Phase 2: Logic Fixes & Bug Squashing
Product Search Bug: Fixed the search/filtering logic in the ProductDao. The system now correctly handles optional parameters for Category, Price Range, and Sub-category, allowing for dynamic and accurate search results.

Product Duplication Fix: Resolved an issue where updating a product would accidentally create a new entry. I modified the PUT request logic to ensure the database executes an UPDATE statement targeting the specific Product ID rather than an INSERT.

🛠️ Tech Stack
Java 17

Spring Boot (Web, Security, Data JDBC)

MySQL (Database)

Maven (Dependency Management)

💻 Code Highlight: Dynamic Search Logic
One of the most interesting pieces of code is the dynamic SQL builder in the ProductDao. This allows the API to filter products only by the criteria the user actually provides, avoiding "null" errors in the database.

🧪 Testing with Insomnia
To ensure the API is working correctly, I used Insomnia to test each endpoint.

Categories:

GET /categories - Verify all categories return.

POST /categories - Verify 401 Unauthorized for guests and 201 Created for Admins.

Product Search:

GET /products?minPrice=10&maxPrice=50 - Verify the price filter logic.

Product Update:

PUT /products/{id} - Verify the product name/price changes without creating a new ID.

