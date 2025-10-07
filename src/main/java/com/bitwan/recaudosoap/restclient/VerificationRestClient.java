package com.bitwan.recaudosoap.restclient;

import com.bitwan.recaudosoap.config.ApiProperties;
import com.bitwan.recaudosoap.dto.VerificationRequestDto;
import com.bitwan.recaudosoap.dto.VerificationResponseDto;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

@Service
public class VerificationRestClient {

    private final RestTemplate restTemplate;
    private final ApiProperties apiProperties;

    public VerificationRestClient(RestTemplate restTemplate, ApiProperties apiProperties) {
        this.restTemplate = restTemplate;
        this.apiProperties = apiProperties;
    }

    public VerificationResponseDto verify(VerificationRequestDto requestDto) {
        // URL desde application.yml
        String url = apiProperties.getClients().get("verification").getUrl();

        // Headers (el interceptor ya inyecta Authorization)
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<VerificationRequestDto> entity = new HttpEntity<>(requestDto, headers);

        // Llamada a la API REST
        return restTemplate.postForObject(url, entity, VerificationResponseDto.class);
    }
}
