package br.edu.ifsp.scl.persistence

import androidx.lifecycle.LiveData
import androidx.test.espresso.matcher.ViewMatchers.assertThat
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.not

infix fun Any.shouldBeEqualTo(value: Any) = assertThat(this, equalTo(value))
infix fun Any.shouldBeDifferentFrom(value: Any) = assertThat(this, not(value))

val <T> LiveData<T>.observedValue: T?
    get() {
        observeForever {}
        return value
    }