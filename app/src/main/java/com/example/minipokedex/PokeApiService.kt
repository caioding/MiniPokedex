package com.example.minipokedex

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface PokeApiService {

    // Fetches a list of Pokémon
    @GET("pokemon")
    suspend fun getPokemonList(
        @Query("limit") limit: Int = 200, // Default limit, can be overridden
        @Query("offset") offset: Int = 0
    ): Response<PokemonListResponse>

    // Fetches details for a specific Pokémon by its ID or name
    @GET("pokemon/{idOrName}")
    suspend fun getPokemonDetail(
        @Path("idOrName") idOrName: String
    ): Response<PokemonDetailResponse>

    // Fetches a list of all Pokémon types
    @GET("type")
    suspend fun getPokemonTypes(
        @Query("limit") limit: Int = 50 // There are not that many types, 50 should be enough
    ): Response<TypeListResponse>

    // Fetches details for a specific Pokémon type (including Pokémon of that type)
    @GET("type/{idOrName}")
    suspend fun getPokemonsByType(
        @Path("idOrName") idOrName: String
    ): Response<TypeDetailResponse>
}