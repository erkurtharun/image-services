package com.gardrops.shared.redis

object RedisKeys {
    fun sessionKey(sessionId: String) = "sess:$sessionId"
    fun sessionImagesKey(sessionId: String) = "sess:$sessionId:imgs"
    fun rateKey(ns: String, ip: String, method: String, path: String) =
        "$ns:rl:$ip:$method:$path"
}
