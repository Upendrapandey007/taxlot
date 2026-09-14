package com.taxlot.tax

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class TaxServiceApplication

fun main(args: Array<String>) {
    runApplication<TaxServiceApplication>(*args)
}
