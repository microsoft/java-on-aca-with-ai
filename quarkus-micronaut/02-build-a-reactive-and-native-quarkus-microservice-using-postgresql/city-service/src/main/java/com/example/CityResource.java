package com.example;

import io.quarkus.logging.Log;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import java.util.List;

@Path("/")
public class CityResource {

    @GET
    @Path("cities")
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<List<City>> getCities() {
        Log.info("Getting all cities");
        return City.listAll();
    }
}
