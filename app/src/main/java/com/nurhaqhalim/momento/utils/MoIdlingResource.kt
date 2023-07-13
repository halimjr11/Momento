package com.nurhaqhalim.momento.utils

import androidx.test.espresso.idling.CountingIdlingResource

object MoIdlingResource {
    private const val RESOURCE = "REMOTE MEDIATOR"
    val idlingResource = CountingIdlingResource(RESOURCE)

    fun increment() {
        idlingResource.increment()
    }

    fun decrement() {
        idlingResource.decrement()
    }
}