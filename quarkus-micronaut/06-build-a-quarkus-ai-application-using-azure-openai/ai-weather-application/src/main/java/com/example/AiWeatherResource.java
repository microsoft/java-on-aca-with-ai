package com.example;

import jakarta.inject.Inject;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.io.IOException;
import java.time.LocalDate;

@Path("/weather")
public class AiWeatherResource {

    private final AiWeatherService aiWeatherService;
    private final String weatherHistory;

    @Inject
    public AiWeatherResource(AiWeatherService aiWeatherService) throws IOException {
        this.aiWeatherService = aiWeatherService;
        this.weatherHistory = new String(getClass().getClassLoader().getResourceAsStream("Paris.csv").readAllBytes());
    }

    @Path("/ask")
    @POST
    @Produces(MediaType.TEXT_PLAIN)
    public String askAi(String question) {

        String result = aiWeatherService.ask(
            LocalDate.now().toString(),
            "Paris",
            weatherHistory,
            question
        );

        return result;
    }
}
