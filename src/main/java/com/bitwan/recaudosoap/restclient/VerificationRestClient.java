package com.bitwan.recaudosoap.restclient;

import com.bitwan.recaudosoap.config.ApiProperties;
import com.bitwan.recaudosoap.dto.VerificationRequestDto;
import com.bitwan.recaudosoap.dto.VerificationResponseDto;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.Map;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

@Service
public class VerificationRestClient {

    private final RestTemplate restTemplate;
    private final ApiProperties apiProperties;
    private final ObjectMapper objectMapper;

    public VerificationRestClient(RestTemplate restTemplate, ApiProperties apiProperties, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.apiProperties = apiProperties;
        this.objectMapper = objectMapper;
    }

    public VerificationResponseDto verify(VerificationRequestDto requestDto) {
        // URL desde application.yml
        String url = apiProperties.getClients().get("verification").getUrl();

        // Headers (el interceptor ya inyecta Authorization)
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<VerificationRequestDto> entity = new HttpEntity<>(requestDto, headers);

        // Llamada a la API REST
        //return restTemplate.postForObject(url, entity, VerificationResponseDto.class);

        try {
            // ✅ Caso exitoso (HTTP 200)
            return restTemplate.postForObject(url, entity, VerificationResponseDto.class);

        }catch (HttpClientErrorException ex) {
            // ⚠️ Manejo explícito de errores 4xx o 5xx
            VerificationResponseDto errorResponse = new VerificationResponseDto();

            try {
                // Intentamos convertir el cuerpo de error JSON a un mapa dinámico
                String body = ex.getResponseBodyAsString();
                Map<String, Object> json = objectMapper.readValue(body, new TypeReference<Map<String, Object>>() {});
                //Map<String, Object> json = objectMapper.readValue(body, Map.class);

                // Si existen las claves, las tomamos tal cual
                errorResponse.setError(getStringValue(json, "error"));
                errorResponse.setDetalles(getStringValue(json, "detalles"));
                errorResponse.setCausa(getStringValue(json, "causa"));

            } catch (Exception parseEx) {
                // Si el cuerpo no es JSON válido
                errorResponse.setError("HTTP_" + ex.getStatusCode().value());
                errorResponse.setDetalles("Error en la respuesta del servicio: " + ex.getStatusText());
                errorResponse.setCausa("No se pudo interpretar el cuerpo del error.");
            }

            return errorResponse;

        } catch (Exception e) {
            // ⚠️ Otros errores (timeout, conexión, etc.)
            VerificationResponseDto errorResponse = new VerificationResponseDto();
            errorResponse.setError("EXCEPTION");
            errorResponse.setDetalles("Error inesperado: " + e.getMessage());
            return errorResponse;
        }
    }

    private String getStringValue(Map<String, Object> map, String key) {
        Object val = map.get(key);
        return val != null ? val.toString() : null;
    }
}
