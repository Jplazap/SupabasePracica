package com.example.supabasepracica

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.supabasepracica.data.SupabaseManager
import com.example.supabasepracica.models.Alumno
import com.example.supabasepracica.utils.SupabaseErrorHandler
import com.google.android.material.card.MaterialCardView
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
        )

        setContentView(R.layout.activity_main)

        val pantalla = findViewById<LinearLayout>(R.id.main)
        ViewCompat.setOnApplyWindowInsetsListener(pantalla) { view, insets ->
            val barras = insets.getInsets(
                WindowInsetsCompat.Type.systemBars() or
                        WindowInsetsCompat.Type.displayCutout()
            )
            view.setPadding(barras.left, barras.top, barras.right, barras.bottom)
            insets
        }

        val estado = findViewById<TextView>(R.id.estadoAlumnos)
        val lista = findViewById<LinearLayout>(R.id.listaAlumnos)

        consultarAlumnos(estado, lista)
    }

    private fun consultarAlumnos(estado: TextView, lista: LinearLayout) {
        estado.text = "Cargando alumnos…"

        lifecycleScope.launch {
            try {
                val alumnos = withTimeout(15_000L) {
                    SupabaseManager.client
                        .from("alumnos")
                        .select { order("nombres", Order.ASCENDING) }
                        .decodeList<Alumno>()
                }

                estado.text = when (alumnos.size) {
                    0 -> "No se encontraron alumnos."
                    1 -> "1 alumno"
                    else -> "${alumnos.size} alumnos"
                }

                alumnos.forEach { alumno ->
                    lista.addView(crearTarjeta(alumno))
                }
            } catch (e: TimeoutCancellationException) {
                estado.text = "Supabase no respondió en 15 segundos."
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                estado.text = "No se pudo cargar la lista."
                SupabaseErrorHandler.show(this@MainActivity, e)
            }
        }
    }

    private fun crearTarjeta(alumno: Alumno): MaterialCardView {
        val tarjeta = MaterialCardView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = dp(12)
            }

            radius = dp(16).toFloat()
            cardElevation = dp(1).toFloat()
            setCardBackgroundColor(Color.WHITE)
            strokeWidth = dp(1)
            setStrokeColor(Color.parseColor("#DCE4EF"))
        }

        val contenido = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(14), dp(16), dp(14))
        }

        val nombre = TextView(this).apply {
            text = alumno.nombres
            textSize = 17f
            setTextColor(Color.parseColor("#14243D"))
            setTypeface(typeface, Typeface.BOLD)
        }

        val datos = TextView(this).apply {
            text = "Paralelo: ${alumno.paralelo}\n" +
                    "Correo: ${alumno.correo ?: "Sin registrar"}\n" +
                    "Teléfono: ${alumno.telefono ?: "Sin registrar"}"
            textSize = 14f
            setTextColor(Color.parseColor("#52647B"))
            setPadding(0, dp(6), 0, 0)
            setLineSpacing(dp(3).toFloat(), 1f)
        }

        contenido.addView(nombre)
        contenido.addView(datos)
        tarjeta.addView(contenido)

        return tarjeta
    }

    private fun dp(valor: Int): Int =
        (valor * resources.displayMetrics.density).roundToInt()
}