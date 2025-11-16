package com.visor.school.attendanceservice

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.client.discovery.EnableDiscoveryClient

@SpringBootApplication
@EnableDiscoveryClient
class AttendanceServiceApplication

fun main(args: Array<String>) {
    runApplication<AttendanceServiceApplication>(*args)
}

