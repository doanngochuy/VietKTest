package com.example.vietktest


import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import microsoft.aspnet.signalr.client.Credentials
import microsoft.aspnet.signalr.client.Logger
import microsoft.aspnet.signalr.client.Platform
import microsoft.aspnet.signalr.client.http.android.AndroidPlatformComponent
import microsoft.aspnet.signalr.client.hubs.HubConnection
import microsoft.aspnet.signalr.client.hubs.HubProxy
import microsoft.aspnet.signalr.client.transport.ServerSentEventsTransport
import java.net.URLEncoder

class MainActivity : AppCompatActivity() {

    companion object {
        const val SSID = "o2Ih1kIxrbNaS5uCPpe3" //thay doi theo account
        const val RID = "LeTx/eEAOlM="         //thay doi theo account
        const val BID = "ZnoAkt+5g3Q="        //thay doi theo account

        const val LOG = "LOG"
        const val METHOD = "notify"
    }

    private var mHubConnection: HubConnection? = null
    private var mHubProxy: HubProxy? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        startSignalR {
            // nhan va xu ly tin nhan
            Toast.makeText(this, it, Toast.LENGTH_LONG).show()
        }

        btnSend.setOnClickListener {
            val content = edtContent.text.toString() // noi dung gui
            sendMessage(mHubProxy, METHOD, content )
        }

    }

    private fun startSignalR(onReceive: (String) -> Unit) : Boolean {
        var result : Boolean = false
        Log.e(LOG,"Start SignalR")
        try {
            stopSignalR()

            Platform.loadPlatformComponent(AndroidPlatformComponent())

            // Create a new console logger.
            val logger = Logger { message, level ->
                Log.e(LOG,"message = $message")
                Log.e(LOG,"level   = $level")
            }

            val credentials = Credentials {
                it.addHeader("User-Agent", BuildConfig.APPLICATION_ID)

                it.addHeader("Cookie", "ss-id=$SSID")
            }

            // Connect to the server.
            mHubConnection = HubConnection(
                "https://signalr.pos365.vn/signalr",
                signalRQueryString(RID, BID),
                true, logger)
            mHubConnection?.credentials = credentials
            mHubConnection?.error { Log.e(LOG,"ERROR - ${it.message}") } // Subscribe to the error event.
            mHubConnection?.connected {
                Log.e(LOG,"CONNECTED")
            } // Subscribe to the connected event.
            mHubConnection?.closed { Log.e(LOG,"DISCONNECTED") } // Subscribe to the closed event.
            mHubConnection?.received {
                Log.e(LOG,"RECEIVED")
                try {
                    val content = RealTime.convertStringToObject(it)
                    onReceive(content.messenger)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } // Subscribe to the received event.

            // Create the hub proxy.
            mHubProxy = mHubConnection?.createHubProxy("SaleHub")
            val clientTransport = ServerSentEventsTransport(mHubConnection?.logger)
            val signalRFuture = mHubConnection
                ?.start(clientTransport)
                ?.done {
                    Log.e(LOG,"DONE CONNECTING ...")
                } // Start the connection.

            signalRFuture?.get()
        } catch (e: Exception) {
            e.printStackTrace()
            result = false
        }
        return result
    }

    private fun stopSignalR() :Boolean{
        Log.e(LOG,"Stop SignalR")
        try {
            mHubConnection?.let {
                it.disconnect()
                it.stop()
            }
            mHubConnection = null
            return true

        } catch (e: Exception) {
            return false
        }
    }

    private fun signalRQueryString(rId: String, bId : String) = "rid=${URLEncoder.encode(rId, "UTF-8")}&bid=${URLEncoder.encode(bId, "UTF-8")}"

    private fun sendMessage(hubProxy: HubProxy?, method: String, message: String) {
        hubProxy?.let {
            it.invoke(method, message)
                .done {
                    Log.e(LOG,"Send DONE!")
                }
                .onError {
                    Log.e(LOG,"Send ERROR! ${ it.printStackTrace() }")
                }
        }
    }
}
