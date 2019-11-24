package br.edu.ifsp.scl.persistence

import androidx.lifecycle.LiveData
import androidx.test.espresso.matcher.ViewMatchers.assertThat
import org.hamcrest.CoreMatchers.*

infix fun Any.shouldBeEqualTo(value: Any) = assertThat(this, equalTo(value))
infix fun Any.shouldBeDifferentFrom(value: Any) = assertThat(this, not(value))

infix fun Iterable<Any>.shouldContain(item: Any) = assertThat(this, hasItems(item))
fun Iterable<Any>.shouldContain(vararg items: Any) = items.forEach { this shouldContain it }

infix fun Iterable<Any>.shouldNotContain(item: Any) = assertThat(this, not(hasItems(item)))
fun Iterable<Any>.shouldNotContain(vararg items: Any) = items.forEach { this shouldNotContain it }

val <T> LiveData<T>.observedValue: T?
    get() {
        observeForever {}
        return value
    }