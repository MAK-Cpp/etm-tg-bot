package ru.makcpp.etm_solutions_bot

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@SpringBootApplication
@ConfigurationPropertiesScan
class EtmSolutionsBotApplication

fun main(args: Array<String>) {
    runApplication<EtmSolutionsBotApplication>(*args)
}