package com.example.project

//import com.example.project.R
//import android.content.ContentValues.TAG
//import android.content.Context
//import android.view.View
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.IMqttActionListener
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.IMqttToken
import org.eclipse.paho.client.mqttv3.MqttCallback
//import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttException
import org.eclipse.paho.client.mqttv3.MqttMessage


class MainActivity : AppCompatActivity() {

    private val mqttBroker = "tcp://broker.emqx.io:1883"
    private val clientId = "Android_Client"
    private val mqttClient by lazy { createMqttClient() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

// Установка обработчиков нажатия на кнопки
        setButtonClickListeners()
    }
    override fun onStart() {
        super.onStart()
        connectMqtt()
    }

    override fun onStop() {
        super.onStop()
        disconnectMqtt()
    }

    private fun createMqttClient(): MqttAndroidClient {
        val mqttClient = MqttAndroidClient(applicationContext, mqttBroker, clientId)
        mqttClient.setCallback(object : MqttCallback {
            override fun connectionLost(cause: Throwable?) {
                Log.d(TAG, "Connection lost: ${cause.toString()}")
            }

            override fun messageArrived(topic: String?, message: MqttMessage?) {
                Log.d(TAG, "Received message: ${message.toString()} from topic: $topic")
            }

            override fun deliveryComplete(token: IMqttDeliveryToken?) {
// Not needed for publishing
            }
        })
        return mqttClient
    }

    private fun connectMqtt() {
        val options = MqttConnectOptions()
        try {
            mqttClient.connect(options, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    Log.d(TAG, "Connection success")
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    Log.d(TAG, "Connection failure: ${exception.toString()}")
                }
            })
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }

    private fun disconnectMqtt() {
        try {
            mqttClient.disconnect()
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }

    private fun setButtonClickListeners() {
        findViewById<Button>(R.id.buttonCalibrate).setOnClickListener {
            sendMessage("home/calibrate", "Calibrate sensors")
        }
        findViewById<Button>(R.id.buttonClose).setOnClickListener {
            sendMessage("home/close", "Close curtains")
        }
        findViewById<Button>(R.id.buttonOpen).setOnClickListener {
            sendMessage("home/open", "Open curtains")
        }
        findViewById<Button>(R.id.buttonControlIllumination).setOnClickListener {
            sendMessage("home/control_illumination", "Update illumination data")
        }
        findViewById<Button>(R.id.buttonControlTemperature).setOnClickListener {
            sendMessage("home/control_temperature", "Update temperature data")
        }
    }

    private fun sendMessage(topic: String, message: String) {
        if (mqttClient.isConnected) {
            try {
                val mqttMessage = MqttMessage(message.toByteArray())
                mqttClient.publish(topic, mqttMessage)
            } catch (e: MqttException) {
                e.printStackTrace()
            }
        } else {
            Log.d(TAG, "MQTT client is not connected")
        }
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}