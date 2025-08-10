package com.gardrops.imageuploadapi.application.usecase

import java.util.*

interface ListSessionImages {
    fun list(sessionId: UUID): List<UUID>
}
