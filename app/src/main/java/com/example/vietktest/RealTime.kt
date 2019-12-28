package com.example.vietktest

import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken

data class RealTime(
    @SerializedName("H") @Expose var h: String = "",
    @SerializedName("M") @Expose var m: String = "",
    @SerializedName("A") @Expose var messenger: String = "") {

    companion object {
        fun convertStringToObject(jsonElement: JsonElement)
                = GsonBuilder().create().fromJson(jsonElement, object : TypeToken<RealTime>() {}.type) as RealTime
    }

    override fun toString(): String {
        val gson = GsonBuilder().create().toJson(this)
        return gson.toString()
    }

    fun checkUpdateRealTime(): Boolean {
        return m == "Delete" || m == "Update" || m == "UpdateSelf"
    }
}