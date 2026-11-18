package com.example.supabasepracica.utils

import android.content.Context
import com.google.android.material.dialog.MaterialAlertDialogBuilder

object SupabaseErrorHandler {
    fun show(context: Context, error: Throwable) {
        MaterialAlertDialogBuilder(context)
            .setTitle("Error de conexión")
            .setMessage(error.localizedMessage ?: "No se pudo completar la consulta.")
            .setPositiveButton("Aceptar", null)
            .show()
    }
}