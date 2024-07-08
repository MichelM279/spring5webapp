package guru.springframework.beerclient.client;

import guru.springframework.beerclient.config.WebClientConfig;
import guru.springframework.beerclient.model.BeerDto;
import guru.springframework.beerclient.model.BeerPagedList;
import org.assertj.core.api.AtomicReferenceArrayAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BeerClientImplTest {

    private BeerClient beerClient;

    private static String  MY_BEER_NAME = "its very good";

    @BeforeEach
    void setUp() {
        beerClient = new BeerClientImpl(new WebClientConfig().beerWebClient());
    }

    @Test
    void listBeers() {
        Mono<BeerPagedList> beerPagedListMono = beerClient.listBeers(null, null, null, null, null);
        BeerPagedList beerPagedList = beerPagedListMono.block();

        assertThat(beerPagedList).isNotNull();
        assertThat(beerPagedList.getContent().size()).isGreaterThan(0);
        System.out.println(beerPagedList.toList());
    }

    @Test
    void listBeersPageSize10() {
        Mono<BeerPagedList> beerPagedListMono = beerClient.listBeers(1, 10, null, null, null);
        BeerPagedList beerPagedList = beerPagedListMono.block();

        assertThat(beerPagedList).isNotNull();
        assertThat(beerPagedList.getContent().size()).isEqualTo(10);
    }

    @Test
    void listBeersNoRecords() {
        Mono<BeerPagedList> beerPagedListMono = beerClient.listBeers(10, 20, null, null, null);
        BeerPagedList beerPagedList = beerPagedListMono.block();

        assertThat(beerPagedList).isNotNull();
        assertThat(beerPagedList.getContent().size()).isEqualTo(0);
    }


    @Test
    void getBeerById() {
        Mono<BeerPagedList> beerPagedListMono = beerClient.listBeers(null, null, null, null, null);
        UUID id = beerPagedListMono.block().stream().findFirst().get().getId();

        Mono<BeerDto> beerDtoMono = beerClient.getBeerById(id, null);

        assertThat(beerDtoMono.block()).isNotNull();
    }

    @Test
    void createBeer() {
        BeerDto beerDto = BeerDto.builder()
                .beerName("Koelsch Finkenstyle")
                .beerStyle("IPA")
                .upc("234848549558")
                .price(new BigDecimal("5.99"))
                .build();
        Mono<ResponseEntity<Void>> responseEntityMono = beerClient.createBeer(beerDto);

        ResponseEntity responseEntity = responseEntityMono.block();

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    @Test
    void updateBeer() {
        Mono<BeerPagedList> beerPagedListMono = beerClient.listBeers(null, null, null, null, null);
        BeerPagedList pagedList = beerPagedListMono.block();
        BeerDto beerDto = pagedList.getContent().get(0);
        BeerDto updatedBeer = BeerDto.builder()
                .beerName(MY_BEER_NAME)
                .beerStyle(beerDto.getBeerStyle())
                .price(beerDto.getPrice())
                .upc(beerDto.getUpc()
                ).build();

        Mono<ResponseEntity<Void>> responseEntityMono = beerClient.updateBeer(beerDto.getId(), updatedBeer);
        ResponseEntity<Void> responseEntity = responseEntityMono.block();

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    void deleteBeerById() {
        Mono<BeerPagedList> beerPagedListMono = beerClient.listBeers(null, null, null, null, null);
        BeerPagedList pagedList = beerPagedListMono.block();
        BeerDto beerDto = pagedList.getContent().get(0);

        Mono<ResponseEntity<Void>> responseEntityMono = beerClient.deleteBeerById(beerDto.getId());
        ResponseEntity<Void> responseEntity = responseEntityMono.block();

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    void deleteBeerByIdNotFound() {

        Mono<ResponseEntity<Void>> responseEntityMono = beerClient.deleteBeerById(UUID.randomUUID());

        assertThrows(WebClientResponseException.class, () -> {
            ResponseEntity<Void> responseEntity = responseEntityMono.block();
            assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        });
    }

    @Test
    void deleteBeerHandleException() {
        Mono<ResponseEntity<Void>> responseEntityMono = beerClient.deleteBeerById(UUID.randomUUID());

        ResponseEntity<Void> responseEntity = responseEntityMono.onErrorResume(throwable -> {
            if (throwable instanceof WebClientResponseException) {
                WebClientResponseException exception = (WebClientResponseException) throwable;
                return Mono.just(ResponseEntity.status(exception.getStatusCode()).build());
            } else {
                throw new RuntimeException(throwable);
            }
        }).block();

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void getBeerByUPC() {
        Mono<BeerPagedList> beerPagedListMono = beerClient.listBeers(null, null, null, null, null);
        String upc = beerPagedListMono.block().stream().findFirst().get().getUpc();

        Mono<BeerDto> beerDtoMono = beerClient.getBeerByUPC(upc);

        assertThat(beerDtoMono.block()).isNotNull();
    }

    @Test
    void functionalTestGetBeerById() throws InterruptedException {
        AtomicReference<String> beerName = new AtomicReference<>();
        CountDownLatch countDownLatch = new CountDownLatch(1); // Expecting 1 instance

        beerClient.listBeers(null, null, null, null, null)
                .map(beerPagedList -> beerPagedList.getContent().get(0).getId())
                .map(beerId -> beerClient.getBeerById(beerId, false))
                .flatMap(mono -> mono)
                .subscribe(beerDto -> {
                    System.out.println(beerDto.getBeerName());
                    beerName.set(beerDto.getBeerName());
                    assertThat(beerDto.getBeerName()).isEqualTo(MY_BEER_NAME); // doesnt work here, race condition
                    countDownLatch.countDown(); // last position, or race condition to below assertthat
                });

        // Thread.sleep(5000); // dont to, instead countdownlatch
        countDownLatch.await(); // usage mainly for tests
        assertThat(beerName.get()).isEqualTo(MY_BEER_NAME);
    }
}