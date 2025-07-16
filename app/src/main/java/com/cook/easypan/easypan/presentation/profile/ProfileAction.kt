sealed interface ProfileAction {
    data object OnSignOut : ProfileAction
    data object OnNotificationsToggle : ProfileAction
}