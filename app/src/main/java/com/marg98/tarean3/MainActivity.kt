package com.marg98.tarean3

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.get
import com.airbnb.lottie.LottieAnimationView
import com.github.javafaker.Faker
import com.google.android.flexbox.FlexboxLayout
import kotlin.random.Random

class MainActivity : AppCompatActivity() {
    private lateinit var txtPregunta: TextView
    private lateinit var btnNuevoJuego: Button
    private var respuesta: String = ""
    private lateinit var flexAlfabeto: FlexboxLayout
    private lateinit var flexResponse: FlexboxLayout
    private var indicesOcupados: ArrayList<Int> = arrayListOf()
    private var intentosPermitidos: Int = 0
    private var intentosHechos: Int = 0
    private lateinit var txtCantIntentos: TextView
    private lateinit var txtMsjIntentos: TextView
    private var finalizado: Boolean = false
    private lateinit var lottieResult: LottieAnimationView
    private lateinit var lotieAnimThinking: LottieAnimationView
    private lateinit var textMsjResultado: TextView
    private lateinit var txtMsjRespuestaCorrecta: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        setContentView(R.layout.activity_main)

        txtPregunta = findViewById(R.id.txtPregunta)
        lotieAnimThinking = findViewById(R.id.animation_view_thik)
        flexResponse = findViewById(R.id.edt)
        flexAlfabeto = findViewById(R.id.flexboxLayout)
        txtCantIntentos = findViewById(R.id.txtCantIntentos)
        txtMsjIntentos = findViewById(R.id.txtMsjIntentos)
        lottieResult = findViewById(R.id.animation_view_resultado)
        textMsjResultado = findViewById(R.id.txtMsjResultado)
        txtMsjRespuestaCorrecta = findViewById(R.id.txtMsjRespuestaCorrecta)

        //1. generar palabra a adivinar
//1.1 la cantidad de intetos permitidos se le dara: tamaño de caracteres + 3
        respuesta = obtenerPalabraAleatoria().uppercase()
        intentosPermitidos = respuesta.length + 3
        txtCantIntentos.text = "$intentosHechos/$intentosPermitidos"
//2. generar alfabeto que incluya las letras de la palabra a adivinar
        val alfabeto =
            generarAlfabeto(respuesta)//3. desordenar el alfabeto generado para que sea mas dinamica
        val alfabetoDesorden = desordenar(alfabeto)
//4. generar los espacios donde se iran mostrando la respuesta
        mostrarEspacioRespuesta(respuesta.length, flexResponse)
//4. mostrar en la vista cada letra generada como boton para que se pueda seleccionar
        mostrarAlfabeto(alfabetoDesorden.uppercase(), flexAlfabeto)

        btnNuevoJuego = findViewById(R.id.btnNuevoJuego)
        btnNuevoJuego.visibility = View.GONE

        btnNuevoJuego.setOnClickListener {
            reiniciarJuego()
            btnNuevoJuego.visibility = View.GONE
        }
    }

    fun generarAlfabeto(semilla: String): String {
        val randomValues = List(5) { Random.nextInt(65, 90).toChar() }
        return "$semilla${randomValues.joinToString(separator = "")}"
    }

    fun desordenar(theWord: String): String {
        val theTempWord = theWord.toMutableList()
        for (item in 0..Random.nextInt(1, theTempWord.count() - 1)) {
            val indexA = Random.nextInt(theTempWord.count() - 1)
            val indexB = Random.nextInt(theTempWord.count() - 1)
            val temp = theTempWord[indexA]
            theTempWord[indexA] = theTempWord[indexB]
            theTempWord[indexB] = temp
        }
        return theTempWord.joinToString(separator = "")
    }

    fun obtenerPalabraAleatoria(): String {
        val faker = Faker()
        val palabra = faker.artist().name()
        return palabra.split(' ').get(0) //a veces devuelve nombres compuestos
    }

    fun mostrarEspacioRespuesta(cantidad: Int, vista: FlexboxLayout) {
        for (letter in 1..cantidad) {
            val btnLetra = EditText(this)
            btnLetra.isEnabled = false
            val layoutParams = FlexboxLayout.LayoutParams(
                FlexboxLayout.LayoutParams.WRAP_CONTENT,
                FlexboxLayout.LayoutParams.WRAP_CONTENT
            )
            layoutParams.setMargins(5, 5, 5, 5)
            btnLetra.layoutParams = layoutParams
            vista.addView(btnLetra)
        }
    }

    fun mostrarAlfabeto(alfabeto: String, vista: FlexboxLayout) {
        for (letter in alfabeto) {
            val btnLetra = Button(this)
            btnLetra.text = letter.toString()
            btnLetra.textSize = 12f
            val layoutParams = FlexboxLayout.LayoutParams(
                FlexboxLayout.LayoutParams.WRAP_CONTENT,
                FlexboxLayout.LayoutParams.WRAP_CONTENT
            )
            layoutParams.setMargins(5, 5, 5, 5)
            btnLetra.layoutParams = layoutParams
            vista.addView(btnLetra)
            btnLetra.setOnClickListener {
                clickLetra(it as Button)
            }
        }
    }

    fun clickLetra(btnClicked: Button) {
        if (!finalizado) {
//obtener el indice de la letra seleccionada inicialmente
            var starIndex = 0
            var resIndex = respuesta.indexOf(btnClicked.text.toString())
//si el indice ya fue ocupado entonces no tomar en cuenta los indices hacia atras
            while (indicesOcupados.contains(resIndex)) {
                starIndex = resIndex + 1
                resIndex = respuesta.indexOf(btnClicked.text.toString(), starIndex)
            }
//si la respuesta contiene la letra seleccionada
            if (resIndex != -1) {
                val flexRow = flexResponse.get(resIndex) as EditText
                flexRow.setText(respuesta.get(resIndex).toString())
                indicesOcupados.add(resIndex)
                btnClicked.setBackgroundColor(Color.GREEN)
                btnClicked.isEnabled = false
                btnClicked.setTextColor(Color.WHITE)
            } else {
                Toast.makeText(
                    applicationContext, "No es una letra valida",
                    Toast.LENGTH_SHORT
                ).show()
                btnClicked.setBackgroundColor(Color.RED)
                btnClicked.isEnabled = false
                btnClicked.setTextColor(Color.WHITE)
            }
            intentosHechos++
            txtCantIntentos.text = "$intentosHechos/$intentosPermitidos"
            verificarResultado()
        }
    }

    fun verificarResultado() {
        if (intentosHechos == intentosPermitidos || indicesOcupados.size == respuesta.length) {
            finalizado = true
//si gano o perdió
            if (indicesOcupados.size == respuesta.length) {
                lottieResult.setAnimation(R.raw.winner)
                textMsjResultado.text = "Felicidades!"

            } else {
                lottieResult.setAnimation(R.raw.failed)
                textMsjResultado.text = "Perdiste :("
            }

            txtMsjRespuestaCorrecta.setText("La respuesta correcta es: $respuesta")
            // Mostrar el botón "Iniciar Nuevo Juego"
            btnNuevoJuego.visibility = View.VISIBLE
//despues de configurar la vista ponerlas como visibles
            textMsjResultado.visibility = View.VISIBLE
            lottieResult.visibility = View.VISIBLE
            txtMsjRespuestaCorrecta.visibility = View.VISIBLE
//ocultar los que no se deben mostrar
            flexResponse.visibility = View.GONE
            txtCantIntentos.visibility = View.GONE
            flexAlfabeto.visibility = View.GONE
            txtMsjIntentos.visibility = View.GONE
            txtPregunta.visibility = View.GONE
            lotieAnimThinking.visibility = View.GONE
        }
    }

    fun reiniciarJuego() {
        // Restablecer variables de juego
        respuesta = obtenerPalabraAleatoria().uppercase()
        intentosPermitidos = respuesta.length + 3
        intentosHechos = 0
        indicesOcupados.clear()
        finalizado = false

        // Ocultar la ventana de resultado
        textMsjResultado.visibility = View.GONE
        lottieResult.visibility = View.GONE
        txtMsjRespuestaCorrecta.visibility = View.GONE

        // Restablecer la visibilidad de las vistas del juego principal
        flexResponse.visibility = View.VISIBLE
        txtCantIntentos.visibility = View.VISIBLE
        flexAlfabeto.visibility = View.VISIBLE
        txtMsjIntentos.visibility = View.VISIBLE
        txtPregunta.visibility = View.VISIBLE
        lotieAnimThinking.visibility = View.VISIBLE

        // Restablecer los botones de letras
        for (i in 0 until flexAlfabeto.childCount) {
            val btnLetra = flexAlfabeto[i] as Button
            btnLetra.setBackgroundColor(Color.TRANSPARENT)
            btnLetra.isEnabled = true
            btnLetra.setTextColor(Color.BLACK)
        }

        // Restablecer los campos de respuesta
        flexResponse.removeAllViews()
        mostrarEspacioRespuesta(respuesta.length, flexResponse)

        // Actualizar el texto de los intentos
        txtCantIntentos.text = "$intentosHechos/$intentosPermitidos"
    }


}