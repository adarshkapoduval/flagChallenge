package com.adarsh.flag.utils

fun formatMs(ms: Long): String {
    val s = (ms / 1000).coerceAtLeast(0L)
    val m = s / 60
    val sec = s % 60
    return "%02d:%02d".format(m, sec)
}