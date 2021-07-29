package com.tfowl.io.socket

import kotlin.reflect.KClass

internal fun interface SuspendingExecutable {

    @Throws(Throwable::class)
    suspend fun execute()
}

internal suspend inline fun <reified T : Throwable> assertThrows(noinline block: suspend () -> Unit): T {
    return assertThrows(T::class, SuspendingExecutable(block))
}

internal suspend fun <T : Throwable> assertThrows(clazz: KClass<T>, executable: SuspendingExecutable): T {
    return try {
        executable.execute()
        throw AssertionError("Expected to throw $clazz")
    } catch (t: Throwable) {
        if (clazz.isInstance(t)) return t as T
        else throw AssertionError("Expected to throw $clazz but threw ${t::class} instead")
    }
}