package com.bitwan.recaudosoap.controller;

import com.bitwan.recaudosoap.dto.ConfirmationPseRequestDto;
import com.bitwan.recaudosoap.dto.ConfirmationPseResponseDto;
import com.bitwan.recaudosoap.dto.PaymentRequestDto;
import com.bitwan.recaudosoap.dto.PaymentResponseDto;
import com.bitwan.recaudosoap.dto.VerificationRequestDto;
import com.bitwan.recaudosoap.dto.VerificationResponseDto;
import com.bitwan.recaudosoap.mapper.ClientePSEMapper;
import com.bitwan.recaudosoap.restclient.ConfirmationPseRestClient;
import com.bitwan.recaudosoap.restclient.PaymentRestClient;
import com.bitwan.recaudosoap.restclient.VerificationRestClient;
import com.bitwan.soap.clientePSE.ConfirmTransactionPaymentInvoice;
import com.bitwan.soap.clientePSE.ConfirmTransactionPaymentInvoiceResponse;
import com.bitwan.soap.clientePSE.GetTransactionInformationInvoice;
import com.bitwan.soap.clientePSE.GetTransactionInformationInvoiceResponse;

import javax.xml.namespace.QName;

import org.springframework.web.client.HttpClientErrorException;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;
import org.springframework.ws.soap.SoapFaultException;

@Endpoint
public class ClientePSEEndpoint {

    // Debe coincidir con el targetNamespace de tu XSD
    //private static final String NAMESPACE_URI = "https://pruebados.bitwan.info/InsitelCollectionServicePse";
    private static final String NAMESPACE_URI = "http://www.achcolombia.com.co/PSEHostingInvoicesWS";


    private final VerificationRestClient verificationRestClient;
    private final PaymentRestClient paymentRestClient;
    private final ConfirmationPseRestClient confirmationPseRestClient;

    public ClientePSEEndpoint(VerificationRestClient verificationRestClient,
                              PaymentRestClient paymentRestClient,
                              ConfirmationPseRestClient confirmationPseRestClient) {
        this.verificationRestClient = verificationRestClient;
        this.paymentRestClient = paymentRestClient;
        this.confirmationPseRestClient = confirmationPseRestClient;
    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "GetTransactionInformationInvoice")
    @ResponsePayload
    public GetTransactionInformationInvoiceResponse getTransactionInformationInvoice(
            @RequestPayload GetTransactionInformationInvoice request) {
        
        //try {                
            // SOAP -> DTO REST
            VerificationRequestDto verifyDto = ClientePSEMapper.toVerificationRequestDto(request);

            // Llamada REST
            VerificationResponseDto restResp = verificationRestClient.verify(verifyDto);

            // REST -> SOAP (mapeo)
            return ClientePSEMapper.toGetTransactionInformationInvoiceResponse(restResp, request);
           
    }
    
    
    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "ConfirmTransactionPaymentInvoice")
    @ResponsePayload
    public ConfirmTransactionPaymentInvoiceResponse confirmTransactionPaymentInvoice(
            @RequestPayload ConfirmTransactionPaymentInvoice request) {

        // SOAP -> DTO REST
        //PaymentRequestDto paymentDto = ClientePSEMapper.toPaymentRequestDto(request);
        ConfirmationPseRequestDto pseRequest = ClientePSEMapper.toConfirmationPseRequestDto(request);

        // Llamada REST
        //PaymentResponseDto restResp = paymentRestClient.notifyPayment(paymentDto);
        ConfirmationPseResponseDto pseResponse = confirmationPseRestClient.confirm(pseRequest);

        // REST -> SOAP
        //return ClientePSEMapper.toConfirmPaymentResponse(restResp);
        return ClientePSEMapper.toConfirmPsePaymentResponse(pseResponse);
    }
}
