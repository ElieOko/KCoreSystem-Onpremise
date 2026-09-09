package com.schoolstats.util

actual fun currentTimeMillis(): Long = System.currentTimeMillis()

actual fun randomUuid(): String = java.util.UUID.randomUUID().toString()
