package br.edu.ifsp.scl.common

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations

infix fun <T, R> LiveData<T>.mappedTo(transform: (T) -> R) =
    Transformations.map(this) { transform(it) }

infix fun <T, R> LiveData<T>.mediatedBy(transform: (T) -> R) =
    MediatorLiveData<R>().apply {
        addSource(this@mediatedBy) { value = transform(it) }
    } as MutableLiveData<R>