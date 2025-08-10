package com.gardrops.imageuploadapi.domain.port.out

import java.io.InputStream

interface ImageProcessingPort {
    fun processAndStore(image: InputStream, destinationPath: String)
}
