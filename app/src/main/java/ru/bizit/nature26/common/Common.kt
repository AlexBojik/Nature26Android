package ru.bizit.nature26.common

import ru.bizit.nature26.interfaces.RetrofitServices
import ru.bizit.nature26.retrofit.RetrofitClient

object Common {
    private const val BASE_URL = "http://nature.mpr26.ru/api/"
//    private const val BASE_URL = "http://192.168.31.158:3000/"
//    private const val BASE_URL = "http://192.168.121.150:3000/"
    val retrofitService: RetrofitServices
        get() = RetrofitClient.getClient(BASE_URL).create(RetrofitServices::class.java)
}