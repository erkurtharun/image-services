package com.gardrops.imageprocessingapi.domain.port.`in`

import java.io.InputStream

interface ProcessImage {
    fun processAndStore(input: InputStream, destinationPath: String): String
}
