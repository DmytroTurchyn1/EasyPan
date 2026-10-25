package com.cook.easypan.core.domain

interface NotificationPermissionChecker {
    val isRuntimePermissionRequired: Boolean
    fun isGranted(): Boolean
    fun subscribeToTopicIfGranted()
}
