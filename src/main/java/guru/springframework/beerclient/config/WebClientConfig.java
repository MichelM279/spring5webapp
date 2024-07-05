package guru.springframework.beerclient.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient beerWebClient() {
        return WebClient.builder().baseUrl(WebClientProperties.BASE_URL).build();
    }
}
