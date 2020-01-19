package com.sample.mqttapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import kotlinx.android.synthetic.main.activity_main.*
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended
import org.eclipse.paho.client.mqttv3.MqttMessage
import java.lang.Exception

class MainActivity : AppCompatActivity(), MqttCallbackExtended, View.OnClickListener {

    companion object {
        private val TAG = MainActivity::class.java.simpleName
    }

    private lateinit var client: MqttAndroidClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setClickListener()

        setClientSetting()

        connectToMqtt()
    }

    private fun connectToMqtt() {
        MqttHelper.connectToMqtt(client)
    }

    private fun setClickListener() {
        button_publish.setOnClickListener(this)
    }

    private fun setClientSetting() {
        client = MqttHelper.getClient(this)
        client.setCallback(this)
    }

    override fun onPause() {
        client.unregisterResources()
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        client.registerResources(this)
    }

    override fun onDestroy() {
        MqttHelper.unSubscribeMqttChannel(client)
        MqttHelper.disconnectMqtt(client)
        super.onDestroy()
    }

    override fun connectComplete(reconnect: Boolean, serverURI: String?) {
        Log.i(TAG, "connectComplete")
    }

    override fun messageArrived(topic: String?, message: MqttMessage?) {
        runOnUiThread {
            Log.d(TAG, "topic -> $topic")
            Log.d(TAG, "Message received -> $message")
            edit_text_message.text.clear()
            text_view_message.text = message.toString()
        }
    }

    override fun connectionLost(cause: Throwable?) {
        Log.i(TAG, "connectionLost")
    }

    override fun deliveryComplete(token: IMqttDeliveryToken?) {
        Log.i(TAG, "deliveryComplete")
    }

    override fun onClick(view: View?) {
        when(view) {
            button_publish -> {
                if (edit_text_message.text.isNotEmpty()) {
                    try {
                        MqttHelper.publishMessage(client, edit_text_message.text.toString())
                    } catch (ex: Exception) {
                        Log.e(TAG, ex.message ?: "error occurred")
                    }
                }
            }
        }
    }
}
