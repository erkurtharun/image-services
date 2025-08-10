package com.gardrops.imageprocessingapi.domain.port.out

import java.io.InputStream

interface ImageStorage {
    fun save(destinationPath: String, imageJpeg: InputStream): String
    fun delete(path: String): Boolean = false
    fun exists(path: String): Boolean = false
}