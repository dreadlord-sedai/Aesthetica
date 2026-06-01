package com.aesthetica.service;

import com.google.gson.JsonObject;
import com.aesthetica.dto.*;
import com.aesthetica.entity.*;
import com.aesthetica.util.AppUtil;
import com.aesthetica.util.Env;
import com.aesthetica.util.HibernateUtil;
import com.aesthetica.util.PayHereUtil;
import com.aesthetica.validation.Validator;
import jakarta.servlet.http.HttpServletRequest;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.List;

public class CheckoutService {
    private final OrderService orderService = new OrderService();

    public String processCheckout(CheckoutRequestDTO requestDTO, HttpServletRequest request) {
        JsonObject responseObject = new JsonObject();
        boolean status = false;
        String message = "";

        Session hibernateSession = HibernateUtil.getSessionFactory().openSession();
        User sessionUser = (User) request.getSession().getAttribute("user");

        try {
            if (sessionUser == null) {
                // Guest checkout flow: validate request DTO and create an order from session cart
                if (requestDTO.isCurrentAddress()) {
                    // Cannot use "current address" when not logged in
                    message = "Please uncheck 'Current Address' and provide contact details for guest checkout.";
                } else if (requestDTO.getFirstName() == null || requestDTO.getFirstName().isBlank()) {
                    message = "First Name is required!";
                } else if (requestDTO.getLastName() == null || requestDTO.getLastName().isBlank()) {
                    message = "Last Name is required!";
                } else if (requestDTO.getCitySelect() == AppUtil.DEFAULT_SELECTOR_VALUE) {
                    message = "Please select a city!";
                } else if (requestDTO.getLineOne() == null || requestDTO.getLineOne().isBlank()) {
                    message = "Address line one is required!";
                } else if (requestDTO.getPostalCode() == null || requestDTO.getPostalCode().isBlank()) {
                    message = "Postal code is required!";
                } else if (!requestDTO.getPostalCode().matches(Validator.POSTAL_CODE_VALIDATION)) {
                    message = "Enter a valid postal code!";
                } else if (requestDTO.getMobile() == null || requestDTO.getMobile().isBlank()) {
                    message = "Mobile number is required!";
                } else if (!requestDTO.getMobile().matches(Validator.MOBILE_VALIDATION)) {
                    message = "Enter a valid mobile number!";
                } else {
                    // Get session cart
                    @SuppressWarnings("unchecked")
                    List<Cart> sessionCart = (List<Cart>) request.getSession().getAttribute("sessionCart");
                    if (sessionCart == null || sessionCart.isEmpty()) {
                        message = "Cart is empty";
                    } else {
                        // create pending order and items from session cart
                        Transaction tx = hibernateSession.beginTransaction();
                        try {
                            Status pendingStatus = hibernateSession.createNamedQuery("Status.findByValue", Status.class)
                                    .setParameter("value", String.valueOf(Status.Type.PENDING))
                                    .getSingleResult();

                            Order order = new Order();
                            order.setStatus(pendingStatus);
                            // No user attached for guest order
                            hibernateSession.persist(order);

                            for (Cart sc : sessionCart) {
                                Stock managedStock = hibernateSession.find(Stock.class, sc.getStock().getId());
                                if (managedStock == null) continue;
                                OrderItem orderItem = new OrderItem();
                                orderItem.setOrder(order);
                                orderItem.setQuantity(sc.getQty());
                                orderItem.setRating(AppUtil.DEFAULT_RATING_VALUE);
                                orderItem.setStock(managedStock);
                                if (managedStock.getProduct() != null) {
                                    orderItem.setSeller(managedStock.getProduct().getSeller());
                                }
                                hibernateSession.persist(orderItem);
                            }

                            tx.commit();
                            // clear session cart
                            request.getSession().setAttribute("sessionCart", null);

                            // create payment details using guest info from requestDTO
                            PayHereDTO paymentDetails = createPaymentDetails(hibernateSession, order, requestDTO);
                            responseObject.add("paymentDetails", AppUtil.GSON.toJsonTree(paymentDetails));
                            status = true;
                        } catch (Exception e) {
                            if (tx != null) tx.rollback();
                            System.err.println("Error creating guest order: " + e.getMessage());
                            message = "Failed to create order. Please try again.";
                        }
                    }
                }
            } else {
                // Logged-in user flow (existing behavior)
                User dbUser = hibernateSession.find(User.class, sessionUser.getId());
                if (requestDTO.isCurrentAddress()) {
                    Address address = hibernateSession.createQuery("FROM Address a WHERE a.user=:user AND a.primary=:primary", Address.class)
                            .setParameter("user", dbUser)
                            .setParameter("primary", requestDTO.isCurrentAddress())
                            .getSingleResultOrNull();
                    if (address == null) {
                        message = "Address not found. Please check again!";
                    } else {
                        // Order pending method call here
                        Order pendingOrder = orderService.createPendingOrder(dbUser, hibernateSession);
                        PayHereDTO paymentDetails = createPaymentDetails(hibernateSession, pendingOrder, null);
                        responseObject.add("paymentDetails", AppUtil.GSON.toJsonTree(paymentDetails));
                        status = true;
                    }
                } else {
                    if (requestDTO.getFirstName().isBlank()) {
                        message = "First Name is required!";
                    } else if (requestDTO.getLastName().isBlank()) {
                        message = "Last Name is required!";
                    } else if (requestDTO.getCitySelect() == AppUtil.DEFAULT_SELECTOR_VALUE) {
                        message = "Please select a city!";
                    } else if (requestDTO.getLineOne().isBlank()) {
                        message = "Address line one is required!";
                    } else if (requestDTO.getPostalCode().isBlank()) {
                        message = "Postal code is required!";
                    } else if (!requestDTO.getPostalCode().matches(Validator.POSTAL_CODE_VALIDATION)) {
                        message = "Enter a valid postal code!";
                    } else if (requestDTO.getMobile().isBlank()) {
                        message = "Mobile number is required!";
                    } else if (!requestDTO.getMobile().matches(Validator.MOBILE_VALIDATION)) {
                        message = "Enter a valid mobile number!";
                    } else {
                        City city = hibernateSession.find(City.class, requestDTO.getCitySelect());
                        if (city == null) {
                            message = "City not found. Select correct city!";
                        } else {
                            Address existingPrimary = hibernateSession.createQuery("FROM Address a WHERE a.user=:user AND a.primary=:primary", Address.class)
                                    .setParameter("user", dbUser)
                                    .setParameter("primary", true)
                                    .getSingleResultOrNull();
                            if (existingPrimary != null) { // primary address already exists.
                                existingPrimary.setPrimary(false);
                                hibernateSession.merge(existingPrimary);
                            }
                            Address address = new Address();
                            address.setPrimary(true);
                            address.setLineOne(requestDTO.getLineOne());
                            address.setLineTwo(requestDTO.getLineTwo());
                            address.setPostalCode(requestDTO.getPostalCode());
                            address.setCity(city);
                            address.setUser(dbUser);
                            hibernateSession.persist(address);
                            // Order pending method call here
                            Order pendingOrder = orderService.createPendingOrder(dbUser, hibernateSession);
                            PayHereDTO paymentDetails = createPaymentDetails(hibernateSession, pendingOrder, null);
                            responseObject.add("paymentDetails", AppUtil.GSON.toJsonTree(paymentDetails));
                            status = true;
                        }
                    }
                }
            }
        } finally {
            // close session
            if (hibernateSession != null && hibernateSession.isOpen()) {
                hibernateSession.close();
            }
        }

        responseObject.addProperty("status", status);
        responseObject.addProperty("message", message);
        return AppUtil.GSON.toJson(responseObject);
    }



    private PayHereDTO createPaymentDetails(Session hibernateSession, Order o, CheckoutRequestDTO requestDTO) {
        String orderId = "000" + o.getId();
        String returnURL = Env.get("app.public.url") + "/api/payments/return";
        String cancelURL = Env.get("app.public.url") + "/api/payments/cancel";
        String notifyURL = Env.get("app.public.url") + "/api/payments/notify";

        Order order = hibernateSession.find(Order.class, o.getId());
        User user = null;
        Address address = null;
        String buyerCityName = null;

        if (order.getUser() != null) {
            user = hibernateSession.find(User.class, order.getUser().getId());
            address = hibernateSession.createQuery("FROM Address a WHERE a.user=:user AND a.primary=:primary", Address.class)
                    .setParameter("user", user)
                    .setParameter("primary", true)
                    .getSingleResult();
            if (address != null && address.getCity() != null) {
                buyerCityName = address.getCity().getName();
            }
        } else if (requestDTO != null) {
            // Guest: build address details from requestDTO
            City city = hibernateSession.find(City.class, requestDTO.getCitySelect());
            if (city != null) buyerCityName = city.getName();
            // create a simple Address-like string from DTO
            address = new Address();
            address.setLineOne(requestDTO.getLineOne());
            address.setLineTwo(requestDTO.getLineTwo() == null ? "" : requestDTO.getLineTwo());
            address.setPostalCode(requestDTO.getPostalCode());
            address.setCity(city);
        }

        // build items and compute amount

        StringBuilder items = new StringBuilder();
        double amount = 0;
        List<OrderItem> orderItems = hibernateSession.createQuery("FROM OrderItem oi WHERE oi.order=:order", OrderItem.class)
                .setParameter("order", order)
                .getResultList();

        DeliveryType withinCity = hibernateSession.createNamedQuery("DeliveryType.findByName", DeliveryType.class)
                .setParameter("name", String.valueOf(DeliveryType.Value.WITHIN_CITY)).getSingleResult();
        DeliveryType outOfCity = hibernateSession.createNamedQuery("DeliveryType.findByName", DeliveryType.class)
                .setParameter("name", String.valueOf(DeliveryType.Value.OUT_OF_CITY)).getSingleResult();

        for (OrderItem orderItem : orderItems) {
            if (!items.isEmpty()) {
                items.append(",");
            }
            items.append(orderItem.getStock().getProduct().getTitle())
                    .append(" x ")
                    .append(orderItem.getQuantity());
            amount += orderItem.getStock().getPrice() * orderItem.getQuantity();
            if (orderItem.getSeller() != null && orderItem.getSeller().getUser() != null) {
                User seller = orderItem.getSeller().getUser();
                Address sellerAddress = hibernateSession.createQuery("FROM Address a WHERE a.user=:user AND a.primary=true", Address.class)
                        .setParameter("user", seller)
                        .getSingleResultOrNull();
                if (sellerAddress != null && buyerCityName != null && sellerAddress.getCity() != null) {
                    if (buyerCityName.equals(sellerAddress.getCity().getName())) {
                        amount += withinCity.getPrice();
                    } else {
                        amount += outOfCity.getPrice();
                    }
                }
            }
        }
//
        String hashValue = PayHereUtil.generateHash(orderId, amount);
        PayHereDTO payHereDTO = new PayHereDTO();
        payHereDTO.setSandbox(true);
        payHereDTO.setMerchant_id(PayHereUtil.getMerchantId());
        payHereDTO.setReturn_url(returnURL);
        payHereDTO.setCancel_url(cancelURL);
        payHereDTO.setNotify_url(notifyURL);
        payHereDTO.setOrder_id(orderId);
        payHereDTO.setItems(items.toString());
        payHereDTO.setAmount(String.format(java.util.Locale.US, "%.2f", amount));
        payHereDTO.setCurrency(PayHereUtil.APP_CURRENCY);
        payHereDTO.setHash(hashValue);
        if (user != null) {
            payHereDTO.setFirst_name(user.getFirstName());
            payHereDTO.setLast_name(user.getLastName());
            payHereDTO.setEmail(user.getEmail());
            payHereDTO.setPhone(user.getMobile());
            StringBuilder userAddress = new StringBuilder();
            if (address != null) {
                userAddress.append(address.getLineOne());
                if (!address.getLineTwo().isBlank()) userAddress.append(",").append(address.getLineTwo());
            }
            payHereDTO.setAddress(userAddress.toString());
            if (address != null && address.getCity() != null) payHereDTO.setCity(address.getCity().getName());
        } else if (requestDTO != null) {
            payHereDTO.setFirst_name(requestDTO.getFirstName());
            payHereDTO.setLast_name(requestDTO.getLastName());
            // generate guest email from mobile to satisfy non-null email column expectations in downstream
            String guestEmail = requestDTO.getMobile().replaceAll("\\D", "") + "@guest.aesthetica";
            payHereDTO.setEmail(guestEmail);
            payHereDTO.setPhone(requestDTO.getMobile());
            StringBuilder guestAddress = new StringBuilder(requestDTO.getLineOne());
            if (requestDTO.getLineTwo() != null && !requestDTO.getLineTwo().isBlank()) guestAddress.append(",").append(requestDTO.getLineTwo());
            payHereDTO.setAddress(guestAddress.toString());
            City city = hibernateSession.find(City.class, requestDTO.getCitySelect());
            if (city != null) payHereDTO.setCity(city.getName());
        }
        payHereDTO.setCountry(PayHereUtil.APP_COUNTRY);
        return payHereDTO;
    }
}
