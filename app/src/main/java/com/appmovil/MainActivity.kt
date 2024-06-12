package com.appmovil

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.toBitmap
import com.google.firebase.firestore.FirebaseFirestore
import java.io.ByteArrayOutputStream

class MainActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()

    private lateinit var nombres: EditText
    private lateinit var apellidos: EditText
    private lateinit var documento: Spinner
    private lateinit var numeroDocumento: EditText
    private lateinit var genero: RadioGroup
    private lateinit var celular: EditText
    private lateinit var correo: EditText
    private lateinit var direccion: EditText
    private lateinit var imagen: ImageView
    private lateinit var guardar: Button
    private lateinit var seleccionarImagen: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        nombres = findViewById(R.id.nombres)
        apellidos = findViewById(R.id.apellidos)
        documento = findViewById(R.id.documento)
        numeroDocumento = findViewById(R.id.numeroDocumento)
        genero = findViewById(R.id.genero)
        celular = findViewById(R.id.celular)
        correo = findViewById(R.id.correo)
        direccion = findViewById(R.id.direccion)
        imagen = findViewById(R.id.imagen)
        guardar = findViewById(R.id.guardar)
        seleccionarImagen = findViewById(R.id.seleccionarImagen)

        val tiposDocumento = arrayOf("Cédula de Ciudadanía", "Tarjeta de Identidad", "Registro Civíl")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, tiposDocumento)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        documento.adapter = adapter

        imagen.setOnClickListener {
            abrirSelectorImagen()
        }

        guardar.setOnClickListener {
            if (validarCampos()) {
                guardarDatosFirestore()
            } else {
                Toast.makeText(this, "Por favor complete todos los campos correctamente", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun validarCampos(): Boolean {
        val nombresText = nombres.text.toString().trim()
        val apellidosText = apellidos.text.toString().trim()
        val numeroDocumentoText = numeroDocumento.text.toString().trim()
        val celularText = celular.text.toString().trim()
        val correoText = correo.text.toString().trim()
        val direccionText = direccion.text.toString().trim()

        return nombresText.isNotEmpty() && apellidosText.isNotEmpty() && numeroDocumentoText.isNotEmpty() &&
                celularText.isNotEmpty() && correoText.isNotEmpty() && direccionText.isNotEmpty()
    }

    private fun guardarDatosFirestore() {
        val datos = hashMapOf(
            "nombres" to nombres.text.toString(),
            "apellidos" to apellidos.text.toString(),
            "documento" to documento.selectedItem.toString(),
            "numeroDocumento" to numeroDocumento.text.toString(),
            "genero" to findViewById<RadioButton>(genero.checkedRadioButtonId).text.toString(),
            "celular" to celular.text.toString(),
            "correo" to correo.text.toString(),
            "direccion" to direccion.text.toString()
        )

        val imagenBase64 = obtenerImagenBase64(imagen)
        datos["imagen"] = imagenBase64

        db.collection("usuarios")
            .add(datos)
            .addOnSuccessListener {
                Toast.makeText(this, "Datos guardados exitosamente", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al guardar datos: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun abrirSelectorImagen() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        startActivityForResult(intent, CODIGO_SELECCIONAR_IMAGEN)
    }

    private fun obtenerImagenBase64(imageView: ImageView): String {
        val bitmap = (imageView.drawable).toBitmap()
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        val byteArray = outputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }

    companion object {
        private const val CODIGO_SELECCIONAR_IMAGEN = 100
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CODIGO_SELECCIONAR_IMAGEN && resultCode == Activity.RESULT_OK) {
            val imagenSeleccionada = data?.data
            val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, imagenSeleccionada)
            val resizedImage = Bitmap.createScaledBitmap(bitmap, 200, 200, false)
            imagen.setImageBitmap(resizedImage)

            seleccionarImagen.visibility = View.GONE
        }
    }
}