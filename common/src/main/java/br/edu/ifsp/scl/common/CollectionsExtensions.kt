package br.edu.ifsp.scl.common

fun <K, V> MutableMap<K, V>.update(key: K, transform: (old: V) -> V) =
    get(key)?.let { set(key, transform(it)) }