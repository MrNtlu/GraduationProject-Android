package com.mrntlu.localsocialmedia.utils

import android.content.Context
import com.mrntlu.localsocialmedia.R
import com.afollestad.materialdialogs.MaterialDialog

class MaterialDialogUtil {
    companion object{
        fun showErrorDialog(context: Context, message: String) {
            MaterialDialog(context).show {
                title(text = context.getString(R.string.error_))
                message(text = message)
                negativeButton(text = context.getString(R.string.ok_)) {
                    it.dismiss()
                }
                cornerRadius(6f)
            }
        }

        fun showInfoDialog(context: Context, title: String, message: String) {
            MaterialDialog(context).show {
                title(text = title)
                message(text = message)
                negativeButton(text = context.getString(R.string.ok_)) {
                    it.dismiss()
                }
                cornerRadius(6f)
            }
        }

        fun setDialog(context: Context, message: String, dialogButton: DialogButtons) =
            setDialog(context, context.getString(R.string.are_you_sure), message, dialogButton)

        fun setDialog(context: Context, title: String, message: String, dialogButton: DialogButtons) = setDialog(context, title, message, dialogButton, null)

        fun setDialog(context: Context, title: String, message: String, dialogButton: DialogButtons, negativeButton: (()->Unit)? = null): MaterialDialog {
            return MaterialDialog(context).show {
                title(text = title)
                message(text = message)
                positiveButton(text = context.getString(R.string.yes)) {
                    dialogButton.positiveButton()
                    it.dismiss()
                }
                negativeButton(text = context.getString(R.string.no)) {
                    it.dismiss()
                    if (negativeButton != null)
                        negativeButton()
                }
                cornerRadius(6f)
            }
        }
    }
}
interface DialogButtons {
    fun positiveButton()
}