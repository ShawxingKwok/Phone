package pers.shawxingkwok.test.util

import kotlin.reflect.KFunction

internal fun assertAll(
    func: KFunction<*>,
    vararg argGroups: List<*>,
){
    val unacceptedArgs = mutableListOf<List<*>>()
    val throwables = mutableListOf<String>()

    argGroups.forEach { args ->
        try {
            func.call(*args.toTypedArray())
        } catch (tr: Throwable) {
            unacceptedArgs += args
            throwables += "$args ${tr.stackTraceToString()}"
        }
    }

    assert(throwables.none()){
        throwables.joinToString("\n", prefix = "Unaccepted arguments: $unacceptedArgs\n")
    }
}