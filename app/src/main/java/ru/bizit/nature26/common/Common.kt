package ru.bizit.nature26.common

import ru.bizit.nature26.interfaces.RetrofitServices
import ru.bizit.nature26.retrofit.RetrofitClient

object Common {
    private const val BASE_URL = "https://nature.mpr26.ru/api/"
    val retrofitService: RetrofitServices
    get() = RetrofitClient.getClient(BASE_URL).create(RetrofitServices::class.java)
}