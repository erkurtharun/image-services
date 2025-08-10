package com.gardrops.imageprocessingapi.infrastructure.outbound.transform

import com.gardrops.imageprocessingapi.domain.model.ProcessSpec
import com.gardrops.imageprocessingapi.domain.port.out.ImageTransformer
import org.springframework.stereotype.Component
import java.awt.RenderingHints
import java.awt.geom.AffineTransform
import java.awt.image.AffineTransformOp
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import javax.imageio.IIOImage
import javax.imageio.ImageIO
import javax.imageio.ImageWriteParam
import javax.imageio.ImageWriter
import javax.imageio.stream.MemoryCacheImageOutputStream

@Component
class JavaxImageIoTransformer(
    private val spec: ProcessSpec
) : ImageTransformer {

    override fun transformToJpeg(source: InputStream): InputStream {
        val original = ImageIO.read(source) ?: error("Unsupported image format")
        val scaled = scaleWithin(original, spec.maxWidth, spec.maxHeight)

        val baos = ByteArrayOutputStream()
        var writer: ImageWriter? = null
        var ios: MemoryCacheImageOutputStream? = null
        try {
            writer = ImageIO.getImageWritersByFormatName("jpeg").next()
            ios = MemoryCacheImageOutputStream(baos)
            writer.output = ios

            val param: ImageWriteParam = writer.defaultWriteParam.apply {
                compressionMode = ImageWriteParam.MODE_EXPLICIT
                compressionQuality = spec.jpegQuality.coerceIn(0f, 1f)
            }
            writer.write(null, IIOImage(scaled, null, null), param)
        } finally {
            writer?.dispose()
            ios?.close()
        }

        return ByteArrayInputStream(baos.toByteArray())
    }

    private fun scaleWithin(img: BufferedImage, maxW: Int, maxH: Int): BufferedImage {
        val ratio = minOf(maxW.toDouble() / img.width, maxH.toDouble() / img.height, 1.0)
        val w = (img.width * ratio).toInt().coerceAtLeast(1)
        val h = (img.height * ratio).toInt().coerceAtLeast(1)
        if (w == img.width && h == img.height) return ensureRgb(img)

        val tx = AffineTransform().apply { scale(w.toDouble() / img.width, h.toDouble() / img.height) }
        val op = AffineTransformOp(tx, AffineTransformOp.TYPE_BICUBIC)
        val out = BufferedImage(w, h, BufferedImage.TYPE_INT_RGB)
        op.filter(ensureRgb(img), out)
        return out
    }

    private fun ensureRgb(img: BufferedImage): BufferedImage {
        if (img.type == BufferedImage.TYPE_INT_RGB) return img
        val out = BufferedImage(img.width, img.height, BufferedImage.TYPE_INT_RGB)
        val g = out.createGraphics()
        try {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
            g.drawImage(img, 0, 0, null)
        } finally {
            g.dispose()
        }
        return out
    }
}
