package com.featurevisor.sdk

import com.featurevisor.types.EventName

class Emitter {
    private val listeners = mutableMapOf<EventName, (Array<out Any>) -> Unit>()

    fun addListener(event: EventName, listener: (Array<out Any>) -> Unit) {
        listeners.putIfAbsent(event, listener)
    }

    fun removeListener(event: EventName) {
        listeners.remove(event)
    }

    fun removeAllListeners() {
        listeners.clear()
    }

    fun emit(event: EventName, vararg args: Any) {
        listeners.getOrDefault(event, null)?.invoke(args)
    }
}
