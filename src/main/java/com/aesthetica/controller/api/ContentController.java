package com.aesthetica.controller.api;

import com.aesthetica.service.CityService;
import com.aesthetica.service.ContentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataParam;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@Path("/data")
public class ContentController {

    @Path("/fresh-deals")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response loadFreshDeals() {
        String responseJson = new ContentService().loadFreshDeals();
        return Response.ok().entity(responseJson).build();
    }

    @Path("/cities")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response loadCities() {
        String loadAllCities = new CityService().loadAllCities();
        return Response.ok().entity(loadAllCities).build();
    }

    @Path("/categories")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response loadCategories() {
        String categories = new ContentService().loadCategories();
        return Response.ok().entity(categories).build();
    }

    @Path("/categories")
    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addCategory(
            @FormDataParam("name") String name,
            @FormDataParam("image") List<FormDataBodyPart> imageBodyParts,
            @Context HttpServletRequest request) {

        String imagePath = "";

        if (imageBodyParts != null && !imageBodyParts.isEmpty()) {
            FormDataBodyPart imageBodyPart = imageBodyParts.get(0);

            if (imageBodyPart != null && imageBodyPart.getContentDisposition() != null
                    && imageBodyPart.getContentDisposition().getFileName() != null
                    && !imageBodyPart.getContentDisposition().getFileName().isBlank()) {

                String appPath = request.getServletContext().getRealPath("");
                String uploadDir = appPath + File.separator + "assets" + File.separator + "images" + File.separator + "category";

                File directory = new File(uploadDir);
                if (!directory.exists()) {
                    directory.mkdirs();
                }

                InputStream inputStream = imageBodyPart.getValueAs(InputStream.class);
                String originalName = imageBodyPart.getContentDisposition().getFileName();
                String uniqueName = UUID.randomUUID().toString() + "_" + originalName;
                String savePath = uploadDir + File.separator + uniqueName;

                try {
                    Files.copy(inputStream, Paths.get(savePath), StandardCopyOption.REPLACE_EXISTING);
                    imagePath = "assets/images/category/" + uniqueName;
                } catch (IOException e) {
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity("{\"success\":false,\"message\":\"Failed to save category image\"}")
                            .build();
                }
            }
        }

        String responseJson = new ContentService().addCategory(name, imagePath);
        return Response.ok().entity(responseJson).build();
    }

    @Path("/tea-gallery")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response loadTeaGallery() {
        System.out.println("Loading tea gallery");
        String responseJson = new ContentService().loadTeaGallery();
        return Response.ok().entity(responseJson).build();
    }
}
