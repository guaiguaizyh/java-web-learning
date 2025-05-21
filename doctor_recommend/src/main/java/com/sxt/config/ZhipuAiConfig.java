package com.sxt.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 智谱AI配置类
 */
@Configuration
@ConfigurationProperties(prefix = "zhipu.ai")
@Data
public class ZhipuAiConfig {
    /**
     * 智谱AI的API Key
     */
    private String apiKey = "4053c5f293c04bc0bc2215abd1bbef87.9UJnAhmjx0Hr6TSj";
    
    /**
     * 使用的模型，如glm-3-turbo, glm-4
     */
    private String model = "glm-4";
    
    /**
     * 温度参数，控制生成文本的随机性，值越大越随机
     */
    private Double temperature;
    
    /**
     * 最大生成的token数
     */
    private Integer maxTokens = 2048;
    
    /**
     * 获取API配置
     */
    public String getApiConfig() {
        return apiKey;
    }
} 