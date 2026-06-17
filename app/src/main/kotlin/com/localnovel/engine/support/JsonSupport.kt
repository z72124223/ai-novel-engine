package com.localnovel.engine.support

import kotlinx.serialization.json.Json

object JsonSupport {
    val json: Json = Json {
        prettyPrint = true
        encodeDefaults = true
        ignoreUnknownKeys = true
    }
}
