package com.example.craftbeer

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import java.io.File

@SpringBootApplication
class CraftbeerApplication

fun main(args: Array<String>) {
	runApplication<CraftbeerApplication>(*args)
}

@RestController
class MessageResource {
    @GetMapping
    fun index(): String = File("/data/beers.json").readText(Charsets.UTF_8)
}
