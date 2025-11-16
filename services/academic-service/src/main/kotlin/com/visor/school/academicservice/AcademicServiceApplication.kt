package com.visor.school.academicservice

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.client.discovery.EnableDiscoveryClient

@SpringBootApplication
@EnableDiscoveryClient
class AcademicServiceApplication

fun main(args: Array<String>) {
    runApplication<AcademicServiceApplication>(*args)
}

