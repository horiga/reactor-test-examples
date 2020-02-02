package org.horiga.study.reactortest

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.reactivestreams.Subscription
import org.slf4j.LoggerFactory
import reactor.core.publisher.BaseSubscriber
import reactor.core.publisher.Flux
import reactor.test.StepVerifier

class ReactorTests {

    companion object {
        val log = LoggerFactory.getLogger(ReactorTests::class.java)!!
    }

    data class Ship(
        val name: String,
        val availableNumberOfPassengers: Int,
        val numberOfPassengers: Int = 0
    )

    class ShipSubscriber : BaseSubscriber<Ship>() {

        override fun hookOnSubscribe(subscription: Subscription) {
            log.info("Test START!! (hookOnSubscribe)")
            super.hookOnSubscribe(subscription)
        }

        override fun hookOnComplete() {
            log.info("Test END!! (hookOnComplete)")
        }

        override fun hookOnNext(value: Ship) {
            log.info("hookOnNext: $value")
        }

        override fun hookOnError(throwable: Throwable) {
            log.warn("hookOnError!! ${throwable.message}")
        }
    }

    @Test
    fun `test StepVerifier with expects`() {
        val flux = Flux.just(
            Ship("Merry", 30),
            Ship("Akagi", 150),
            Ship("Asuka", 1000),
            Ship("Santa Maria", 600)
        ).apply {
            subscribe(ShipSubscriber())
        }

        StepVerifier.create(flux)
            .expectNextCount(4)
            .expectNextMatches { it.name == "Merry" }
            .expectNextMatches { it.name == "Akagi" }
            .expectNextMatches { it.name == "Asuka" }
            .expectNextMatches { it.availableNumberOfPassengers == 600 }
            .expectComplete()
            .verify()

//        val fluxWithError = flux.concatWith(
//            Mono.error(IllegalStateException("Passenger too much"))
//        )
//
//        StepVerifier.create(fluxWithError)
//            .expectNextCount(4)
//            .expectError(IllegalStateException::class.java)
//            .verify()
    }

    @Test
    fun `test StepVerifier with consume`() {
        val flux = Flux.just("candy", "chocolate", "cookie").apply {
            subscribe { s -> log.info(">> $s") }
        }.map { s -> s.toUpperCase() }

        StepVerifier.create(flux)
            .recordWith { mutableListOf() }
            .thenConsumeWhile { value ->
                log.info("in `thenConsumeWhile(): $value`")
                true
            }
            .consumeRecordedWith { elements ->
                log.info("in `consumeRecordedWith()`: $elements, assert isNotEmpty()")
                Assertions.assertThat(elements).isNotEmpty()
            }
            .verifyComplete()
    }

}