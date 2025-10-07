package com.bitwan.recaudosoap.mapper;

import com.bitwan.recaudosoap.dto.ConfirmationPseRequestDto;
import com.bitwan.recaudosoap.dto.ConfirmationPseResponseDto;
import com.bitwan.recaudosoap.dto.PaymentRequestDto;
import com.bitwan.recaudosoap.dto.PaymentResponseDto;
import com.bitwan.recaudosoap.dto.VerificationRequestDto;
import com.bitwan.recaudosoap.dto.VerificationResponseDto;
import com.bitwan.recaudosoap.dto.VerificationResponseDto.FacturaDto;
import com.bitwan.soap.clientePSE.ConfirmTransactionPaymentInvoice;
import com.bitwan.soap.clientePSE.ConfirmTransactionPaymentInvoiceResponse;
import com.bitwan.soap.clientePSE.GetTransactionInformationInvoice;
import com.bitwan.soap.clientePSE.GetTransactionInformationInvoiceResponse;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.stream.Collectors;

public class ClientePSEMapper {

    // SOAP -> REST (verificacion)
    public static VerificationRequestDto toVerificationRequestDto(GetTransactionInformationInvoice soapRequest) {
        VerificationRequestDto dto = new VerificationRequestDto();
        // según tu XSD, QueryID es el campo que contiene la referencia
        String queryId = soapRequest.getQueryID();
        if (queryId != null) {
            queryId = queryId.trim();
            if (queryId.isEmpty()) {
                throw new IllegalArgumentException("El atributo QueryID no puede ser vacío");
            }
            dto.setReferencia(queryId);
        }
        //dto.setReferencia(soapRequest.getQueryID());
        return dto;
    }

    // REST -> SOAP (verificacion response)
    public static GetTransactionInformationInvoiceResponse toGetTransactionInformationInvoiceResponse(
            VerificationResponseDto restResponse,
            GetTransactionInformationInvoice soapRequest) {

        GetTransactionInformationInvoiceResponse soapResponse = new GetTransactionInformationInvoiceResponse();
        GetTransactionInformationInvoiceResponse.GetTransactionInformationInvoiceResult result =
                new GetTransactionInformationInvoiceResponse.GetTransactionInformationInvoiceResult();

        // ReturnCode: OK si hubo respuesta, ERROR en otro caso
        if (restResponse == null) {
            result.setReturnCode("ERROR");
            result.setErrorMessage("No data from verification service");
            soapResponse.setGetTransactionInformationInvoiceResult(result);
            return soapResponse;
        }

        result.setReturnCode("OK");
        result.setErrorMessage(null);

        // Crear el contenedor Invoices
        GetTransactionInformationInvoiceResponse.GetTransactionInformationInvoiceResult.Invoices invoicesContainer =
                new GetTransactionInformationInvoiceResponse.GetTransactionInformationInvoiceResult.Invoices();

        List<FacturaDto> facturas = restResponse.getFacturas();
        if (facturas != null) {
            for (FacturaDto f : facturas) {
                GetTransactionInformationInvoiceResponse.GetTransactionInformationInvoiceResult.Invoices.InvoiceField invoiceField =
                        new GetTransactionInformationInvoiceResponse.GetTransactionInformationInvoiceResult.Invoices.InvoiceField();

                // Mapeo campos según lo acordado
                // InvoiceID <- idAsientoContable (convertir a String si es Long)
                invoiceField.setInvoiceID(f.getIdAsientoContable() != null ? String.valueOf(f.getIdAsientoContable()) : "");

                invoiceField.setPaymentDescription("Pago de Factura local");

                // Amount, VatAmount como BigDecimal
                BigDecimal amount = BigDecimal.ZERO;
                if (f.getValor() != null && !f.getValor().isBlank()) {
                    try {
                        amount = new BigDecimal(f.getValor());
                    } catch (Exception ex) {
                        amount = BigDecimal.ZERO;
                    }
                }
                invoiceField.setAmount(amount);
                invoiceField.setVatAmount(amount);

                // ExpirationDate -> XMLGregorianCalendar si viene fecha en formato ISO (yyyy-MM-dd)
                XMLGregorianCalendar xmlDate = toXMLGregorianCalendar(f.getFechaVencimiento());
                invoiceField.setExpirationDate(xmlDate);

                invoiceField.setServiceCode("1001");
                // si restResponse incluye email podrías usarlo; si no, default
                invoiceField.setEMail(restResponse.getReferencia() != null ? "" : "cliente@example.com");

                // ReferenceNumber1 = 'CC'
                invoiceField.setReferenceNumber1("CC");
                // ReferenceNumber2 = referencia (QueryID)
                invoiceField.setReferenceNumber2(soapRequest.getQueryID());
                // ReferenceNumber3 = CustomerIP del request SOAP
                invoiceField.setReferenceNumber3(soapRequest.getCustomerIP() != null ? soapRequest.getCustomerIP() : "");

                invoiceField.setFields(null);

                invoicesContainer.getInvoiceField().add(invoiceField);
            }
        }

        result.setInvoices(invoicesContainer);
        soapResponse.setGetTransactionInformationInvoiceResult(result);
        return soapResponse;
    }

    // SOAP -> REST (confirmar pago)
    public static PaymentRequestDto toPaymentRequestDto(ConfirmTransactionPaymentInvoice soapRequest) {
        PaymentRequestDto dto = new PaymentRequestDto();

        // Obtener lista de InvoiceConfirmField desde el SOAP request
        List<ConfirmTransactionPaymentInvoice.Invoices.InvoiceConfirmField> invoiceConfirmFields =
                soapRequest.getInvoices() != null ? soapRequest.getInvoices().getInvoiceConfirmField() : null;

        // referencia: tomar ReferenceNumber2 del primer registro no nulo (convención)
        String referencia = null;
        if (invoiceConfirmFields != null && !invoiceConfirmFields.isEmpty()) {
            for (ConfirmTransactionPaymentInvoice.Invoices.InvoiceConfirmField f : invoiceConfirmFields) {
                if (f.getReferenceNumber2() != null && !f.getReferenceNumber2().isBlank()) {
                    referencia = f.getReferenceNumber2();
                    break;
                }
            }
        }
        dto.setReferencia(referencia != null ? referencia : "");

        // facturas: lista de InvoiceID
        List<String> facturas = invoiceConfirmFields == null ? List.of() :
                invoiceConfirmFields.stream()
                        .map(ConfirmTransactionPaymentInvoice.Invoices.InvoiceConfirmField::getInvoiceID)
                        .collect(Collectors.toList());
        dto.setFacturas(facturas);

        // valor: sumatoria de Amounts
        BigDecimal total = BigDecimal.ZERO;
        if (invoiceConfirmFields != null) {
            for (ConfirmTransactionPaymentInvoice.Invoices.InvoiceConfirmField f : invoiceConfirmFields) {
                BigDecimal a = f.getAmount() != null ? f.getAmount() : BigDecimal.ZERO;
                total = total.add(a);
            }
        }
        dto.setValor(total.toPlainString());

        // transactionCode fijo por ahora
        dto.setTransactionCode("1001");

        return dto;
    }

    // REST -> SOAP (confirm payment response)
    public static ConfirmTransactionPaymentInvoiceResponse toConfirmPaymentResponse(PaymentResponseDto restResponse) {
        ConfirmTransactionPaymentInvoiceResponse soapResponse = new ConfirmTransactionPaymentInvoiceResponse();
        ConfirmTransactionPaymentInvoiceResponse.ConfirmTransactionPaymentInvoiceResult result =
                new ConfirmTransactionPaymentInvoiceResponse.ConfirmTransactionPaymentInvoiceResult();

        if (restResponse == null) {
            result.setReturnCode("ERROR");
            result.setOptional("No response from payment API");
        } else {
            result.setReturnCode("OK");
            result.setOptional("Pago registrado exitosamente");
        }

        soapResponse.setConfirmTransactionPaymentInvoiceResult(result);
        return soapResponse;
    }

    // === NUEVOS MÉTODOS PARA PSE REST ===

    // SOAP -> REST (confirmar pago PSE REST)
    public static ConfirmationPseRequestDto toConfirmationPseRequestDto(ConfirmTransactionPaymentInvoice soapRequest) {
        ConfirmationPseRequestDto dto = new ConfirmationPseRequestDto();

        List<ConfirmTransactionPaymentInvoice.Invoices.InvoiceConfirmField> invoiceConfirmFields =
                soapRequest.getInvoices() != null ? soapRequest.getInvoices().getInvoiceConfirmField() : null;

        List<String> idsAsiento = invoiceConfirmFields == null ? List.of() :
                invoiceConfirmFields.stream()
                        .map(ConfirmTransactionPaymentInvoice.Invoices.InvoiceConfirmField::getInvoiceID)
                        .collect(Collectors.toList());
        dto.setIdsAsientoContable(idsAsiento);

        BigDecimal total = BigDecimal.ZERO;
        if (invoiceConfirmFields != null) {
            for (ConfirmTransactionPaymentInvoice.Invoices.InvoiceConfirmField f : invoiceConfirmFields) {
                BigDecimal a = f.getAmount() != null ? f.getAmount() : BigDecimal.ZERO;
                total = total.add(a);
            }
        }
        dto.setValor(total.toPlainString());

        dto.setTransactionCode("1001");

        return dto;
    }

    // REST -> SOAP (confirm payment response PSE REST)
    public static ConfirmTransactionPaymentInvoiceResponse toConfirmPsePaymentResponse(ConfirmationPseResponseDto restResponse) {
        ConfirmTransactionPaymentInvoiceResponse soapResponse = new ConfirmTransactionPaymentInvoiceResponse();
        ConfirmTransactionPaymentInvoiceResponse.ConfirmTransactionPaymentInvoiceResult result =
                new ConfirmTransactionPaymentInvoiceResponse.ConfirmTransactionPaymentInvoiceResult();

        if (restResponse == null) {
            result.setReturnCode("ERROR");
            result.setOptional("No response from PSE REST API");
        } else {
            result.setReturnCode("OK");
            result.setOptional("Pago registrado exitosamente (PSE REST)");
        }

        soapResponse.setConfirmTransactionPaymentInvoiceResult(result);
        return soapResponse;
    }


    // Helper: convierte "yyyy-MM-dd" (u otros ISO parcial) a XMLGregorianCalendar
    private static XMLGregorianCalendar toXMLGregorianCalendar(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) return null;
        try {
            // intenta parsear fecha tipo "yyyy-MM-dd" o "yyyy-MM-dd'T'HH:mm:ss"
            LocalDate ld = LocalDate.parse(dateStr);
            GregorianCalendar cal = GregorianCalendar.from(ld.atStartOfDay(ZoneId.systemDefault()));
            return DatatypeFactory.newInstance().newXMLGregorianCalendar(cal);
        } catch (Exception ex) {
            // intento alternativo: crear usando DatatypeFactory.newXMLGregorianCalendar(dateStr) puede fallar si no viene completo
            try {
                return DatatypeFactory.newInstance().newXMLGregorianCalendar(dateStr);
            } catch (Exception e) {
                return null;
            }
        }
    }
}
