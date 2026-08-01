package com.openfinds.app.core.network.json

import kotlinx.serialization.json.Json

val OpenFindJson: Json =
    Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        classDiscriminator = "type"
    }
