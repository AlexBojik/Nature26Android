package ru.bizit.nature26.interfaces

import retrofit2.Call
import retrofit2.http.*
import ru.bizit.nature26.*


interface RetrofitServices {
    @GET("base_layers")
    fun getBaseLayers(): Call<MutableList<BaseLayer>>

    @GET("layers")
    fun getLayers(@HeaderMap headers: Map<String, String>): Call<MutableList<Layer>>

    @GET("user/{token}")
    fun getUser(@Path("token") token: String): Call<User>

    @GET("field_values/{id}")
    fun getFeatures(@Path("id") id: String): Call<MutableList<Feature>>

    @POST("filter")
    fun filter(@HeaderMap headers: Map<String, String>, @Body filter: Filter): Call<MutableList<GeoObject>>

    @POST("send")
    fun postMessage(@Body message: UserMessage): Call<MutableList<String>>

    @GET("news")
    fun getNews(): Call<MutableList<News>>

    @POST("check")
    fun postCheck(@Body check: Check): Call<MutableList<String>>
}

class Filter (
    var type: Int,
    var str: String
)

class Check(
    var lon: Double,
    var lat: Double
)