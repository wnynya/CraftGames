package com.github.lazoyoung.craftgames.exception

class FaultyConfiguration : Exception {
    constructor(message: String) : super(message)
    constructor(message: String, cause: Throwable) : super(message, cause)
}