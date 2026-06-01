package com.aesthetica.service;

import com.google.gson.JsonObject;
import com.aesthetica.dto.CartDTO;
import com.aesthetica.entity.Cart;
import com.aesthetica.entity.Stock;
import com.aesthetica.entity.User;
import com.aesthetica.util.AppUtil;
import com.aesthetica.util.HibernateUtil;
import com.aesthetica.validation.Validator;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.hibernate.Hibernate; // Import added
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.ArrayList;
import java.util.List;

public class CartService {
    private static final int MINIMUM_PRODUCT_QTY = 0;

    public String addToCart(String prId, String qty, HttpServletRequest request) {
        JsonObject responseObject = new JsonObject();
        boolean status = false;
        String message;

        if (prId == null || !prId.matches(Validator.IS_INTEGER)) {
            message = "Invalid product id!";
        } else if (qty == null || !qty.matches(Validator.IS_INTEGER)) {
            message = "Invalid product quantity!";
        } else if (Integer.parseInt(qty) <= MINIMUM_PRODUCT_QTY) {
            message = "Product quantity must be greater than zero";
        } else {
            int productId = Integer.parseInt(prId);
            int reqQty = Integer.parseInt(qty);

            try (Session hibernateSession = HibernateUtil.getSessionFactory().openSession()) {
                Stock stock = hibernateSession.createQuery("FROM Stock s WHERE s.product.id=:id", Stock.class)
                        .setParameter("id", productId)
                        .uniqueResult();

                if (stock == null) {
                    message = "Product not found in stock!";
                } else if (stock.getQuantity() < reqQty) {
                    message = "Insufficient product quantity!";
                } else {

                    // --- FIX 1: INITIALIZE PROXY ---
                    // This is crucial for Guest (Session) Carts.
                    // We force the Product data to load NOW while the session is open.
                    // If we don't do this, the object stored in HttpSession will have an empty Product proxy.
                    Hibernate.initialize(stock.getProduct());
                    // -------------------------------

                    HttpSession httpSession = request.getSession();
                    User sessionUser = (User) httpSession.getAttribute("user");

                    if (sessionUser == null) {
                        status = true;
                        if (httpSession.getAttribute("sessionCart") == null) {
                            noSessionFirstTime(stock, reqQty, httpSession);
                            message = "Cart updated successfully";
                        } else {
                            return AppUtil.GSON.toJson(noSessionSecondTime(stock, reqQty, httpSession));
                        }
                    } else {
                        status = true;
                        if (httpSession.getAttribute("sessionCart") == null) {
                            return AppUtil.GSON.toJson(loggedWithOutSessionCart(hibernateSession, stock, reqQty, sessionUser));
                        } else {
                            return AppUtil.GSON.toJson(loggedWithSessionCart(hibernateSession, stock, reqQty, httpSession));
                        }
                    }
                }
                } catch (Exception e) {
                    System.err.println("Error updating cart: " + e.getMessage());
                message = "Internal Server Error";
            }
        }

        responseObject.addProperty("status", status);
        responseObject.addProperty("message", message);
        return AppUtil.GSON.toJson(responseObject);
    }

    // ... (loggedWithOutSessionCart, loggedWithSessionCart, noSessionSecondTime, noSessionFirstTime methods remain the same) ...
    // Note: I omitted them here for brevity, but you should keep them exactly as they were in your code.
    // They don't need changes because the 'stock' passed to them is now initialized.

    private JsonObject loggedWithOutSessionCart(Session hibernateSession, Stock stock, int reqQty, User sessionUser) {
        JsonObject response = new JsonObject();
        Transaction transaction = hibernateSession.beginTransaction();
        try {
            User managedUser = hibernateSession.merge(sessionUser);
            Cart existingCart = hibernateSession.createQuery("FROM Cart c WHERE c.user=:user AND c.stock=:stock", Cart.class)
                    .setParameter("user", managedUser)
                    .setParameter("stock", stock)
                    .uniqueResult();

            if (existingCart == null) {
                Cart cart = new Cart();
                cart.setUser(managedUser);
                cart.setStock(stock);
                cart.setQty(reqQty);
                hibernateSession.persist(cart);
                response.addProperty("message", "Product added to cart");
            } else {
                int newQty = existingCart.getQty() + reqQty;
                if (newQty > stock.getQuantity()) {
                    response.addProperty("status", false);
                    response.addProperty("message", "Stock limit exceeded");
                    return response;
                }
                existingCart.setQty(newQty);
                hibernateSession.merge(existingCart);
                response.addProperty("message", "Cart updated");
            }
            transaction.commit();
            response.addProperty("status", true);
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            response.addProperty("status", false);
            response.addProperty("message", "Error updating database cart");
        }
        return response;
    }

    private JsonObject loggedWithSessionCart(Session hibernateSession, Stock stock, int reqQty, HttpSession httpSession) {
        User sessionUser = (User) httpSession.getAttribute("user");
        List<Cart> sessionCart = getSessionAttribute(httpSession);

        if (sessionCart != null) {
            Transaction tx = hibernateSession.beginTransaction();
            for (Cart sc : sessionCart) {
                Stock scStock = hibernateSession.get(Stock.class, sc.getStock().getId());
                User managedUser = hibernateSession.merge(sessionUser);

                Cart dbItem = hibernateSession.createQuery("FROM Cart c WHERE c.user=:user AND c.stock=:stock", Cart.class)
                        .setParameter("user", managedUser)
                        .setParameter("stock", scStock)
                        .uniqueResult();

                if (dbItem == null) {
                    Cart n = new Cart();
                    n.setUser(managedUser);
                    n.setStock(scStock);
                    n.setQty(sc.getQty());
                    hibernateSession.persist(n);
                }
            }
            tx.commit();
            httpSession.setAttribute("sessionCart", null);
        }
        return loggedWithOutSessionCart(hibernateSession, stock, reqQty, sessionUser);
    }

    private JsonObject noSessionSecondTime(Stock stock, int reqQty, HttpSession httpSession) {
        JsonObject response = new JsonObject();
        List<Cart> sessionCart = getSessionAttribute(httpSession);
        Cart existing = sessionCart.stream().filter(c -> c.getStock().getId() == stock.getId()).findFirst().orElse(null);

        if (existing != null) {
            int newQty = existing.getQty() + reqQty;
            if (newQty > stock.getQuantity()) {
                response.addProperty("status", false);
                response.addProperty("message", "Quantity exceeds stock");
            } else {
                existing.setQty(newQty);
                response.addProperty("status", true);
                response.addProperty("message", "Cart updated");
            }
        } else {
            Cart cart = new Cart();
            cart.setId(sessionCart.size() + 1);
            cart.setStock(stock);
            cart.setQty(reqQty);
            sessionCart.add(cart);
            response.addProperty("status", true);
            response.addProperty("message", "Added to cart");
        }
        return response;
    }

    private void noSessionFirstTime(Stock stock, int reqQty, HttpSession httpSession) {
        List<Cart> cartList = new ArrayList<>();
        Cart cart = new Cart();
        cart.setId(1);
        cart.setStock(stock);
        cart.setQty(reqQty);
        cartList.add(cart);
        httpSession.setAttribute("sessionCart", cartList);
    }

    public void mergeSessionCartToLoggedInUser(HttpServletRequest request) {
        HttpSession httpSession = request.getSession(false);
        if (httpSession == null) {
            return;
        }

        User user = (User) httpSession.getAttribute("user");
        if (user == null) {
            return;
        }

        List<Cart> sessionCart = getSessionAttribute(httpSession);
        if (sessionCart == null || sessionCart.isEmpty()) {
            return;
        }

        try (Session hibernateSession = HibernateUtil.getSessionFactory().openSession()) {
            User managedUser = hibernateSession.get(User.class, user.getId());
            if (managedUser == null) {
                return;
            }

            Transaction tx = hibernateSession.beginTransaction();
            try {
                for (Cart sc : sessionCart) {
                    if (sc == null || sc.getStock() == null) {
                        continue;
                    }

                    Stock managedStock = hibernateSession.get(Stock.class, sc.getStock().getId());
                    if (managedStock == null) {
                        continue;
                    }

                    Cart existing = hibernateSession.createQuery("FROM Cart c WHERE c.user=:user AND c.stock=:stock", Cart.class)
                            .setParameter("user", managedUser)
                            .setParameter("stock", managedStock)
                            .uniqueResult();

                    if (existing == null) {
                        Cart newCart = new Cart();
                        newCart.setUser(managedUser);
                        newCart.setStock(managedStock);
                        newCart.setQty(Math.min(sc.getQty(), managedStock.getQuantity()));
                        hibernateSession.persist(newCart);
                    } else {
                        int mergedQty = Math.min(existing.getQty() + sc.getQty(), managedStock.getQuantity());
                        existing.setQty(mergedQty);
                    }
                }

                tx.commit();
                httpSession.removeAttribute("sessionCart");
            } catch (Exception e) {
                if (tx != null) tx.rollback();
                System.err.println("Error merging session cart into user cart: " + e.getMessage());
                System.err.println(e);
            }
        }
    }

    public String LoadCartItems(HttpServletRequest request) {
        JsonObject response = new JsonObject();
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");

        try (Session s = HibernateUtil.getSessionFactory().openSession()) {
            if (user != null && session.getAttribute("sessionCart") != null) {
                mergeSessionCartToLoggedInUser(request);
            }

            List<Cart> list;
            if (user == null) {
                list = getSessionAttribute(session);
            } else {
                // --- FIX 2: JOIN FETCH ---
                // We fetch the Stock and Product eagerly in the query.
                // This prevents the LazyInitializationException when accessing product details later.
                String hql = "SELECT c FROM Cart c " +
                        "JOIN FETCH c.stock s " +
                        "JOIN FETCH s.product p " +
                        "WHERE c.user.id=:uid";

                list = s.createQuery(hql, Cart.class)
                        .setParameter("uid", user.getId())
                        .list();
                // -------------------------
            }

            if (list == null || list.isEmpty()) {
                response.addProperty("status", false);
                response.addProperty("message", "Cart is empty");
            } else {
                response.add("cartList", AppUtil.GSON.toJsonTree(getCartDTOList(list)));
                response.addProperty("status", true);
            }
        }
        return AppUtil.GSON.toJson(response);
    }

    private List<CartDTO> getCartDTOList(List<Cart> cartList) {
        List<CartDTO> dtoList = new ArrayList<>();
        for (Cart cart : cartList) {
            CartDTO dto = new CartDTO();
            dto.setCartId(cart.getId());
            // These lines caused the error before because .getProduct() was a detached proxy
            dto.setProductId(cart.getStock().getProduct().getId());
            dto.setStockId(cart.getStock().getId());
            dto.setTitle(cart.getStock().getProduct().getTitle());
            dto.setPrice(cart.getStock().getPrice());
            dto.setQty(cart.getQty());
            dto.setImages(cart.getStock().getProduct().getImages());
            dtoList.add(dto);
        }
        return dtoList;
    }

    public String removeCartItem(int cartId, HttpServletRequest request) {
        JsonObject response = new JsonObject();
        HttpSession httpSession = request.getSession(false);
        User user = httpSession != null ? (User) httpSession.getAttribute("user") : null;

        if (user != null && httpSession.getAttribute("sessionCart") != null) {
            mergeSessionCartToLoggedInUser(request);
        }

        if (user == null && httpSession != null) {
            List<Cart> sessionCart = getSessionAttribute(httpSession);
            if (sessionCart != null) {
                boolean removed = sessionCart.removeIf(cart -> cart.getId() == cartId);
                if (removed) {
                    if (sessionCart.isEmpty()) {
                        httpSession.setAttribute("sessionCart", null);
                    }
                    response.addProperty("status", true);
                    response.addProperty("message", "Removed");
                } else {
                    response.addProperty("status", false);
                    response.addProperty("message", "Not found");
                }
            } else {
                response.addProperty("status", false);
                response.addProperty("message", "Not found");
            }
            return AppUtil.GSON.toJson(response);
        }

        if (user == null) {
            response.addProperty("status", false);
            response.addProperty("message", "Cart session not found");
            return AppUtil.GSON.toJson(response);
        }

        try (Session s = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = s.beginTransaction();
            try {
                Cart cart = s.createQuery("SELECT c FROM Cart c JOIN FETCH c.stock WHERE c.id=:id AND c.user.id=:uid", Cart.class)
                        .setParameter("id", cartId)
                        .setParameter("uid", user.getId())
                        .uniqueResult();
                if (cart != null) {
                    s.remove(cart);
                    tx.commit();
                    response.addProperty("status", true);
                    response.addProperty("message", "Removed");
                } else {
                    response.addProperty("status", false);
                    response.addProperty("message", "Not found");
                }
            } catch (Exception e) {
                if (tx != null) tx.rollback();
                System.err.println("Error removing cart item: " + e.getMessage());
                System.err.println(e);
                response.addProperty("status", false);
                response.addProperty("message", "Error removing cart item");
            }
        } catch (Exception outer) {
            System.err.println("Session error while removing cart item: " + outer.getMessage());
            System.err.println(outer);
            response.addProperty("status", false);
            response.addProperty("message", "Error removing cart item");
        }
        return AppUtil.GSON.toJson(response);
    }

    public String updateCartItemQuantity(int cartId, int delta, HttpServletRequest request) {
        JsonObject response = new JsonObject();
        HttpSession httpSession = request.getSession(false);
        User user = httpSession != null ? (User) httpSession.getAttribute("user") : null;

        if (user != null && httpSession.getAttribute("sessionCart") != null) {
            mergeSessionCartToLoggedInUser(request);
        }

        if (delta == 0) {
            response.addProperty("status", false);
            response.addProperty("message", "Invalid quantity change");
            return AppUtil.GSON.toJson(response);
        }

        if (httpSession == null) {
            response.addProperty("status", false);
            response.addProperty("message", "Cart session not found");
            return AppUtil.GSON.toJson(response);
        }

        if (user == null) {
            List<Cart> sessionCart = getSessionAttribute(httpSession);
            if (sessionCart == null || sessionCart.isEmpty()) {
                response.addProperty("status", false);
                response.addProperty("message", "Cart is empty");
                return AppUtil.GSON.toJson(response);
            }

            Cart target = sessionCart.stream().filter(cart -> cart.getId() == cartId).findFirst().orElse(null);
            if (target == null) {
                response.addProperty("status", false);
                response.addProperty("message", "Cart item not found");
                return AppUtil.GSON.toJson(response);
            }

            int newQty = target.getQty() + delta;
            int stockQty = target.getStock().getQuantity();
            if (newQty > stockQty) {
                response.addProperty("status", false);
                response.addProperty("message", "Quantity exceeds stock");
            } else if (newQty <= 0) {
                sessionCart.removeIf(cart -> cart.getId() == cartId);
                if (sessionCart.isEmpty()) {
                    httpSession.setAttribute("sessionCart", null);
                }
                response.addProperty("status", true);
                response.addProperty("message", "Removed from cart");
            } else {
                target.setQty(newQty);
                response.addProperty("status", true);
                response.addProperty("message", "Cart updated");
            }
            return AppUtil.GSON.toJson(response);
        }

        try (Session s = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = s.beginTransaction();
            Cart cart = s.createQuery("SELECT c FROM Cart c JOIN FETCH c.stock WHERE c.id=:id AND c.user.id=:uid", Cart.class)
                    .setParameter("id", cartId)
                    .setParameter("uid", user.getId())
                    .uniqueResult();

            if (cart == null) {
                response.addProperty("status", false);
                response.addProperty("message", "Cart item not found");
                return AppUtil.GSON.toJson(response);
            }

            int newQty = cart.getQty() + delta;
            int stockQty = cart.getStock().getQuantity();
            if (newQty > stockQty) {
                response.addProperty("status", false);
                response.addProperty("message", "Quantity exceeds stock");
                return AppUtil.GSON.toJson(response);
            }

            if (newQty <= 0) {
                s.remove(cart);
                response.addProperty("message", "Removed from cart");
            } else {
                cart.setQty(newQty);
                response.addProperty("message", "Cart updated");
            }

            tx.commit();
            response.addProperty("status", true);
            return AppUtil.GSON.toJson(response);
        } catch (Exception e) {
            response.addProperty("status", false);
            response.addProperty("message", "Error updating cart item");
            return AppUtil.GSON.toJson(response);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T getSessionAttribute(HttpSession session) {
        return (T) session.getAttribute("sessionCart");
    }
}