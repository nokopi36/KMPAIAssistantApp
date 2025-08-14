package com.nokopi.kmpaiassistantapp.debug

import org.koin.core.context.GlobalContext

object DebugInfo {
    fun logKoinModules() {
        val koin = GlobalContext.getOrNull()
        if (koin != null) {
            println("=== Koin Debug Info ===")
            println("Koin is initialized: ${koin != null}")
            println("Koin instance: $koin")
        } else {
            println("Koin is NOT initialized!")
        }
    }
}