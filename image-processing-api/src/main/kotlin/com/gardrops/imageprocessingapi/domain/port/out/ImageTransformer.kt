package com.gardrops.imageprocessingapi.domain.port.out

import java.io.InputStream

interface ImageTransformer {
    fun transformToJpeg(source: InputStream): InputStream
}
