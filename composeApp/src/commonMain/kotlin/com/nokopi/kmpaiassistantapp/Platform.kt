package com.nokopi.kmpaiassistantapp

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform