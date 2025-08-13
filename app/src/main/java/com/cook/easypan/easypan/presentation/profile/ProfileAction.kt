/*
 * Created  14/8/2025
 *
 * Copyright (c) 2025 . All rights reserved.
 * Licensed under the MIT License.
 * See LICENSE file in the project root for details.
 */

sealed interface ProfileAction {
    data object OnSignOut : ProfileAction
    data object OnNotificationsClick : ProfileAction
    data object OnKeepScreenOnToggle : ProfileAction
    data object OnHelpClick : ProfileAction
}