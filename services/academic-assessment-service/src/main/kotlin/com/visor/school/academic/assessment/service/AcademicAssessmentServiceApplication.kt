package com.visor.school.academic.assessment.service

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.client.discovery.EnableDiscoveryClient

@SpringBootApplication
@EnableDiscoveryClient
class AcademicAssessmentServiceApplication

fun main(args: Array<String>) {
    runApplication<AcademicAssessmentServiceApplication>(*args)
}

