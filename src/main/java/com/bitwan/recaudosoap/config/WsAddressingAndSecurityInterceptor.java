package com.bitwan.recaudosoap.config;

import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.EndpointInterceptor;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.saaj.SaajSoapMessage;

import jakarta.xml.soap.*;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class WsAddressingAndSecurityInterceptor implements EndpointInterceptor {

    @Override
    public boolean handleRequest(MessageContext messageContext, Object endpoint) {
        return true;
    }

    @Override
    public boolean handleResponse(MessageContext messageContext, Object endpoint) throws Exception {
        SoapMessage soapMessage = (SoapMessage) messageContext.getResponse();
        SaajSoapMessage saajMessage = (SaajSoapMessage) soapMessage;

        SOAPMessage message = saajMessage.getSaajMessage();
        SOAPEnvelope envelope = message.getSOAPPart().getEnvelope();
        SOAPBody body = envelope.getBody();

        System.out.println("Interceptor ejecutado");


        // Solo aplicar a GetTransactionInformationInvoiceResponse
        String localName = body.getFirstChild() != null ? body.getFirstChild().getLocalName() : "";
        if (!"GetTransactionInformationInvoiceResponse".equals(localName)) {
            return true;
        }

        SOAPHeader header = envelope.getHeader();
        if (header == null) {
            header = envelope.addHeader();
        }

        // Namespaces
        String WSA_NS = "http://schemas.xmlsoap.org/ws/2004/08/addressing";
        String WSSE_NS = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd";
        String WSU_NS = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd";

        // --- WS-Addressing headers ---
        SOAPHeaderElement action = header.addHeaderElement(envelope.createName("Action", "wsa", WSA_NS));
        action.addTextNode("http://www.achcolombia.com.co/PSEHostingInvoicesWS/GetTransactionInformationInvoiceResponse");

        SOAPHeaderElement messageId = header.addHeaderElement(envelope.createName("MessageID", "wsa", WSA_NS));
        messageId.addTextNode("urn:uuid:" + UUID.randomUUID());

        SOAPHeaderElement relatesTo = header.addHeaderElement(envelope.createName("RelatesTo", "wsa", WSA_NS));
        relatesTo.addTextNode("urn:uuid:f71483bd-911a-4a49-8b10-b6222c88bdeb");

        SOAPHeaderElement to = header.addHeaderElement(envelope.createName("To", "wsa", WSA_NS));
        to.addTextNode("http://schemas.xmlsoap.org/ws/2004/08/addressing/role/anonymous");

        // --- WS-Security headers ---
        SOAPHeaderElement security = header.addHeaderElement(envelope.createName("Security", "wsse", WSSE_NS));
        SOAPElement timestamp = security.addChildElement(envelope.createName("Timestamp", "wsu", WSU_NS));

        String created = OffsetDateTime.now().format(DateTimeFormatter.ISO_INSTANT);
        String expires = OffsetDateTime.now().plusMinutes(5).format(DateTimeFormatter.ISO_INSTANT);

        SOAPElement createdEl = timestamp.addChildElement(envelope.createName("Created", "wsu", WSU_NS));
        createdEl.addTextNode(created);

        SOAPElement expiresEl = timestamp.addChildElement(envelope.createName("Expires", "wsu", WSU_NS));
        expiresEl.addTextNode(expires);

        // 👇 Muy importante: guardar cambios
        message.saveChanges();

        return true;
    }

    @Override
    public boolean handleFault(MessageContext messageContext, Object endpoint) {
        return true;
    }

    @Override
    public void afterCompletion(MessageContext messageContext, Object endpoint, Exception ex) {
    }
}
