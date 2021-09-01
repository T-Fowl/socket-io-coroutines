package com.tfowl.socketio

import kotlin.reflect.KClass


internal suspend inline fun <reified T : Throwable> assertThrows(noinline block: suspend () -> Unit): T {
    return assertThrows(T::class, block)
}

internal suspend fun <T : Throwable> assertThrows(clazz: KClass<T>, block: suspend () -> Unit): T {
    return try {
        block()
        throw AssertionError("Expected to throw $clazz")
    } catch (t: Throwable) {
        if (clazz.isInstance(t)) return t as T
        else throw AssertionError("Expected to throw $clazz but threw ${t::class} instead")
    }
}