package au.edu.swin.sdmd.l10_hardcell

import android.graphics.Bitmap
import android.os.Handler
import android.os.HandlerThread
import android.os.Message
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import java.util.concurrent.ConcurrentHashMap

private const val TAG = "CADrawer"

class LooperThread<in T>(private val responseHandler: Handler,
                         private val onCADrawn: (T, Bitmap) -> Unit)
    : HandlerThread(TAG), LifecycleObserver {

    private var hasQuit = false
    val CREATE = 0
    private lateinit var requestHandler: Handler
    private val requestMap = ConcurrentHashMap<T, ElemCA>()

    fun queueCA(target: T, ca: ElemCA) {
        Log.i(TAG, "Got a CA!: " + ca.code)
        requestMap[target] = ca
        requestHandler.obtainMessage(CREATE, target).sendToTarget()
    }

    override fun onLooperPrepared() {
        requestHandler = object : Handler() {
            override fun handleMessage(msg: Message) {
                if (msg.what == CREATE) {
                    val target = msg.obj as T
                    Log.i(TAG, "Got a request for code: " + requestMap[target])
                    handleRequest(target)
                }
            }
        }
    }


    private fun handleRequest(target: T) {
        val ca = requestMap[target] as ElemCA
        ca.drawCA()
        val bitmap = ca.getCA()
        Log.i(TAG, "Bitmap created")

        responseHandler.post(Runnable {
            if (requestMap[target] != ca || hasQuit) {
                return@Runnable
            }
            requestMap.remove(target)
            onCADrawn(target, bitmap)
        })
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun setup() {
        Log.i(TAG, "starting thread")
        start()
        looper
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun tearDown() {
        Log.i(TAG, "destroy thread")
        clearQueue()
        quit()
    }

    private fun clearQueue() {
        requestHandler.removeMessages(CREATE)
        requestMap.clear()
    }

    override fun quit(): Boolean {
        hasQuit = true
        return super.quit()
    }

}