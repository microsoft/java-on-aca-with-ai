package com.example;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import io.quarkiverse.langchain4j.RegisterAiService;

@RegisterAiService
public interface AiWeatherService {

    @SystemMessage("""
        Today is {{today}}.
        You will act as a meteorological expert who helps analyze and forecast weather.
        Given the following historical weather (in CSV format with header) of {{city}} over an entire year, please answer the questions and make predictions.
        Don't use any external data.

        {{weatherHistory}}
        """
    )
    @UserMessage("{{question}}")
    String ask(@V("today") String today, @V("city") String city, @V("weatherHistory") String weatherHistory, @V("question") String question);
}
