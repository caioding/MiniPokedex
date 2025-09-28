package com.example.minipokedex

import com.google.gson.annotations.SerializedName

// --- Data classes for Pokemon List API (/pokemon) ---
data class PokemonListResponse(
    val count: Int,
    val next: String?,
    val previous: String?,
    val results: List<PokemonListItem>
)

// Represents a Pokémon in a list (from /pokemon or /type/{name})
data class PokemonListItem(
    val name: String,
    @SerializedName("url") val detailUrl: String // URL to the Pokémon's own detail endpoint
) {
    // Helper to extract ID from the detailUrl, e.g. "https://pokeapi.co/api/v2/pokemon/1/" -> "1"
    fun getPokemonId(): String {
        return detailUrl.trimEnd('/').split("/").last()
    }

    // Helper to construct the official artwork image URL
    fun getImageUrl(): String {
        val id = getPokemonId()
        return "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/$id.png"
    }
}

// --- Data classes for Pokemon Detail API (/pokemon/{idOrName}) ---
data class PokemonDetailResponse(
    val id: Int,
    val name: String,
    val height: Int, // Height in decimetres
    val weight: Int, // Weight in hectograms
    val types: List<TypeSlot>,
    val stats: List<StatResponse>,
    val sprites: Sprites
)

data class TypeSlot(
    val slot: Int,
    val type: TypeInfo // Reusing TypeInfo defined below
)

data class StatResponse(
    @SerializedName("base_stat") val baseStat: Int,
    val effort: Int,
    val stat: StatInfo
)

data class StatInfo(
    val name: String,
    val url: String
)

data class Sprites(
    @SerializedName("front_default") val frontDefault: String?, // Basic sprite
    val other: OtherSprites?
)

data class OtherSprites(
    @SerializedName("official-artwork") val officialArtwork: OfficialArtwork?
)

data class OfficialArtwork(
    @SerializedName("front_default") val frontDefault: String? // Preferred high-quality sprite
)

// --- Data classes for Pokemon Types API ---
// For /api/v2/type (list of all types)
data class TypeListResponse(
    val count: Int,
    val next: String?,
    val previous: String?,
    val results: List<TypeInfo> // Each item here is a TypeInfo
)

// Represents a type, used in PokemonDetailResponse and TypeListResponse
data class TypeInfo(
    val name: String,
    val url: String // URL to the type's own detail endpoint
)

// For /api/v2/type/{idOrName} (details of a specific type)
data class TypeDetailResponse(
    val id: Int,
    val name: String,
    @SerializedName("pokemon") val pokemonSlots: List<PokemonOfTypeSlot> // List of Pokémon belonging to this type
)

// Represents a Pokémon slot within a Type's detail response
data class PokemonOfTypeSlot(
    val slot: Int,
    val pokemon: PokemonListItem // This is the Pokemon itself, with name and detailUrl
)

// --- Data class for Generation Filter (Local model, not from API) ---
data class Generation(val name: String, val idRange: IntRange) // Used in MainActivity for filtering
