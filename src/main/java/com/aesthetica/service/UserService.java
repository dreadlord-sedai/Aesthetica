package com.aesthetica.service;

import com.google.gson.JsonObject;
import com.aesthetica.dto.UserDTO;
import com.aesthetica.entity.Status;
import com.aesthetica.entity.User;
import com.aesthetica.util.AppUtil;
import com.aesthetica.util.HibernateUtil;
import com.aesthetica.validation.Validator;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.ws.rs.core.Context;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;

public class UserService {

    public String addNewUser(UserDTO userDTO) {

        boolean status = false;
        JsonObject responseObject = new JsonObject();
        responseObject.addProperty("status", false);
        String message;

        if (userDTO.getFirstName() == null) {
            message = "First Name is required";
        } else if (userDTO.getFirstName().isBlank()) {
            message = "First Name can not be empty";
        } else if (userDTO.getLastName() == null) {
            message = "Last Name is required";
        } else if (userDTO.getLastName().isBlank()) {
            message = "Last Name can not be empty";
        } else if (userDTO.getEmail() == null) {
            message = "Email is required";
        } else if (userDTO.getEmail().isBlank()) {
            message = "Email is can not be empty";
        } else if (!userDTO.getEmail().matches(Validator.EMAIL_VALIDATION)) {
            message = "Please provide valid email.";
        } else if (userDTO.getMobile() == null) {
            message = "Mobile number is required";
        } else if (userDTO.getMobile().isBlank()) {
            message = "Mobile number can not be empty";
        } else if (!userDTO.getMobile().matches(Validator.MOBILE_VALIDATION)) {
            message = "Check mobile number again";
        } else if (userDTO.getPassword() == null) {
            message = "Password is required";
        } else if (userDTO.getPassword().isBlank()) {
            message = "Password can not be empty";
        } else if (userDTO.getConfirmPassword() == null) {
            message = "Confirm password is required";
        } else if (!userDTO.getPassword().equals(userDTO.getConfirmPassword())) {
            message = "Passwords do not match";
        } else if (!userDTO.getPassword().matches(Validator.PASSWORD_VALIDATION)) {
            message = "Please provide valid password. \n" +
                    "The password must contains at least one capital letter, one simple letter, " +
                    "one digit, one special character and password must be greater that 8 characters";
        } else {
            Session hibernateSession = HibernateUtil.getSessionFactory().openSession();
            User singleUser = hibernateSession.createNamedQuery("User.getByEmail", User.class).
                    setParameter("email", userDTO.getEmail())
                    .getSingleResultOrNull();
            if (singleUser != null) {
                message = "Email is already registered";
            } else {
                User u = new User();
                u.setFirstName(userDTO.getFirstName());
                u.setLastName(userDTO.getLastName());
                u.setMobile(userDTO.getMobile());
                u.setEmail(userDTO.getEmail());
                u.setPassword(userDTO.getPassword());

                Status activeStatus = hibernateSession.createNamedQuery("Status.findByValue", Status.class)
                    .setParameter("value", String.valueOf(Status.Type.ACTIVE)).getSingleResultOrNull();

                if (activeStatus == null) {
                    activeStatus = new Status();
                    activeStatus.setValue(String.valueOf(Status.Type.ACTIVE));
                }

                u.setStatus(activeStatus);

                Transaction transaction = hibernateSession.beginTransaction();

                try {
                    if (activeStatus.getId() == 0) {
                        hibernateSession.persist(activeStatus);
                    }
                    hibernateSession.persist(u);
                    transaction.commit();
                    status = true;
                    message = "Account created successfully. You can sign in now.";
                } catch (HibernateException e) {
                    transaction.rollback();
                    message = "Account Creation failed.Please try again!";
                }
            }
            hibernateSession.close();
        }
        responseObject.addProperty("status", status);
        responseObject.addProperty("message", message);
        return AppUtil.GSON.toJson(responseObject);
    }

    public String addNewUserAndLogin(UserDTO userDTO, @Context HttpServletRequest request) {
        // First validate and create the user using the same logic as addNewUser
        boolean status = false;
        JsonObject responseObject = new JsonObject();
        String message;

        if (userDTO.getFirstName() == null) {
            message = "First Name is required";
        } else if (userDTO.getFirstName().isBlank()) {
            message = "First Name can not be empty";
        } else if (userDTO.getLastName() == null) {
            message = "Last Name is required";
        } else if (userDTO.getLastName().isBlank()) {
            message = "Last Name can not be empty";
        } else if (userDTO.getEmail() == null) {
            message = "Email is required";
        } else if (userDTO.getEmail().isBlank()) {
            message = "Email is can not be empty";
        } else if (!userDTO.getEmail().matches(Validator.EMAIL_VALIDATION)) {
            message = "Please provide valid email.";
        } else if (userDTO.getMobile() == null) {
            message = "Mobile number is required";
        } else if (userDTO.getMobile().isBlank()) {
            message = "Mobile number can not be empty";
        } else if (!userDTO.getMobile().matches(Validator.MOBILE_VALIDATION)) {
            message = "Check mobile number again";
        } else if (userDTO.getPassword() == null) {
            message = "Password is required";
        } else if (userDTO.getPassword().isBlank()) {
            message = "Password can not be empty";
        } else if (userDTO.getConfirmPassword() == null) {
            message = "Confirm password is required";
        } else if (!userDTO.getPassword().equals(userDTO.getConfirmPassword())) {
            message = "Passwords do not match";
        } else if (!userDTO.getPassword().matches(Validator.PASSWORD_VALIDATION)) {
            message = "Please provide valid password. \n" +
                    "The password must contains at least one capital letter, one simple letter, " +
                    "one digit, one special character and password must be greater that 8 characters";
        } else {
            Session hibernateSession = HibernateUtil.getSessionFactory().openSession();
            User singleUser = hibernateSession.createNamedQuery("User.getByEmail", User.class)
                    .setParameter("email", userDTO.getEmail())
                    .getSingleResultOrNull();
            if (singleUser != null) {
                message = "Email is already registered";
            } else {
                User u = new User();
                u.setFirstName(userDTO.getFirstName());
                u.setLastName(userDTO.getLastName());
                u.setMobile(userDTO.getMobile());
                u.setEmail(userDTO.getEmail());
                u.setPassword(userDTO.getPassword());

                Status activeStatus = hibernateSession.createNamedQuery("Status.findByValue", Status.class)
                        .setParameter("value", String.valueOf(Status.Type.ACTIVE)).getSingleResultOrNull();

                if (activeStatus == null) {
                    activeStatus = new Status();
                    activeStatus.setValue(String.valueOf(Status.Type.ACTIVE));
                }

                u.setStatus(activeStatus);

                Transaction transaction = hibernateSession.beginTransaction();

                try {
                    if (activeStatus.getId() == 0) {
                        hibernateSession.persist(activeStatus);
                    }
                    hibernateSession.persist(u);
                    transaction.commit();

                    // Auto-login: set user in session
                    HttpSession httpSession = request.getSession();
                    httpSession.setAttribute("user", u);

                    // Merge any guest session cart into the new user's account
                    try {
                        new CartService().mergeSessionCartToLoggedInUser(request);
                    } catch (Exception cartMergeError) {
                        System.err.println("Sign-up succeeded, but cart merge failed: " + cartMergeError.getMessage());
                        System.err.println(cartMergeError);
                    }

                    status = true;
                    message = "Account created successfully. You are now signed in!";
                } catch (HibernateException e) {
                    transaction.rollback();
                    message = "Account Creation failed. Please try again!";
                }
            }
            hibernateSession.close();
        }
        responseObject.addProperty("status", status);
        responseObject.addProperty("message", message);
        return AppUtil.GSON.toJson(responseObject);
    }

    public String userLogin(UserDTO userDTO,  @Context HttpServletRequest request) {
        boolean status = false;
        JsonObject responseObject = new JsonObject();
        String message;

        if (userDTO.getEmail() == null) {
            message = "Email is required";
        } else if (userDTO.getEmail().isBlank()) {
            message = "Email is required";
        } else if (!userDTO.getEmail().matches(Validator.EMAIL_VALIDATION)) {
            message = "Please provide valid email.";
        } else if (userDTO.getPassword() == null) {
            message = "Password is required";
        } else if (userDTO.getPassword().isBlank()) {
            message = "Password is required";
        } else {
            Session hibernateSession = HibernateUtil.getSessionFactory().openSession();
            User singleUser = hibernateSession.createNamedQuery("User.getByEmail", User.class)
                    .setParameter("email", userDTO.getEmail())
                    .getSingleResultOrNull();
            if (singleUser == null) {
                message = "A user with this email does not exist please register";
            }else  {
                if(!singleUser.getPassword().equals(userDTO.getPassword())) {
                    message = "Please check your login credentials.";
                }else  {
                    HttpSession httpSession = request.getSession();
                    httpSession.setAttribute("user", singleUser);
                    try {
                        new CartService().mergeSessionCartToLoggedInUser(request);
                    } catch (Exception cartMergeError) {
                        System.err.println("Login succeeded, but cart merge failed: " + cartMergeError.getMessage());
                        System.err.println(cartMergeError);
                    }
                    status = true;
                    message = "Login Successful";

                }
            }
            hibernateSession.close();
        }

        responseObject.addProperty("status", status);
        responseObject.addProperty("message", message);
        return AppUtil.GSON.toJson(responseObject);
    }
}
