package com.bitwan.recaudosoap.dto;

import java.util.List;

public class VerificationResponseDto {
    private String referencia;
    private List<FacturaDto> facturas;
    private String total;

    public static class FacturaDto {
        private String numeroFactura;
        private Long idAsientoContable;
        private String valor;
        private String fechaEmision;
        private String fechaVencimiento;

        // getters & setters
        public String getNumeroFactura() { return numeroFactura; }
        public void setNumeroFactura(String numeroFactura) { this.numeroFactura = numeroFactura; }
        public Long getIdAsientoContable() { return idAsientoContable; }
        public void setIdAsientoContable(Long idAsientoContable) { this.idAsientoContable = idAsientoContable; }
        public String getValor() { return valor; }
        public void setValor(String valor) { this.valor = valor; }
        public String getFechaEmision() { return fechaEmision; }
        public void setFechaEmision(String fechaEmision) { this.fechaEmision = fechaEmision; }
        public String getFechaVencimiento() { return fechaVencimiento; }
        public void setFechaVencimiento(String fechaVencimiento) { this.fechaVencimiento = fechaVencimiento; }
    }

    // getters & setters
    public String getReferencia() { return referencia; }
    public void setReferencia(String referencia) { this.referencia = referencia; }
    public List<FacturaDto> getFacturas() { return facturas; }
    public void setFacturas(List<FacturaDto> facturas) { this.facturas = facturas; }
    public String getTotal() { return total; }
    public void setTotal(String total) { this.total = total; }
}
