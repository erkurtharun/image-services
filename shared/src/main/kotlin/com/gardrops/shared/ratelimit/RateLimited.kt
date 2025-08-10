package com.gardrops.shared.ratelimit

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class RateLimited(
    val limitPerMinute: Int = 10
)
