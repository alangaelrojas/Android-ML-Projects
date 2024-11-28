package com.example.mlprojects.animals

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.mlprojects.R
import com.example.mlprojects.databinding.AnimalsActivityBinding
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class AnimalsActivity : AppCompatActivity() {

    private lateinit var binding: AnimalsActivityBinding

    private val imgInput: ImageView get() = binding.imgInput
    private val txtPrediccion: TextView get() = binding.txtPrediccion
    private val btnElegir: Button get() = binding.btnElegir
    private val btnPredecir: Button get() = binding.btnPredecir

    private var tflite: Interpreter? = null
    private var options: Interpreter.Options = Interpreter.Options()
    private var imgData: ByteBuffer? = null
    private var imgPixels: IntArray = IntArray(IMG_SIZE * IMG_SIZE) //(32*32) 1024px

    private val labels = arrayOf("GATO", "PERRO", "PANDA")
    private val NUM_CLASSES = labels.size
    private var result: Array<FloatArray> = Array(1) { FloatArray(NUM_CLASSES) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = AnimalsActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Inicializar interpreter tensorflow-lite
        tflite = Interpreter(loadModelFile(), options)

        //https://bit.ly/2YgAqeG (explica los parámetros de ByteBuffer.allocateDirect(x,x,x,x) )
        imgData =
            ByteBuffer.allocateDirect(4 * BATCH_SIZE * IMG_SIZE * IMG_SIZE * PIXEL_SIZE) //Crea un ByteBuffer con una capacidad (4*1*32*32*3) = 12288 Bytes
        /*
         * ByteBuffer representa la imagen como una matriz 1D con 3 bytes por canal (rojo, verde y azul).
         * Llamamos order (ByteOrder.nativeOrder ()) para asegurarnos de que los bits se almacenan en el orden nativo del dispositivo.
         * */
        // Para imágenes del dataset MNIST:  imgData = ByteBuffer.allocateDirect(4*28*28);
        imgData?.order(ByteOrder.nativeOrder())

        // Elegir imagen
        btnElegir.setOnClickListener {
            chooseImageFromGallery()
        }
        //boton predecir
        btnPredecir.setOnClickListener {
            predict()
        }
    }

    /***
     * 1 - Cargar el modelo TFLite  **/
    @Throws(IOException::class)
    private fun loadModelFile(): MappedByteBuffer {
        val fileDescriptor = this.assets.openFd("animals.tflite")
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    /***
     * 2 - Seleccionar la imagen  **/
    private fun chooseImageFromGallery() {
        val intent = Intent()
        // Show only images, no videos or anything else
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(intent, IMAGE_PICK_CODE)
    }

    /***
     * 2 - Intentar predecir la imagen  **/
    private fun predict() {
        // Obtener la imagen de la vista de imagen y convertirla en un mapa de bits
        imgInput.invalidate()
        val drawable = imgInput.drawable as BitmapDrawable
        val bitmap = drawable.bitmap
        val bitmap_resize = getResizedBitmap(bitmap, IMG_SIZE, IMG_SIZE) //bitmap.. ancho, altura

        // Convertir el mapa de bits en un ByteBuffer
        convertBitmapToByteBuffer(bitmap_resize)

        // Realizar la inferencia
        tflite?.run(imgData, result)

        // Mostrar el resultado
        txtPrediccion.text = ""

        //argmax(result[0]) => 0, 1, 2
        txtPrediccion.text = """
                        ${labels[argmax(result[0])]}
                        Probs:${result[0].contentToString()}
                        """.trimIndent()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == IMAGE_PICK_CODE) {
            checkNotNull(data)
            imgInput!!.setImageURI(data.data)
            txtPrediccion.text = ""
        }
    }

    // runtime
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_CODE) {
            chooseImageFromGallery()
        }
    }

    private fun getResizedBitmap(bm: Bitmap, newWidth: Int, newHeight: Int): Bitmap {
        val width = bm.width
        val height = bm.height
        val scaleWidth = (newWidth.toFloat()) / width
        val scaleHeight = (newHeight.toFloat()) / height
        val matrix = Matrix()
        matrix.postScale(scaleWidth, scaleHeight)

        return Bitmap.createBitmap(bm, 0, 0, width, height, matrix, false)
    }

    /*
     * El código de convertBitmapToByteBuffer procesa previamente las imágenes de mapa de bits entrantes
     * de la cámara a este ByteBuffer. Llama al método addPixelValue/convertPixel para agregar cada conjunto de valores
     * de píxeles al ByteBuffer secuencialmente
     * */
    private fun convertBitmapToByteBuffer(bitmap: Bitmap) {
        if (imgData == null) {
            return
        }
        //rebobinar el búfer
        imgData?.rewind()

        bitmap.getPixels(imgPixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
        var pixel = 0
        for (i in 0 until IMG_SIZE) {
            for (j in 0 until IMG_SIZE) {
                val value = imgPixels[pixel++]
                addPixelValue(value)
            }
        }
    }


    //Original: usado en imágenes de dataset MNIST (blanco y negro)
    /*
        private static float convertPixel(int color) {
            return (255 - (((color >> 16) & 0xFF) * 0.299f
                    + ((color >> 8) & 0xFF) * 0.587f
                    + (color & 0xFF) * 0.114f)) / 255.0f;
        }
      */

    /*
     * Para ClassifierFloatMobileNet, debemos proporcionar un número de punto flotante para cada canal donde el
     * valor esté entre 0 y 1. Para hacer esto, enmascaramos cada canal de color como antes, pero luego dividimos
     * cada valor resultante entre 255.f.
     * En efecto el método "addPixelValue" normaliza los pixeles escalandalos en [0.0,1.0] y convierte la imagen
     * a punto flotante.
     * http://borg.csueastbay.edu/~grewe/CS663/Exercises/ExerciseA4Before.html
     * */
    private fun addPixelValue(pixelValue: Int) {
        imgData?.putFloat(((pixelValue shr 16) and 0xFF) / 255f) //Normaliza canal Rojo (R)
        imgData?.putFloat(((pixelValue shr 8) and 0xFF) / 255f) //Normaliza canal Verde (G)
        imgData?.putFloat((pixelValue and 0xFF) / 255f) //Normaliza canal Azul (B)
    }


    //Otros: Convertir pixeles a escala de grises:
    /*
    protected void addPixelValue(int pixelValue) {
        float mean = (((pixelValue >> 16) & 0xFF) + ((pixelValue >> 8) & 0xFF) +
                (pixelValue & 0xFF)) / 3.0f;
        imgData.putFloat(mean / 127.5f - 1.0f);
    }
    */

    /***
     * The argmax function finds the index of the maximum value in a given FloatArray.
     * */
    private fun argmax(probs: FloatArray): Int { //[0.76111, 0.50311, 0.30111
        Log.d("array", "=> " + probs.contentToString())
        var maxIds = -1
        var maxProb = 0.0f
        for (i in probs.indices) {
            if (probs[i] > maxProb) {
                maxProb = probs[i] //0.76111
                maxIds = i //0
            }
        }
        return maxIds
    }

    companion object {
        /*
            ByteBuffer: Es solo un contenedor o tanque de almacenamiento para leer o escribir datos en.
            Se le asignan datos utilizando la API allocateDirect()
        */
        private const val IMG_SIZE = 32
        private const val BATCH_SIZE = 1
        private const val PIXEL_SIZE = 3 //3 canales

        //constantes para permisos de la camara
        private const val IMAGE_PICK_CODE = 1000
        private const val PERMISSION_CODE = 1001

        fun startActivity(context: Context) {
            context.startActivity(Intent(context, AnimalsActivity::class.java))
        }
    }
}