package com.example.native_mixpanel

import android.content.Context
import com.mixpanel.android.mpmetrics.MixpanelAPI
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.PluginRegistry.Registrar
import org.json.JSONObject
import android.app.Activity

class NativeMixpanelPlugin(private val activity: Activity?): MethodCallHandler {

  private var mixpanel: MixpanelAPI? = null

  companion object {
    var ctxt: Context? = null
    @JvmStatic
    fun registerWith(registrar: Registrar) {
      ctxt = registrar.context()
      val channel = MethodChannel(registrar.messenger(), "native_mixpanel")
      channel.setMethodCallHandler(NativeMixpanelPlugin(registrar.activity()))
    }
  }

  override fun onMethodCall(call: MethodCall, result: Result) {
    if (call.method == "initialize") {
      mixpanel = MixpanelAPI.getInstance(ctxt, call.arguments.toString())
      result.success("Init success..")
    } else if (call.method == "getDistinctId") {
      result.success(mixpanel?.distinctId)
    } else if(call.method == "identify") {
      val id = call.arguments.toString()
      mixpanel?.identify(id)
      mixpanel?.people?.identify(id)
      result.success("Identify success..")
    } else if(call.method == "alias") {
      mixpanel?.alias(call.arguments.toString(), mixpanel?.distinctId)
      result.success("Alias success..")
    } else if(call.method == "setPeopleProperties") {
      if (call.arguments == null) {
        result.error("Parse Error", "Arguments required for setPeopleProperties platform call", null)
      } else {
        val json = JSONObject(call.arguments.toString())
        mixpanel?.people?.set(json)
        result.success("Set People Properties success..")
      }
    } else if(call.method == "registerSuperProperties") {
      if (call.arguments == null) {
        result.error("Parse Error", "Arguments required for registerSuperProperties platform call", null)
      } else {
        val json = JSONObject(call.arguments.toString())
        mixpanel?.registerSuperProperties(json)
        result.success("Register Properties success..")
      }
    } else if (call.method == "reset") {
      mixpanel?.reset()
      result.success("Reset success..")
    } else if (call.method == "flush") {
      mixpanel?.flush()
      result.success("Flush success..")
    } else if (call.method == "in_app_message") {
      try {
        val inAppApplication = mixpanel?.people?.notificationIfAvailable
        inAppApplication?.let {
          mixpanel?.people?.showNotificationIfAvailable(activity)
        }
        result.success("Listening..")
      } catch (ex: Exception) {
        result.error("Error listening in app message", ex.toString(), null)
      }
    } else {
      if(call.arguments == null) {
        mixpanel?.track(call.method)
      } else {
        val json = JSONObject(call.arguments.toString())
        mixpanel?.track(call.method, json)
      }
      result.success("Track success..")
    }
  }
}
