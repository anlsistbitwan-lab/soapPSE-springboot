package com.bitwan.recaudosoap.restclient;

import com.bitwan.recaudosoap.config.ApiProperties;
import com.bitwan.recaudosoap.dto.ConfirmationPseRequestDto;
import com.bitwan.recaudosoap.dto.ConfirmationPseResponseDto;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

@Service
public class ConfirmationPseRestClient {

    private final RestTemplate restTemplate;
    private final ApiProperties apiProperties;

    public ConfirmationPseRestClient(RestTemplate restTemplate, ApiProperties apiProperties) {
        this.restTemplate = restTemplate;
        this.apiProperties = apiProperties;
    }

    public ConfirmationPseResponseDto confirm(ConfirmationPseRequestDto requestDto) {
        // URL configurada en application.yml bajo api.clients.confirmation-pse
        String url = apiProperties.getClients().get("confirmation-pse").getUrl();

        // Headers (Authorization ya lo maneja el interceptor)
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<ConfirmationPseRequestDto> entity = new HttpEntity<>(requestDto, headers);

        // Llamada REST
        return restTemplate.postForObject(url, entity, ConfirmationPseResponseDto.class);
    }
}
