package ru.bizit.nature26.interfaces

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import ru.bizit.nature26.*


interface RetrofitServices {
    @GET("base_layers")
    fun getBaseLayers(): Call<MutableList<BaseLayer>>

    @GET("layers")
    fun getLayers(): Call<MutableList<Layer>>

    @GET("user/{token}")
    fun getUser(@Path("token") token: String): Call<User>

    @GET("field_values/{id}")
    fun getFeatures(@Path("id") id: String): Call<MutableList<Feature>>

    @POST("filter")
    fun filter(@Body filter: Filter): Call<MutableList<GeoObject>>

    @POST("send")
    fun postMessage(@Body message: UserMessage): Call<MutableList<String>>
}

class Filter (
    var type: Int,
    var str: String
)