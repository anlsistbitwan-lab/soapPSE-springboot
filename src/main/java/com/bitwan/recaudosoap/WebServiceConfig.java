package com.bitwan.recaudosoap;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.ws.config.annotation.EnableWs;
import org.springframework.ws.config.annotation.WsConfigurer;
import org.springframework.ws.config.annotation.WsConfigurerAdapter;
import org.springframework.ws.server.EndpointInterceptor;
import org.springframework.ws.transport.http.MessageDispatcherServlet;
import org.springframework.ws.wsdl.wsdl11.DefaultWsdl11Definition;
import org.springframework.ws.wsdl.wsdl11.SimpleWsdl11Definition;
import org.springframework.ws.wsdl.wsdl11.Wsdl11Definition;
import org.springframework.xml.xsd.SimpleXsdSchema;
import org.springframework.xml.xsd.XsdSchema;

import com.bitwan.recaudosoap.config.WsAddressingAndSecurityInterceptor;
//import com.bitwan.recaudosoap.config.CustomNamespacePrefixMapper;
import jakarta.xml.bind.Marshaller;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Configuration
@EnableWs
public class WebServiceConfig implements WsConfigurer{

    @Bean
    public ServletRegistrationBean<MessageDispatcherServlet> messageDispatcherServlet(ApplicationContext applicationContext) {
        MessageDispatcherServlet servlet = new MessageDispatcherServlet();
        servlet.setApplicationContext(applicationContext);
        servlet.setTransformWsdlLocations(true);
        return new ServletRegistrationBean<>(servlet, "/InsitelCollectionServicePse/*");
    }

    
    /**
     * Redirección manual para servir el archivo WSDL sin la extensión .wsdl
     * Permite acceder a:
     *   http://localhost:8080/InsitelCollectionServicePse/wsdl
     */
    @Bean
    public ServletRegistrationBean<HttpServlet> wsdlRedirectServlet(Environment env) {
        HttpServlet servlet = new HttpServlet() {
            @Override
            protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
                resp.setCharacterEncoding("UTF-8");
                resp.setContentType("text/xml; charset=UTF-8");

                // 1) Intenta leer de Spring (application.properties/yml) -> key: service.url
                // 2) Si no existe, Default local
                String serviceUrl =
                        env.getProperty("wsdl.address",
                        "http://localhost:8080/InsitelCollectionServicePse/wsdl");

                var resource = new ClassPathResource("wsdl/wsdlv2.wsdl");
                String wsdl = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
                wsdl = wsdl.replace("${SERVICE_URL}", serviceUrl);
                resp.getWriter().write(wsdl);
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

    // Registrar Interceptor personalizado WS-Addressing y WS-Security
    @Override
    public void addInterceptors(List<EndpointInterceptor> interceptors) {
        interceptors.add(wsAddressingAndSecurityInterceptor());
    }

    @Bean
    public WsAddressingAndSecurityInterceptor wsAddressingAndSecurityInterceptor() {
        return new WsAddressingAndSecurityInterceptor();
    }

    //Marshallers JAXB configurado con prefijo ns2 para el namespace del contrato actual PSE.
    /*@Bean
    public Jaxb2Marshaller marshaller() {
        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        marshaller.setPackagesToScan("com.bitwan.soap.clientePSE");

        // Configuración personalizada del prefijo ns2
        CustomNamespacePrefixMapper.configure(marshaller);

        return marshaller;
    }*/

    

    
}
