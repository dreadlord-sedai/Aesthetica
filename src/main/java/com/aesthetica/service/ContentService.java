package com.aesthetica.service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.aesthetica.dto.ProductDTO;
import com.aesthetica.dto.StockDTO;
import com.aesthetica.entity.*;
import com.aesthetica.util.AppUtil;
import com.aesthetica.util.HibernateUtil;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.ArrayList;
import java.util.List;

public class ContentService {

    public String addCategory(String name, String iconPath) {
        JsonObject responseObject = new JsonObject();
        responseObject.addProperty("success", false);

        String categoryName = name != null ? name.trim() : "";
        String icon = iconPath != null ? iconPath.trim() : "";

        if (categoryName.isBlank()) {
            responseObject.addProperty("message", "Category name is required");
            return AppUtil.GSON.toJson(responseObject);
        }

        if (icon.isBlank()) {
            responseObject.addProperty("message", "Category icon path is required");
            return AppUtil.GSON.toJson(responseObject);
        }

        try (Session hibernateSession = HibernateUtil.getSessionFactory().openSession()) {
            Category existingCategory = hibernateSession.createQuery(
                    "FROM Category c WHERE lower(c.name) = :name", Category.class)
                    .setParameter("name", categoryName.toLowerCase())
                    .setMaxResults(1)
                    .uniqueResult();

            if (existingCategory != null) {
                responseObject.addProperty("message", "Category already exists");
                return AppUtil.GSON.toJson(responseObject);
            }

            Transaction transaction = hibernateSession.beginTransaction();
            try {
                Category category = new Category();
                category.setName(categoryName);
                category.setPath(icon);

                hibernateSession.persist(category);
                transaction.commit();

                responseObject.addProperty("success", true);
                responseObject.addProperty("message", "Category added successfully");
            } catch (Exception e) {
                if (transaction != null) {
                    transaction.rollback();
                }
                responseObject.addProperty("message", "Failed to add category");
            }
        } catch (Exception e) {
            responseObject.addProperty("message", "Unable to process category request");
        }

        return AppUtil.GSON.toJson(responseObject);
    }

    public String loadCategories() {
        JsonObject responseObject = new JsonObject();
        JsonArray categoryArray = new JsonArray();

        // Use try-with-resources to ensure session closes
        try (Session hibernateSession = HibernateUtil.getSessionFactory().openSession()) {

            // Ensure "path" matches the field name in your Category entity class
            String hql = "SELECT c.id, c.name, c.path FROM Category c";
            Query<Object[]> query = hibernateSession.createQuery(hql, Object[].class);
            List<Object[]> results = query.list();

            for (Object[] row : results) {
                JsonObject categoryJson = new JsonObject();
                categoryJson.addProperty("id", String.valueOf(row[0]));
                categoryJson.addProperty("name", String.valueOf(row[1]));
                categoryJson.addProperty("icon", String.valueOf(row[2]));
                categoryArray.add(categoryJson);
            }

            responseObject.addProperty("success", true);
            responseObject.add("categories", categoryArray);

        } catch (Exception e) {
            e.printStackTrace(); // Log it so you can see it in the console
            responseObject.addProperty("success", false);
            responseObject.addProperty("message", e.getMessage());
        }

        return new Gson().toJson(responseObject);
    }

    public String loadTeaGallery() {
        JsonArray teaArray = new JsonArray();
        try (Session hibernateSession = HibernateUtil.getSessionFactory().openSession()) {
            // Prefer newer products (id >= 4), but fall back to all products if needed.
            String hql = "SELECT DISTINCT p FROM Product p " +
                    "LEFT JOIN FETCH p.images " +
                    "LEFT JOIN FETCH p.stocks " +
                    "WHERE p.id >= :minId AND EXISTS (SELECT 1 FROM Stock s WHERE s.product = p AND s.quantity > 0) " +
                    "ORDER BY p.id DESC";

            List<Product> products = hibernateSession.createQuery(hql, Product.class)
                    .setParameter("minId", 4)
                    .setMaxResults(6)
                    .list();

            if (products.isEmpty()) {
                String fallbackHql = "SELECT DISTINCT p FROM Product p " +
                        "LEFT JOIN FETCH p.images " +
                        "LEFT JOIN FETCH p.stocks " +
                        "WHERE EXISTS (SELECT 1 FROM Stock s WHERE s.product = p AND s.quantity > 0) " +
                        "ORDER BY p.id DESC";
                products = hibernateSession.createQuery(fallbackHql, Product.class)
                        .setMaxResults(6)
                        .list();
            }

            for (Product p : products) {
                JsonObject productJson = new JsonObject();
                productJson.addProperty("id", p.getId());
                productJson.addProperty("productId", p.getId());
                productJson.addProperty("name", p.getTitle());
                productJson.addProperty("title", p.getTitle());
                productJson.addProperty("description", p.getDescription());
                Stock stock = p.getStocks().stream().findFirst().orElse(null);
                productJson.addProperty("price", stock != null ? stock.getPrice() : 0.0);

                // Get the first image URL (or a placeholder if empty)
                String imageUrl = "images/placeholder.jpg";
                if (p.getImages() != null && !p.getImages().isEmpty()) {
                    imageUrl = p.getImages().get(0);
                }
                productJson.addProperty("image", imageUrl);

                teaArray.add(productJson);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return teaArray.toString();
    }

    public String loadFreshDeals() {
        JsonObject responseObject = new JsonObject();

        List<ProductDTO> productDTOList = null;
        try {
            Session hibernateSession = HibernateUtil.getSessionFactory().openSession();
            List<Product> productList = hibernateSession.createQuery("SELECT DISTINCT p FROM Product p JOIN p.stocks s WHERE s.quantity > 0 ORDER BY p.createdAt DESC", Product.class)
                    .setMaxResults(8)
                    .getResultList();

            productDTOList = new ArrayList<>();

            for (Product product : productList) {
                ProductDTO productDTO = new ProductDTO();
                productDTO.setProductId(product.getId());
                productDTO.setTitle(product.getTitle());
                productDTO.setImages(product.getImages());
                productDTO.setCategory(product.getCategory().getId());
                List<StockDTO> stockDTOList = new ArrayList<>();
                for (Stock stock : product.getStocks()) {
                    StockDTO stockDTO = new StockDTO();
                    stockDTO.setProductId(product.getId());
                    stockDTO.setStockId(stock.getId());
                    stockDTO.setQty(stock.getQuantity());
                    stockDTO.setPrice(stock.getPrice());
                    stockDTOList.add(stockDTO);
                }
                productDTO.setStockDTOList(stockDTOList);
                productDTOList.add(productDTO);
            }

            hibernateSession.close();
        } catch (HibernateException e) {
            throw new RuntimeException(e);
        }
        responseObject.add("newArrivals", AppUtil.GSON.toJsonTree(productDTOList));
        return AppUtil.GSON.toJson(responseObject);
    }

}
