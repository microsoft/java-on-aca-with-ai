package com.example;

import jakarta.inject.Inject;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.slf4j.Logger;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@Path("/weather")
public class AiWeatherResource {

    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(AiWeatherResource.class);

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

        LOGGER.info("\uD83D\uDCDD Preparing response...");

        String result = aiWeatherService.ask(
            LocalDate.now().toString(),
            "Paris",
            weatherHistory,
            question
        );

        LOGGER.info("✅ Response for \"{}\" ready", question);

        return result;
    }
}
