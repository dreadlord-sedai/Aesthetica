package com.aesthetica.service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.aesthetica.dto.ProductDTO;
import com.aesthetica.dto.StockDTO;
import com.aesthetica.entity.*;
import com.aesthetica.util.AppUtil;
import com.aesthetica.util.HibernateUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.ArrayList;
import java.util.List;

public class ProductService {
    private static final int MAX_RESULT = 12;
    private static final int FIRST_RESULT = 0;
    private static final int DEFAULT_PRODUCT_QTY = 100;

    public String addProduct(ProductDTO productDTO, HttpServletRequest request) {
        JsonObject responseObject = new JsonObject();
        boolean status = false;
        String message = "";

        // 1. Basic Validation
        if (productDTO.getTitle() == null || productDTO.getTitle().isBlank()) {
            message = "Product title is required!";
        } else if (productDTO.getDescription() == null || productDTO.getDescription().isBlank()) {
            message = "Product description is required!";
        } else if (productDTO.getPrice() <= 0) {
            message = "Product price must be greater than 0";
        } else {

            // 2. Session & User Verification
            HttpSession httpSession = request.getSession(false);
            if (httpSession == null || httpSession.getAttribute("user") == null) {
                message = "Session expired! Please log in.";
            } else {
                Session hibernateSession = HibernateUtil.getSessionFactory().openSession();

                try {
                    Category category = hibernateSession.find(Category.class, productDTO.getCategory());
                    if (category == null) {
                        message = "Category not found (ID: " + productDTO.getCategory() + ")";
                    } else {

                            Product product = new Product();
                            product.setTitle(productDTO.getTitle());
                            product.setDescription(productDTO.getDescription());
                            product.setCategory(category);
                            product.setSeller(null);

                            product.setWeight(1.0);
                            product.setLength(1.0);
                            product.setWidth(1.0);
                            product.setHeight(1.0);

                            if(productDTO.getImages() != null){
                                product.setImages(productDTO.getImages());
                            }

                            Stock stock = new Stock();
                            stock.setProduct(product);
                            stock.setPrice(productDTO.getPrice());
                                stock.setQuantity(DEFAULT_PRODUCT_QTY);
                                stock.setManufacturedDate(null);
                                stock.setExpiryDate(null);

                            Status activeStatus = hibernateSession.createNamedQuery("Status.findByValue", Status.class)
                                .setParameter("value", String.valueOf(Status.Type.ACTIVE))
                                .setMaxResults(1).uniqueResult();

                            if (activeStatus == null) {
                                activeStatus = new Status();
                                activeStatus.setValue(String.valueOf(Status.Type.ACTIVE));
                                hibernateSession.persist(activeStatus);
                            }

                            stock.setDiscount(null);
                            stock.setStatus(activeStatus);

                            Transaction transaction = hibernateSession.beginTransaction();
                            try {
                                hibernateSession.persist(product);
                                hibernateSession.persist(stock);

                                transaction.commit();
                                status = true;
                                responseObject.addProperty("productId", product.getId());
                                message = "Product added successfully!";

                            } catch (Exception e) {
                                if(transaction != null) transaction.rollback();
                                e.printStackTrace();
                                message = "Error saving product to database.";
                            }
                        }
                } catch (Exception e) {
                    e.printStackTrace();
                    message = "An internal error occurred.";
                } finally {
                    hibernateSession.close();
                }
            }
        }

        responseObject.addProperty("status", status);
        responseObject.addProperty("message", message);
        return AppUtil.GSON.toJson(responseObject);
    }

    public String loadAdvancedSearchData(JsonObject requestObject) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        JsonObject response = new JsonObject();

        try {
            Status approvedStatus = session.createNamedQuery("Status.findByValue", Status.class)
                .setParameter("value", String.valueOf(Status.Type.ACTIVE))
                    .uniqueResult();

            if (approvedStatus == null) {
            approvedStatus = session.createNamedQuery("Status.findByValue", Status.class)
                .setParameter("value", String.valueOf(Status.Type.APPROVED))
                .uniqueResult();
            }

            int firstResult = requestObject.has("firstResult") ? requestObject.get("firstResult").getAsInt() : 0;
            String availability = requestObject.has("availability") ? requestObject.get("availability").getAsString() : "All";
            String sortValue = requestObject.has("sortValue") ? requestObject.get("sortValue").getAsString() : "latest";

            // 1. Extract the search query
            String searchQuery = requestObject.has("searchQuery") ? requestObject.get("searchQuery").getAsString().trim() : "";

            double priceMin = 0;
            double priceMax = Double.MAX_VALUE;

            try {
                double p1 = Double.parseDouble(requestObject.get("priceStart").getAsString());
                double p2 = Double.parseDouble(requestObject.get("priceEnd").getAsString());
                priceMin = Math.min(p1, p2);
                priceMax = Math.max(p1, p2);
            } catch (Exception e) {
                // handle or ignore
            }

            JsonArray categoriesJson = requestObject.getAsJsonArray("categories");
            List<String> categoryList = new ArrayList<>();
            for (JsonElement cat : categoriesJson) {
                categoryList.add(cat.getAsString());
            }

            StringBuilder hql = new StringBuilder("FROM Stock s WHERE s.status = :status AND s.price BETWEEN :priceMin AND :priceMax");

            if ("In Stock".equalsIgnoreCase(availability)) {
                hql.append(" AND s.quantity > 0");
            }
            if ("Out of Stock".equalsIgnoreCase(availability)) {
                hql.append(" AND s.quantity = 0");
            }

            if (!categoryList.isEmpty()) {
                hql.append(" AND s.product.category.name IN (:categories)");
            }

            // 2. Append Search Logic (Case insensitive)
            if (!searchQuery.isEmpty()) {
                hql.append(" AND LOWER(s.product.title) LIKE :searchQuery");
            }

            // --- Execute Count Query ---
            String countHql = "SELECT COUNT(s) " + hql.toString();
            Query<Long> countQuery = session.createQuery(countHql, Long.class);
            countQuery.setParameter("status", approvedStatus);
            countQuery.setParameter("priceMin", priceMin);
            countQuery.setParameter("priceMax", priceMax);

            if (!categoryList.isEmpty()) {
                countQuery.setParameterList("categories", categoryList);
            }

            // 3. Set parameter for Count Query
            if (!searchQuery.isEmpty()) {
                countQuery.setParameter("searchQuery", "%" + searchQuery.toLowerCase() + "%");
            }

            Long allProductCount = countQuery.uniqueResult();
            response.addProperty("allProductCount", allProductCount);


            // --- Execute List Query ---
            switch (sortValue) {
                case "price-asc": hql.append(" ORDER BY s.price ASC"); break;
                case "price-desc": hql.append(" ORDER BY s.price DESC"); break;
                case "alpha-asc": hql.append(" ORDER BY s.product.title ASC"); break;
                case "alpha-desc": hql.append(" ORDER BY s.product.title DESC"); break;
                case "oldest": hql.append(" ORDER BY s.id ASC"); break;
                default: hql.append(" ORDER BY s.id DESC"); break;
            }

            Query<Stock> query = session.createQuery(hql.toString(), Stock.class);
            query.setParameter("status", approvedStatus);
            query.setParameter("priceMin", priceMin);
            query.setParameter("priceMax", priceMax);

            if (!categoryList.isEmpty()) {
                query.setParameterList("categories", categoryList);
            }

            // 4. Set parameter for Main Query
            if (!searchQuery.isEmpty()) {
                query.setParameter("searchQuery", "%" + searchQuery.toLowerCase() + "%");
            }

            query.setFirstResult(firstResult);
            query.setMaxResults(MAX_RESULT); // Make sure MAX_RESULT is defined in your class

            List<Stock> stockList = query.list();

            JsonArray productList = new JsonArray();
            for (Stock stock : stockList) {
                JsonObject productObj = new JsonObject();
                productObj.addProperty("productId", stock.getProduct().getId());
                productObj.addProperty("title", stock.getProduct().getTitle());
                productObj.addProperty("price", stock.getPrice());
                productObj.addProperty("qty", stock.getQuantity());

                JsonArray imgArray = new JsonArray();
                if(stock.getProduct().getImages() != null){
                    for(String img : stock.getProduct().getImages()){
                        imgArray.add(img);
                    }
                }
                productObj.add("images", imgArray);
                productList.add(productObj);
            }

            response.add("productList", productList);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            session.close();
        }

        return new Gson().toJson(response);
    }

    public String singleProduct(int productId) {
        JsonObject responseObject = new JsonObject();
        Session hibernateSession = HibernateUtil.getSessionFactory().openSession();

        try {
            Product product = hibernateSession.get(Product.class, productId);

            if (product != null) {
                responseObject.addProperty("id", product.getId());
                responseObject.addProperty("title", product.getTitle());
                responseObject.addProperty("description", product.getDescription());
                if (product.getCategory() != null) {
                    responseObject.addProperty("category", product.getCategory().getName());
                }
                JsonArray imagesArray = new JsonArray();
                for (String img : product.getImages()) {
                    imagesArray.add(img);
                }
                responseObject.add("images", imagesArray);
                JsonArray stockArray = new JsonArray();
                for (Stock stock : product.getStocks()) {
                    JsonObject stockObj = new JsonObject();
                    stockObj.addProperty("stock_id", stock.getId());
                    stockObj.addProperty("price", stock.getPrice());
                    stockObj.addProperty("quantity", stock.getQuantity());
                    if(stock.getDiscount() != null){
                        stockObj.addProperty("discount", stock.getDiscount().getValue()); // Assuming Discount has getValue()
                    }
                    stockArray.add(stockObj);
                }
                responseObject.add("stockList", stockArray);
                responseObject.addProperty("success", true);

            } else {
                responseObject.addProperty("success", false);
                responseObject.addProperty("message", "Product not found");
            }

        } catch (HibernateException e) {
            e.printStackTrace();
            responseObject.addProperty("success", false);
            responseObject.addProperty("message", "Database Error");
        } finally {
            hibernateSession.close();
        }

        return AppUtil.GSON.toJson(responseObject);
    }

    public String loadProductData() {
        JsonObject responseObject = new JsonObject();
        Session hibernateSession = HibernateUtil.getSessionFactory().openSession();

        List<Category> categoryList = hibernateSession.createQuery("FROM Category c", Category.class).getResultList();

        Double minPrice = hibernateSession.createQuery("SELECT MIN(s.price) FROM Stock s", Double.class).uniqueResult();
        Double maxPrice = hibernateSession.createQuery("SELECT MAX(s.price) FROM Stock s", Double.class).uniqueResult();

        Status approvedStatus = hibernateSession.createNamedQuery("Status.findByValue", Status.class)
            .setParameter("value", String.valueOf(Status.Type.ACTIVE))
                .uniqueResult();

        if (approvedStatus == null) {
            approvedStatus = hibernateSession.createNamedQuery("Status.findByValue", Status.class)
                .setParameter("value", String.valueOf(Status.Type.APPROVED))
                .uniqueResult();
        }
        Query<Stock> query = hibernateSession.createQuery("FROM Stock s WHERE s.status =:status ORDER BY s.id DESC", Stock.class)
                .setParameter("status",approvedStatus);

        responseObject.addProperty("allProductCount",query.getResultList().size());

        query.setFirstResult(ProductService.FIRST_RESULT);
        query.setMaxResults(ProductService.MAX_RESULT);

        List<Stock> stockList = query.getResultList();
        List<ProductDTO> productList = new ArrayList<>();

        for(Stock s:stockList){
            ProductDTO productDTO = new ProductDTO();
            productDTO.setProductId(s.getProduct().getId());
            productDTO.setTitle(s.getProduct().getTitle());
            productDTO.setImages(s.getProduct().getImages());
            productDTO.setPrice(s.getPrice());
            productDTO.setQty(s.getQuantity());
            productList.add(productDTO);
        }
        hibernateSession.close();

        responseObject.add("productList",AppUtil.GSON.toJsonTree(productList));
        responseObject.addProperty("minPrice",minPrice);
        responseObject.addProperty("maxPrice",maxPrice);

        return AppUtil.GSON.toJson(responseObject);
    }
}
