package com.bitwan.recaudosoap.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "api")
public class ApiProperties {

    private Map<String, ApiConfig> clients;

    public Map<String, ApiConfig> getClients() {
        return clients;
    }

    public void setClients(Map<String, ApiConfig> clients) {
        this.clients = clients;
    }

    public static class ApiConfig {
        private String url;
        private AuthConfig auth;

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public AuthConfig getAuth() {
            return auth;
        }

        public void setAuth(AuthConfig auth) {
            this.auth = auth;
        }
    }

    public static class AuthConfig {
        private String type; // basic, bearer, none
        private String username;
        private String password;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }
}
