package com.bitwan.recaudosoap;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.ws.config.annotation.EnableWs;
import org.springframework.ws.transport.http.MessageDispatcherServlet;
import org.springframework.ws.wsdl.wsdl11.DefaultWsdl11Definition;
import org.springframework.ws.wsdl.wsdl11.SimpleWsdl11Definition;
import org.springframework.ws.wsdl.wsdl11.Wsdl11Definition;
import org.springframework.xml.xsd.SimpleXsdSchema;
import org.springframework.xml.xsd.XsdSchema;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Configuration
@EnableWs
public class WebServiceConfig {

    @Bean
    public ServletRegistrationBean<MessageDispatcherServlet> messageDispatcherServlet(ApplicationContext applicationContext) {
        MessageDispatcherServlet servlet = new MessageDispatcherServlet();
        servlet.setApplicationContext(applicationContext);
        servlet.setTransformWsdlLocations(true);
        return new ServletRegistrationBean<>(servlet, "/InsitelCollectionServicePse/*");
    }

    @Bean(name = "clientePSE")
    public DefaultWsdl11Definition clientePSEWsdl(XsdSchema clientePseSchema) {
        DefaultWsdl11Definition definition = new DefaultWsdl11Definition();
        definition.setPortTypeName("PSEHostingInvoicesWSSoap");
        definition.setLocationUri("/ws/clientePSE");
        definition.setTargetNamespace("https://pruebados.bitwan.info/InsitelCollectionServicePse");
        definition.setSchema(clientePseSchema);
        return definition;
    }

    @Bean
    public XsdSchema clientePseSchema() {
        return new SimpleXsdSchema(new ClassPathResource("clientePSE/clientePSE.xsd"));
    }

    
    // WSDL estático expuesto en /InsitelCollectionServicePse/wsdl
    @Bean(name = "InsitelCollectionServicePse")
    public SimpleWsdl11Definition insitelCollectionServicePseWsdl() {
        return new SimpleWsdl11Definition(
                new ClassPathResource("/wsdl/wsdlv2.wsdl")
        );
    }

    /**
     * Redirección manual para servir el archivo WSDL sin la extensión .wsdl
     * Permite acceder a:
     *   http://localhost:8080/InsitelCollectionServicePse/wsdl
     */
    @Bean
    public ServletRegistrationBean<HttpServlet> wsdlRedirectServlet() {
        HttpServlet servlet = new HttpServlet() {
             @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
            resp.setCharacterEncoding("UTF-8");
            resp.setContentType("text/xml; charset=UTF-8");
            var resource = new ClassPathResource("wsdl/wsdlv2.wsdl");
            try (var inputStream = resource.getInputStream()) {
                inputStream.transferTo(resp.getOutputStream());
            }
        }

        @Override
        protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
            // Redirige internamente a la ruta real del servicio SOAP
            resp.setStatus(HttpServletResponse.SC_TEMPORARY_REDIRECT);
            resp.setHeader("Location", req.getContextPath() + "/InsitelCollectionServicePse");
        }
        };

        // Mapea el servlet directamente a /InsitelCollectionServicePse/wsdl
        return new ServletRegistrationBean<>(servlet, "/InsitelCollectionServicePse/wsdl");
    }

    
}
