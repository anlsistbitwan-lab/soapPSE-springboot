package com.bitwan.recaudosoap.dto;

import java.util.List;

public class PaymentRequestDto {
    private String referencia;
    private List<String> facturas;
    private String valor;
    private String transactionCode;

    // getters & setters
    public String getReferencia() { return referencia; }
    public void setReferencia(String referencia) { this.referencia = referencia; }
    public List<String> getFacturas() { return facturas; }
    public void setFacturas(List<String> facturas) { this.facturas = facturas; }
    public String getValor() { return valor; }
    public void setValor(String valor) { this.valor = valor; }
    public String getTransactionCode() { return transactionCode; }
    public void setTransactionCode(String transactionCode) { this.transactionCode = transactionCode; }
}


