package com.example.demo;

import org.springframework.ai.azure.openai.AzureOpenAiChatClient;
import org.springframework.ai.chat.ChatResponse;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(path = "/weather")
public class AiWeatherController {

    private final AzureOpenAiChatClient chatClient;

    @Value("classpath:/static/prompt.template")
    private Resource promptTemplate;

    @Value("classpath:/static/Paris.csv")
    private Resource parisWeatherData;

    public AiWeatherController(AzureOpenAiChatClient chatClient) {
        this.chatClient = chatClient;
    }

    @PostMapping("/ask")
    public ChatResponse askAi(@RequestParam String question) {
        PromptTemplate systemMessageTemplate = new SystemPromptTemplate(promptTemplate);
        Message systemMessage = systemMessageTemplate.createMessage(
                Map.of("today", LocalDate.now().toString(),
                        "city", "Paris",
                        "weatherHistory", parisWeatherData));
        Prompt prompt = new Prompt(List.of(systemMessage, new UserMessage(question)));
        return chatClient.call(prompt);
    }

}