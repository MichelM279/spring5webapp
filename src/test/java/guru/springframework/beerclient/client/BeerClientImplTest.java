package guru.springframework.beerclient.client;

import guru.springframework.beerclient.config.WebClientConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class BeerClientImplTest {

    BeerClient beerClient;

    @BeforeEach
    void setUp() {
        beerClient = new BeerClientImpl(new WebClientConfig().beerWebClient());
    }

    @Test
    void listBeers() {
    }

    @Test
    void getBeerById() {
    }

    @Test
    void createBeer() {
    }

    @Test
    void updateBeer() {
    }

    @Test
    void deleteBeerById() {
    }

    @Test
    void getBeerByUPC() {
    }
}