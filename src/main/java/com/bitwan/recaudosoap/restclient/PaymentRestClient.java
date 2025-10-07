package com.bitwan.recaudosoap.restclient;

import com.bitwan.recaudosoap.config.ApiProperties;
import com.bitwan.recaudosoap.dto.PaymentRequestDto;
import com.bitwan.recaudosoap.dto.PaymentResponseDto;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

@Service
public class PaymentRestClient {

    private final RestTemplate restTemplate;
    private final ApiProperties apiProperties;

    public PaymentRestClient(RestTemplate restTemplate, ApiProperties apiProperties) {
        this.restTemplate = restTemplate;
        this.apiProperties = apiProperties;
    }

    public PaymentResponseDto notifyPayment(PaymentRequestDto requestDto) {
        // URL desde application.yml
        String url = apiProperties.getClients().get("payment").getUrl();

        // Headers (Authorization se inyecta vía interceptor)
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<PaymentRequestDto> entity = new HttpEntity<>(requestDto, headers);

        // Llamada a la API REST
        return restTemplate.postForObject(url, entity, PaymentResponseDto.class);
    }
}
