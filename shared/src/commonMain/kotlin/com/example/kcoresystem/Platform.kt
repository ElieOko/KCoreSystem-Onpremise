package com.example.kcoresystem

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform