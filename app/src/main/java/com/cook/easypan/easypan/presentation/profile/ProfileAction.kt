sealed interface ProfileAction {
    data object OnSignOut : ProfileAction
    data object OnNotificationsClick : ProfileAction
    data object OnHelpClick : ProfileAction
}