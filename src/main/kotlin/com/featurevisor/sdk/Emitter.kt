package com.featurevisor.sdk

import com.featurevisor.types.EventName

class Emitter {
    private val listeners = mutableMapOf<EventName, () -> Unit>()

    fun addListener(event: EventName, listener: () -> Unit) {
        listeners.putIfAbsent(event, listener)
    }

    fun removeListener(event: EventName) {
        listeners.remove(event)
    }

    fun removeAllListeners() {
        listeners.clear()
    }

    operator fun invoke(event: EventName) {
        listeners.getOrDefault(event, null)?.invoke()
    }
}
