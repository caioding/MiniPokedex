package com.example.minipokedex

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import coil.load
import com.example.minipokedex.databinding.ActivityDetailBinding
import kotlinx.coroutines.launch
import java.util.Locale

class DetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailBinding
    private var pokemonIdentifier: String? = null // Can be ID or name
    private var pokemonNameFromList: String? = null // Name passed from list for initial display

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup ActionBar with a back button and initial title
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        pokemonNameFromList = intent.getStringExtra("POKEMON_NAME")
        supportActionBar?.title = pokemonNameFromList?.replaceFirstChar { 
            if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() 
        } ?: "Pokemon Details"

        pokemonIdentifier = intent.getStringExtra("POKEMON_ID") // Primarily use ID if available
        if (pokemonIdentifier == null) {
            pokemonIdentifier = pokemonNameFromList // Fallback to name if ID is somehow not passed
        }

        if (pokemonIdentifier != null) {
            fetchPokemonDetails(pokemonIdentifier!!)
        } else {
            Toast.makeText(this, "Error: Pokémon identifier not found", Toast.LENGTH_LONG).show()
            Log.e("DetailActivity", "Pokemon ID/Name not passed in intent or both null.")
            finish() // Close activity if no identifier
        }
    }

    private fun fetchPokemonDetails(identifier: String) {
        binding.detailPokemonImageView.visibility = View.INVISIBLE // Hide until loaded
        lifecycleScope.launch {
            try {
                val response = RetrofitInstance.api.getPokemonDetail(identifier.lowercase(Locale.ROOT))
                if (response.isSuccessful && response.body() != null) {
                    populateUi(response.body()!!)
                    binding.detailPokemonImageView.visibility = View.VISIBLE
                } else {
                    Log.e("DetailActivity", "Error fetching details for \"$identifier\": \${response.code()} \${response.message()}")
                    Toast.makeText(this@DetailActivity, "Error fetching details for \"$identifier\"", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Log.e("DetailActivity", "Exception fetching details for \"$identifier\"", e)
                Toast.makeText(this@DetailActivity, "Exception: \${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun populateUi(pokemon: PokemonDetailResponse) {
        // Update ActionBar title with the exact name from API if different
        supportActionBar?.title = pokemon.name.replaceFirstChar { 
            if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() 
        }

        binding.detailPokemonNameTextView.text = "\${pokemon.name.uppercase(Locale.getDefault())} (#\${pokemon.id})"
        
        // Use official artwork if available, otherwise fallback to front_default sprite
        val imageUrl = pokemon.sprites.other?.officialArtwork?.frontDefault ?: pokemon.sprites.frontDefault
        binding.detailPokemonImageView.load(imageUrl) {
            crossfade(true)
            placeholder(R.mipmap.ic_launcher) // Generic placeholder
            error(R.drawable.ic_error_placeholder) // Custom error placeholder
        }

        binding.detailPokemonTypesTextView.text = pokemon.types.joinToString { 
            it.type.name.replaceFirstChar { char -> char.titlecase(Locale.getDefault()) } 
        }
        
        // Convert height (decimetres to metres) and weight (hectograms to kilograms)
        binding.detailPokemonHeightTextView.text = String.format(Locale.US, "%.1f m", pokemon.height / 10.0)
        binding.detailPokemonWeightTextView.text = String.format(Locale.US, "%.1f kg", pokemon.weight / 10.0)

        val statsToShow = listOf("hp", "attack", "defense", "special-attack", "special-defense", "speed")
        val statsText = pokemon.stats
            .filter { statsToShow.contains(it.stat.name.lowercase(Locale.ROOT)) }
            .joinToString("\n") {
                "\${it.stat.name.replaceFirstChar { char -> char.titlecase(Locale.getDefault()) }}: \${it.baseStat}"
            }
        binding.detailPokemonStatsTextView.text = statsText
    }

    // Handle the ActionBar back button press
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressedDispatcher.onBackPressed() // More modern way to handle back press
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}