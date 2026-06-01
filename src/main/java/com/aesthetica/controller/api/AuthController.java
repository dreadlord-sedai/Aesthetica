package com.aesthetica.controller.api;

import com.google.gson.JsonObject;
import com.aesthetica.util.AppUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/auth")
public class AuthController {

    @GET
    @Path("/admin-status")
    @Produces(MediaType.APPLICATION_JSON)
    public Response adminStatus(@Context HttpServletRequest request) {
        JsonObject json = new JsonObject();
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("user") != null) {
            Boolean isSeller = (Boolean) session.getAttribute("isSeller");
            json.addProperty("isSeller", isSeller != null && isSeller);
            json.addProperty("loggedIn", true);
        } else {
            json.addProperty("isSeller", false);
            json.addProperty("loggedIn", false);
        }
        return Response.ok().entity(AppUtil.GSON.toJson(json)).build();
    }
}
