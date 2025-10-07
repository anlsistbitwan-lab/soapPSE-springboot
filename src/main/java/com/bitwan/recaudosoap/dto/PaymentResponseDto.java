package com.bitwan.recaudosoap.dto;

import java.util.List;

public class PaymentResponseDto {
    private String referencia;
    private List<String> serviciosProcesados;

    // getters & setters
    public String getReferencia() { return referencia; }
    public void setReferencia(String referencia) { this.referencia = referencia; }
    public List<String> getServiciosProcesados() { return serviciosProcesados; }
    public void setServiciosProcesados(List<String> serviciosProcesados) { this.serviciosProcesados = serviciosProcesados; }
}

