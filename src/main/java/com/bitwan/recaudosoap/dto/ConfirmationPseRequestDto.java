package com.bitwan.recaudosoap.dto;

import java.util.List;

public class ConfirmationPseRequestDto {

    private List<String> idsAsientoContable;
    private String valor;
    private String transactionCode;

    public List<String> getIdsAsientoContable() {
        return idsAsientoContable;
    }

    public void setIdsAsientoContable(List<String> idsAsientoContable) {
        this.idsAsientoContable = idsAsientoContable;
    }

    public String getValor() {
        return valor;
    }

    public void setValor(String valor) {
        this.valor = valor;
    }

    public String getTransactionCode() {
        return transactionCode;
    }

    public void setTransactionCode(String transactionCode) {
        this.transactionCode = transactionCode;
    }
}
