package com.example.minipokedex

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.minipokedex.databinding.ActivityMainBinding
import kotlinx.coroutines.launch
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var pokemonAdapter: PokemonAdapter

    private var completeApiPokemonList: List<PokemonListItem> = emptyList()
    private var currentBasePokemonList: List<PokemonListItem> = emptyList()

    private var allApiPokemonTypes: List<TypeInfo> = emptyList()
    private var selectedTypeNameFilter: String? = null
    private var selectedGeneration: Generation? = null

    private val generations = listOf(
        Generation("Gen I", 1..151),
        Generation("Gen II", 152..251),
        Generation("Gen III", 252..386),
        Generation("Gen IV", 387..493),
        Generation("Gen V", 494..649),
        Generation("Gen VI", 650..721),
        Generation("Gen VII", 722..809),
        Generation("Gen VIII", 810..905),
        Generation("Gen IX", 906..1025)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupRecyclerView()
        setupSearchView()
        setupFilterButtons()
        fetchInitialPokemonData()
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

    private fun setupRecyclerView() {
        pokemonAdapter = PokemonAdapter(emptyList()) { pokemon ->
            val intent = Intent(this, DetailActivity::class.java)
            intent.putExtra("POKEMON_NAME", pokemon.name)
            intent.putExtra("POKEMON_ID", pokemon.getPokemonId())
            startActivity(intent)
        }
        binding.recyclerView.apply {
            adapter = pokemonAdapter
            layoutManager = LinearLayoutManager(this@MainActivity)
        }
    }

    private fun setupSearchView() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                applyAllFilters(query)
                binding.searchView.clearFocus()
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                applyAllFilters(newText)
                return true
            }
        })
    }

    private fun setupFilterButtons() {
        binding.filterByTypeButton.setOnClickListener {
            showTypeSelectionDialog()
        }
        binding.filterByGenerationButton.setOnClickListener {
            showGenerationSelectionDialog()
        }
    }

    private fun fetchInitialPokemonData() {
        lifecycleScope.launch {
            showLoading(true)
            try {
                val response = RetrofitInstance.api.getPokemonList(limit = 1025) 
                if (response.isSuccessful && response.body() != null) {
                    completeApiPokemonList = response.body()!!.results
                    currentBasePokemonList = completeApiPokemonList
                    applyAllFilters()
                } else {
                    Log.e("MainActivity", "Error fetching initial Pokemon list: ${response.code()} ${response.message()}")
                    Toast.makeText(this@MainActivity, "Error fetching Pokémon list", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "Exception fetching initial Pokemon list", e)
                Toast.makeText(this@MainActivity, "Exception: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                showLoading(false)
            }
        }
    }

    private fun showTypeSelectionDialog() {
        if (allApiPokemonTypes.isEmpty()) {
            lifecycleScope.launch {
                showLoading(true)
                try {
                    val response = RetrofitInstance.api.getPokemonTypes()
                    if (response.isSuccessful && response.body() != null) {
                        allApiPokemonTypes = response.body()!!.results.filterNot { it.name == "unknown" || it.name == "shadow" }
                        displayTypeDialogInternal()
                    } else {
                        Toast.makeText(this@MainActivity, "Failed to load Pokémon types", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this@MainActivity, "Error loading types: ${e.message}", Toast.LENGTH_SHORT).show()
                } finally {
                    showLoading(false)
                }
            }
        } else {
            displayTypeDialogInternal()
        }
    }

    private fun displayTypeDialogInternal() {
        val typeNames = mutableListOf("ALL TYPES (Clear Filter)")
        typeNames.addAll(allApiPokemonTypes.map { it.name.replaceFirstChar { char -> char.titlecase(Locale.getDefault()) } })

        AlertDialog.Builder(this)
            .setTitle("Filter by Type")
            .setItems(typeNames.toTypedArray()) { dialog, which ->
                if (which == 0) {
                    selectedTypeNameFilter = null
                    binding.filterByTypeButton.text = "Filter by Type"
                    currentBasePokemonList = completeApiPokemonList
                    applyAllFilters()
                } else {
                    val selectedType = allApiPokemonTypes[which - 1]
                    selectedTypeNameFilter = selectedType.name
                    binding.filterByTypeButton.text = "Type: ${selectedType.name.uppercase(Locale.getDefault())}"
                    fetchPokemonsByTypeAndUpdateBaseList(selectedType.name)
                }
                dialog.dismiss()
            }
            .show()
    }

    private fun fetchPokemonsByTypeAndUpdateBaseList(typeName: String) {
        lifecycleScope.launch {
            showLoading(true)
            try {
                val response = RetrofitInstance.api.getPokemonsByType(typeName.lowercase(Locale.getDefault()))
                if (response.isSuccessful && response.body() != null) {
                    currentBasePokemonList = response.body()!!.pokemonSlots.map { it.pokemon }
                } else {
                    Log.e("MainActivity", "Error fetching type details for '$typeName': ${response.code()} ${response.message()}")
                    Toast.makeText(this@MainActivity, "Failed to load Pokémon of type '$typeName'", Toast.LENGTH_SHORT).show()
                    currentBasePokemonList = emptyList()
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "Exception fetching type '$typeName'", e)
                Toast.makeText(this@MainActivity, "Error loading type '$typeName': ${e.message}", Toast.LENGTH_SHORT).show()
                currentBasePokemonList = emptyList()
            }
            applyAllFilters()
            showLoading(false)
        }
    }

    private fun showGenerationSelectionDialog() {
        val genNames = mutableListOf("ALL GENERATIONS (Clear Filter)")
        genNames.addAll(generations.map { it.name })

        AlertDialog.Builder(this)
            .setTitle("Filter by Generation")
            .setItems(genNames.toTypedArray()) { dialog, which ->
                if (which == 0) {
                    selectedGeneration = null
                    binding.filterByGenerationButton.text = "Filter by Gen"
                } else {
                    selectedGeneration = generations[which - 1]
                    binding.filterByGenerationButton.text = selectedGeneration!!.name
                }
                applyAllFilters()
                dialog.dismiss()
            }
            .show()
    }

    private fun applyAllFilters(searchQuery: String? = binding.searchView.query.toString()) {
        var listToFilter = currentBasePokemonList

        selectedGeneration?.let { gen ->
            listToFilter = listToFilter.filter { pokemonItem ->
                val pokemonId = pokemonItem.getPokemonId().toIntOrNull()
                pokemonId != null && pokemonId in gen.idRange
            }
        }
        
        if (!searchQuery.isNullOrBlank()) {
            listToFilter = listToFilter.filter {
                it.name.contains(searchQuery, ignoreCase = true)
            }
        }

        pokemonAdapter.updateData(listToFilter)

        if (listToFilter.isEmpty() && (selectedTypeNameFilter != null || selectedGeneration != null || !searchQuery.isNullOrBlank())){
             Toast.makeText(this, "No Pokémon found with current filters.", Toast.LENGTH_SHORT).show()
        }
    }
}