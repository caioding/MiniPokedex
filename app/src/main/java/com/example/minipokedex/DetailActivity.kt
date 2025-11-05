package com.example.minipokedex

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import coil.load
import com.example.minipokedex.databinding.ActivityDetailBinding
import kotlinx.coroutines.launch
import java.util.Locale

class DetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailBinding
    private var pokemonIdentifier: String? = null
    private var pokemonNameFromList: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        pokemonNameFromList = intent.getStringExtra("POKEMON_NAME")
        supportActionBar?.title = pokemonNameFromList?.replaceFirstChar { 
            if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() 
        } ?: "Pokemon Details"

        pokemonIdentifier = intent.getStringExtra("POKEMON_ID")
        if (pokemonIdentifier == null) {
            pokemonIdentifier = pokemonNameFromList
        }

        if (pokemonIdentifier != null) {
            fetchPokemonDetails(pokemonIdentifier!!)
        } else {
            Toast.makeText(this, "Error: Pokémon identifier not found", Toast.LENGTH_LONG).show()
            Log.e("DetailActivity", "Pokemon ID/Name not passed in intent or both null.")
            finish()
        }
    }

    private fun showLoading(show: Boolean) {
        val loadingContainer = findViewById<View>(R.id.loadingContainer)
        val loadingImageView = findViewById<View>(R.id.loadingImageView)
        if (show) {
            loadingContainer.visibility = View.VISIBLE
            val rotateAnimation = AnimationUtils.loadAnimation(this, R.anim.rotate_animation)
            loadingImageView.startAnimation(rotateAnimation)
        } else {
            loadingContainer.visibility = View.GONE
            loadingImageView.clearAnimation()
        }
    }

    private fun fetchPokemonDetails(identifier: String) {
        lifecycleScope.launch {
            showLoading(true)
            try {
                val response = RetrofitInstance.api.getPokemonDetail(identifier.lowercase(Locale.ROOT))
                if (response.isSuccessful && response.body() != null) {
                    populateUi(response.body()!!)
                } else {
                    Log.e("DetailActivity", "Error fetching details for \"$identifier\": ${response.code()} ${response.message()}")
                    Toast.makeText(this@DetailActivity, "Error fetching details for \"$identifier\"", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Log.e("DetailActivity", "Exception fetching details for \"$identifier\"", e)
                Toast.makeText(this@DetailActivity, "Exception: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                showLoading(false)
            }
        }
    }

    private fun populateUi(pokemon: PokemonDetailResponse) {
        supportActionBar?.title = pokemon.name.replaceFirstChar { 
            if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() 
        }

        binding.detailPokemonNameTextView.text = "${pokemon.name.uppercase(Locale.getDefault())} (#${pokemon.id})"
        
        val imageUrl = pokemon.sprites.other?.officialArtwork?.frontDefault ?: pokemon.sprites.frontDefault
        binding.detailPokemonImageView.load(imageUrl) {
            crossfade(true)
            placeholder(R.mipmap.ic_launcher)
            error(R.drawable.ic_error_placeholder)
        }

        binding.detailPokemonTypesTextView.text = pokemon.types.joinToString { 
            it.type.name.replaceFirstChar { char -> char.titlecase(Locale.getDefault()) } 
        }
        
        // Converte altura e peso
        binding.detailPokemonHeightTextView.text = String.format(Locale.US, "%.1f m", pokemon.height / 10.0)
        binding.detailPokemonWeightTextView.text = String.format(Locale.US, "%.1f kg", pokemon.weight / 10.0)

        val statsToShow = listOf("hp", "attack", "defense", "special-attack", "special-defense", "speed")
        val statsText = pokemon.stats
            .filter { statsToShow.contains(it.stat.name.lowercase(Locale.ROOT)) }
            .joinToString("\n") {
                "${it.stat.name.replaceFirstChar { char -> char.titlecase(Locale.getDefault()) }}: ${it.baseStat}"
            }
        binding.detailPokemonStatsTextView.text = statsText
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressedDispatcher.onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}