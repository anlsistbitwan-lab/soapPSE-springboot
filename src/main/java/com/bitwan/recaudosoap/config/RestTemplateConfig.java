package com.bitwan.recaudosoap.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

@Configuration
public class RestTemplateConfig {

    private final ApiProperties apiProperties;

    public RestTemplateConfig(ApiProperties apiProperties) {
        this.apiProperties = apiProperties;
    }

    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();

        ClientHttpRequestInterceptor authInterceptor = (request, body, execution) -> {
            String requestUrl = request.getURI().toString();

            apiProperties.getClients().forEach((name, config) -> {
                if (requestUrl.startsWith(config.getUrl())) {
                    ApiProperties.AuthConfig auth = config.getAuth();
                    if (auth != null) {
                        if ("basic".equalsIgnoreCase(auth.getType())) {
                            String creds = auth.getUsername() + ":" + auth.getPassword();
                            String base64Creds = Base64.getEncoder().encodeToString(creds.getBytes(StandardCharsets.UTF_8));
                            request.getHeaders().add("Authorization", "Basic " + base64Creds);
                        } else if ("bearer".equalsIgnoreCase(auth.getType())) {
                            request.getHeaders().add("Authorization", "Bearer " + auth.getPassword());
                        }
                        // none → no agrega nada
                    }

                    // Log opcional para verificar
                    System.out.println("[Auth applied] " + name + " → " + request.getHeaders().getFirst("Authorization"));
                }
            });

            return execution.execute(request, body);
        };

        restTemplate.setInterceptors(List.of(authInterceptor));
        return restTemplate;
    }
}
