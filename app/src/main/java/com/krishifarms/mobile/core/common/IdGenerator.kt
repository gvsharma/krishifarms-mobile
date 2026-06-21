package com.krishifarms.mobile.core.common

import java.util.UUID

object IdGenerator {
    fun newLocalId(): String = "local_${UUID.randomUUID()}"
}
