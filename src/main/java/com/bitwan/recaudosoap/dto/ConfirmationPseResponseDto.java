package com.bitwan.recaudosoap.dto;

import java.util.List;

public class ConfirmationPseResponseDto {

    private String referencia;
    private List<String> serviciosProcesados;

    public String getReferencia() {
        return referencia;
    }

    public void setReferencia(String referencia) {
        this.referencia = referencia;
    }

    public List<String> getServiciosProcesados() {
        return serviciosProcesados;
    }

    public void setServiciosProcesados(List<String> serviciosProcesados) {
        this.serviciosProcesados = serviciosProcesados;
    }
}
