package za.ac.cput.config;

import org.apache.catalina.connector.Connector;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Chapter 9.7.3 "Adding HTTP/HTTPS Channel Security", for localhost.
 *
 * Only active with the "https" profile (see application-https.properties and
 * docs/SECURITY.md). Spring Boot serves one port, which is now HTTPS; this opens a second, plain
 * HTTP port whose only job is to send visitors on to the HTTPS one - the <port-mapping> from the
 * chapter. SecurityConfig adds the matching rule that every request must be secure.
 */
@Configuration
@Profile("https")
public class HttpsRedirectConfig {

    @Bean
    public WebServerFactoryCustomizer<TomcatServletWebServerFactory> httpRedirectConnector(
            @Value("${salon.http-redirect-port:8080}") int httpPort,
            @Value("${server.port:8443}") int httpsPort) {
        return factory -> {
            Connector connector = new Connector(TomcatServletWebServerFactory.DEFAULT_PROTOCOL);
            connector.setScheme("http");
            connector.setPort(httpPort);
            connector.setSecure(false);
            connector.setRedirectPort(httpsPort);
            factory.addAdditionalTomcatConnectors(connector);
        };
    }
}
