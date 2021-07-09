package au.edu.swin.sdmd.l10_hardcell

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import java.util.concurrent.Executors
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

// These can be edited for your machine/device
const val HEIGHT_SHORT = 300
const val WIDTH_SHORT = 200
const val HEIGHT_LONG = 3000
const val WIDTH_LONG = 2000

val BACKGROUND = Executors.newFixedThreadPool(2)

class MainActivity : AppCompatActivity(), CoroutineScope {
    private var width: Int = WIDTH_SHORT
    private var height: Int = HEIGHT_SHORT

    lateinit var looperThread: LooperThread<ImageView>
    lateinit var progressBar: ProgressBar

    // setting up for coroutines
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    private lateinit var job: Job

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        job = Job() // coroutine setup

        progressBar = findViewById<ProgressBar>(R.id.progressBar)

        // handler setup and callback
        val responseHandler = Handler()
        looperThread = LooperThread(responseHandler) { image, ca ->
            image.setImageBitmap(ca)
            progressBar.visibility = View.INVISIBLE
        }
        lifecycle.addObserver(looperThread)

        // on UI thread
        val onUI = findViewById<Button>(R.id.onUI)
        onUI.setOnClickListener {
            drawCA(::drawCASync, it)
        }

        // on looper/handler
        val button = findViewById<Button>(R.id.looper)
        button.setOnClickListener {
            drawCA(::drawCALooper, it)
        }

        // using coroutines
        val suspendButton = findViewById<Button>(R.id.suspend)
        suspendButton.setOnClickListener {
            val etRule = findViewById<EditText>(R.id.etRule)
            val input = etRule.text.toString()
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(it.windowToken, 0)

            if (input.isNotEmpty() && input.toInt() in 0..255) {
                launch {
                    drawSuspendCA(input.toInt())
                }
            } else {
                etRule.error = "Number must be 0-255"
            }

        }

        // changing size of image to change drawing time
        val switchSize = findViewById<Switch>(R.id.largeSwitch)
        switchSize.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                height = HEIGHT_LONG
                width = WIDTH_LONG
            } else {
                height = HEIGHT_SHORT
                width = WIDTH_SHORT
            }
        }


    }

    // general function for setting up to draw
    private fun drawCA(drawFn: ((Int) -> Unit), button: View) {
        val etRule = findViewById<EditText>(R.id.etRule)
        val input = etRule.text.toString()
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(button.windowToken, 0)

        if (input.isNotEmpty() && input.toInt() in 0..255) {
            drawFn(input.toInt())
        } else {
            etRule.error = "Number must be 0-255"
        }
    }

    // using the handler
    private fun drawCALooper(rule: Int) {
        val imageView = findViewById<ImageView>(R.id.imageView)
        imageView.setImageBitmap(null)

        progressBar.visibility = View.VISIBLE

        val ca = ElemCA(width, height)
        ca.setNumber(rule)

        looperThread.queueCA(imageView, ca)

    }

    // on the main thread
    private fun drawCASync(rule: Int) {
        val imageView = findViewById<ImageView>(R.id.imageView)
        imageView.setImageBitmap(null)
        progressBar.visibility = View.VISIBLE

        val ca = ElemCA(width, height)
        ca.setNumber(rule)
        ca.drawCA()
        imageView.setImageBitmap(ca.getCA())
        progressBar.visibility = View.INVISIBLE
    }

    // using coroutines
    suspend fun drawSuspendCA(rule: Int) {
        val imageView = findViewById<ImageView>(R.id.imageView)
        imageView.setImageBitmap(null)
        progressBar.visibility = View.VISIBLE

        val ca = ElemCA(width, height)
        ca.setNumber(rule)

        // one approach -- note the icon in the border
        val bm = withContext(Dispatchers.Default) {
            ca.processCA()
        }
        imageView.setImageBitmap(bm)

        // An alternative approach, for those who like async/await
        // val bm = async { ca.processCA() }
        // imageView.setImageBitmap(bm.await())

        progressBar.visibility = View.INVISIBLE

    }

    // cleaning up from both coroutines and handler
    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
        lifecycle.removeObserver(looperThread)
    }

}