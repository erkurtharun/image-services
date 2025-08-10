package com.gardrops.shared.logging

import ch.qos.logback.classic.spi.ILoggingEvent
import com.fasterxml.jackson.core.JsonGenerator
import net.logstash.logback.composite.AbstractJsonProvider
import net.logstash.logback.composite.JsonWritingUtils


class LogMaskingMessageJsonProvider : AbstractJsonProvider<ILoggingEvent>() {

    override fun writeTo(generator: JsonGenerator, event: ILoggingEvent) {
        val original = event.formattedMessage ?: event.message ?: ""
        val masked = SensitiveDataMasker.mask(original)
        JsonWritingUtils.writeStringField(generator, "message", masked)
    }
}
