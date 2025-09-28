package com.example.minipokedex

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {

    private const val BASE_URL = "https://pokeapi.co/api/v2/"

    val api: PokeApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create()) // Gson for JSON parsing
            .build()
            .create(PokeApiService::class.java)
    }
}