package com.sample.mqttapp

import android.content.Context
import android.util.Log
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.*
import java.io.UnsupportedEncodingException

object MqttHelper {

    private val TAG = MqttHelper::class.java.simpleName

    private const val serverUri = "tcp://tailor.cloudmqtt.com:12637" // it can be on the server or hiding in the client side
    private const val username = "mnzsrppm" // it can be on the server or hiding in the client side
    private const val password = "tVX6fpbhtX30" // it can be on the server or hiding in the client side

    private const val subscriptionTopic = "chat"

    fun getClient(context: Context?): MqttAndroidClient {
        val clientId = MqttClient.generateClientId()
        return MqttAndroidClient(context, serverUri, clientId)
    }

    fun connectToMqtt(client: MqttAndroidClient) {
        try {
            val mqttConnectOptions = getMqttConnectOptions()
            client.connect(mqttConnectOptions, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken) {
                    Log.i(TAG, "connected successfully")
                    val disconnectBufferOptions = getDisconnectBufferOptions()
                    client.setBufferOpts(disconnectBufferOptions)
                    subscribeToMqttChannel(client)
                }

                override fun onFailure(asyncActionToken: IMqttToken, exception: Throwable) {
                    // Something went wrong e.g. connection timeout or firewall problems
                    Log.e(TAG, "On Mqtt connect failure ${exception.message}")
                }
            })
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }

    private fun getMqttConnectOptions(): MqttConnectOptions {
        val mqttConnectOptions = MqttConnectOptions()
        mqttConnectOptions.apply {
            isAutomaticReconnect = true
            isCleanSession = false
            connectionTimeout = 60
            userName = username
            password = MqttHelper.password.toCharArray()
        }
        return mqttConnectOptions
    }

    private fun getDisconnectBufferOptions(): DisconnectedBufferOptions {
        val disconnectBufferOptions = DisconnectedBufferOptions()
        disconnectBufferOptions.apply {
            isBufferEnabled = true
            bufferSize = 100
            isPersistBuffer = false
            isDeleteOldestMessages = false
        }
        return disconnectBufferOptions
    }

    private fun subscribeToMqttChannel(client: MqttAndroidClient) {
        try {
            client.subscribe(subscriptionTopic, 0, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    Log.i(TAG, "On Mqtt subscribed successfully")
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    Log.e(TAG, "On Mqtt subscribe failed -> ${exception?.message}")
                }

            })
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }

    fun unSubscribeMqttChannel(client: MqttAndroidClient) {
        try {
            client.unsubscribe(subscriptionTopic, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken) {
                    // The subscription could successfully be removed from the client
                    Log.i(TAG, "On Mqtt unSubscribed")
                }

                override fun onFailure(asyncActionToken: IMqttToken, exception: Throwable) {
                    // some error occurred, this is very unlikely as even if the client
                    // did not had a subscription to the topic the unsubscribe action
                    // will be successfully
                    Log.e(TAG, "On Mqtt unSubscribe failure ${exception.message}")
                }
            })
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }

    fun disconnectMqtt(client: MqttAndroidClient) {
        try {
            client.disconnect(null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken) {
                    // We are connected
                    Log.i(TAG,"On Mqtt disconnected")
                    client.close()
                }

                override fun onFailure(asyncActionToken: IMqttToken, exception: Throwable) {
                    // Something went wrong e.g. connection timeout or firewall problems
                    Log.e(TAG,"On Mqtt disconnect failure ${exception.message}")
                }
            })
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }

    @Throws(MqttException::class, UnsupportedEncodingException::class)
    fun publishMessage(client: MqttAndroidClient, msg: String) {
        val encodedPayload = msg.toByteArray(charset("UTF-8"))
        val message = MqttMessage(encodedPayload)
        message.isRetained = true
        message.qos = 0
        client.publish(subscriptionTopic, message)
    }
}