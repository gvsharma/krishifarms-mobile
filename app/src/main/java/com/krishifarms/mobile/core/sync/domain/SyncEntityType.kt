package com.krishifarms.mobile.core.sync.domain

enum class SyncEntityType(val apiValue: String) {
    FARMER("farmer"),
    EXPENSE("expense"),
    PROCUREMENT("procurement"),
    WORKER("worker"),
    DOCUMENT("document"),
}
