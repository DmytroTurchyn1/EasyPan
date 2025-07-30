package com.cook.easypan.easypan.presentation.recipe_step.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.cook.easypan.R
import com.cook.easypan.core.presentation.EasyPanButtonPrimary
import com.cook.easypan.ui.theme.EasyPanTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertCancelRecipeDialog(
    icon: ImageVector,
    onConfirmation: () -> Unit,
    onDismissRequest: () -> Unit,
    dialogTitle: String,
    dialogText: String,
) {
    AlertDialog(
        icon = {
            Icon(
                imageVector = icon,
                contentDescription = stringResource(R.string.information_icon_description)
            )
        },
        title = {
            Text(text = dialogTitle)
        },
        text = {
            Text(text = dialogText)
        },
        onDismissRequest = {
            onDismissRequest()
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirmation()
                }
            ) {
                Text(stringResource(R.string.confirm_button))
            }
        },
        dismissButton = {
            EasyPanButtonPrimary(
                onClick = {
                    onDismissRequest()
                }
            ) {
                Text(stringResource(R.string.dismiss_button))
            }
        }
    )
}

@Preview
@Composable
private fun AlertCancelRecipeDialogPreview() {
    EasyPanTheme {
        AlertCancelRecipeDialog(
            icon = Icons.Default.Info,
            onConfirmation = { },
            onDismissRequest = { },
            dialogTitle = "Cancel Recipe",
            dialogText = "Are you sure you want to cancel this recipe?"
        )
    }

}