package com.aesthetica.controller.api;

import com.aesthetica.Annotation.IsUser;
import com.aesthetica.dto.CheckoutRequestDTO;
import com.aesthetica.service.CheckoutService;
import com.aesthetica.util.AppUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/checkout")
public class CheckoutController {
    @POST
    @Path("/checkout-process")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response processCheckout(String jsonData, @Context HttpServletRequest request) {
        CheckoutRequestDTO checkoutDTO = AppUtil.GSON.fromJson(jsonData, CheckoutRequestDTO.class);
        System.out.println("\u001B[34m" + checkoutDTO.toString() + "\u001B[0m");
        String responseJson = new CheckoutService().processCheckout(checkoutDTO, request);
        return Response.ok().entity(responseJson).build();
    }
}
