package com.example.demo.controller;

import com.example.demo.entity.Dish;
import com.example.demo.repository.DishRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "*", allowCredentials = "false")
public class DeepSeekController {

    @Autowired
    private DishRepository dishRepository;

    private static final String API_KEY = "sk-fc8dde68ffbc4fedbb04f9ac5bac4775"; // 替换为你的真实 key
    private static final String API_URL = "https://api.deepseek.com/v1/chat/completions";

    @PostMapping
    public Map<String, Object> chat(@RequestBody Map<String, String> request) {
        String userInput = request.get("input");

        // Step 1: 加载全部菜品
        List<Dish> allDishes = dishRepository.findAll();

        // Step 2: 构造 Prompt 提示
        StringBuilder promptBuilder = new StringBuilder();
        promptBuilder.append("你是一个餐厅智能助手，叫小智，负责回答用户问题，也能推荐菜品。\n")
                .append("如果用户需要推荐，请从下列菜品中选择；否则请正常回答问题。\n")
                .append("以下是菜品列表：\n");

        for (Dish dish : allDishes) {
            promptBuilder.append("- ").append(dish.getDishName()).append("\n");
        }

        promptBuilder.append("\n用户提问：").append(userInput).append("\n")
                .append("请基于以上内容，给出简洁明了的回复：");

        // Step 3: 向大模型发出请求
        String reply = askDeepSeek(promptBuilder.toString());

        // Step 4: 返回结果给前端
        if (reply == null || reply.isBlank()) {
            return Map.of("reply", "很抱歉，我暂时无法回答，请稍后再试。");
        }

        return Map.of("reply", reply);
    }

    /**
     * 调用 DeepSeek 大模型接口，获取响应文本
     */
    private String askDeepSeek(String prompt) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(API_KEY); // 设置 Authorization
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> requestBody = Map.of(
                "model", "deepseek-chat",
                "messages", List.of(
                        Map.of("role", "user", "content", prompt)
                )
        );

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(API_URL, entity, Map.class);
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
                if (!choices.isEmpty()) {
                    Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                    return message.get("content").toString().trim();
                }
            }
        } catch (Exception e) {
            System.err.println("调用 DeepSeek 出错: " + e.getMessage());
        }

        return null;
    }
}
