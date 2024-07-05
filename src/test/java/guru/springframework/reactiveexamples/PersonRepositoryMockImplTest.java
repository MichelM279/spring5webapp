package guru.springframework.reactiveexamples;

import guru.springframework.reactiveexamples.domain.Person;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PersonRepositoryMockImplTest {

    PersonRepository personRepository;

    @BeforeEach
    void setUp() {
        personRepository = new PersonRepositoryMockImpl();
    }

    @Test
    void getByIdBlock() {
        Mono<Person> personMono = personRepository.getById(1);

        Person person = personMono.block();

        System.out.println(person.toString());
    }

    @Test
    void getByIdSubscribe() {
        Mono<Person> personMono = personRepository.getById(1);

        StepVerifier.create(personMono)
                .assertNext(person -> {
                    assertEquals("Michael", person.getFirstName());
                    assertEquals("Weston", person.getLastName());
                })
                .verifyComplete();

        personMono.subscribe(person -> {
            System.out.println(person.toString());
        });
    }

    @Test
    void getByIdSubscribeNotFound() {
        Mono<Person> personMono = personRepository.getById(8);

        StepVerifier.create(personMono).expectNextCount(0).verifyComplete();
    }

    @Test
    void getByIdMapFunctionNoSub() {
        Mono<Person> personMono = personRepository.getById(1);

        personMono.map(person -> {
            System.out.println("no print");
            return person.getFirstName();
        });

        StepVerifier.create(personMono)
                .assertNext(person -> {
                    assertEquals("Michael", person.getFirstName());
                    assertEquals("Weston", person.getLastName());
                })
                .verifyComplete();
    }

    @Test
    void getByIdMapFunction() {
        Mono<Person> personMono = personRepository.getById(1);

        personMono.map(person -> {
            System.out.println("in map: " + person.toString());
            return person.getFirstName();
        }).subscribe(firstName -> {
            System.out.println("from map: " + firstName);
        });

        StepVerifier.create(personMono)
                .assertNext(person -> {
                    assertEquals("Michael", person.getFirstName());
                    assertEquals("Weston", person.getLastName());
                })
                .verifyComplete();
    }

    @Test
    void fluxTestBlockFirst() {
        Flux<Person> personFlux = personRepository.findAll();

        Person person = personFlux.blockFirst(); // should not use block, just for demo purposes

        System.out.println(person.toString());
    }

    @Test
    void testFluxSubscribe() {
        Flux<Person> personFlux = personRepository.findAll();

        StepVerifier.create(personFlux).expectNextCount(4).verifyComplete();

        personFlux.subscribe(person -> {
            System.out.println(person.toString());
        });
    }

    @Test
    void testFluxToListMono() {
        Flux<Person> personFlux = personRepository.findAll();

        Mono<List<Person>> personListMono = personFlux.collectList();

        personListMono.subscribe(list -> {
            list.forEach(person -> {
                System.out.println(person.toString());
            });
        });
    }

    @Test
    void testFindPersonById() {
        Flux<Person> personFlux = personRepository.findAll();

        final Integer id = 3;

        Mono<Person> personMono = personFlux.filter(person -> Objects.equals(person.getId(), id)).next();

        personMono.subscribe(person -> {
            System.out.println(person.toString());
        });
    }

    @Test
    void testFindPersonByIdNotFound() {
        Flux<Person> personFlux = personRepository.findAll();

        final Integer id = 8;

        Mono<Person> personMono = personFlux.filter(person -> Objects.equals(person.getId(), id)).next();

        personMono.subscribe(person -> {
            System.out.println(person.toString());
        });
    }





    @Test
    void testFindPersonByIdNotFoundWithException() {
        Flux<Person> personFlux = personRepository.findAll();

        final Integer id = 8;

        Mono<Person> personMono = personFlux.filter(person -> Objects.equals(person.getId(), id)).single();

        personMono.doOnError(throwable -> System.out.println("No person found for id: " + id))
                .onErrorReturn(Person.builder().build())
                .subscribe(person -> System.out.println(person.toString()));
    }
}