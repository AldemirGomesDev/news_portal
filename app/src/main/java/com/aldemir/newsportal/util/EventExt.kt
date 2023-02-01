package com.aldemir.newsportal.util

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

fun <T> MutableLiveData<Event<T>>.emit(eventValue: T) {
    value = Event(eventValue)
}

fun MutableLiveData<Event<Unit>>.emit() {
    emit(Unit)
}

fun <T> MutableLiveData<T>.asLiveData(): LiveData<T> = this

fun <T> Fragment.onEvent(eventSource: LiveData<Event<T>>, handler: (T) -> Unit) {
    eventSource.observe(viewLifecycleOwner, EventObserver { handler(it) })
}

fun <T> AppCompatActivity.onEvent(eventSource: LiveData<Event<T>>, handler: (T) -> Unit) {
    eventSource.observe(this, EventObserver { handler(it) })
}
