package com.example.minipokedex

import android.content.Intent
import android.os.Bundle
import android.util.Log
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

    // Holds the original, unfiltered list fetched from /pokemon?limit=LARGE_NUMBER
    private var completeApiPokemonList: List<PokemonListItem> = emptyList()
    // Holds the list currently being used as a base for search and generation filters
    // This will be completeApiPokemonList, or the list from a type filter
    private var currentBasePokemonList: List<PokemonListItem> = emptyList()

    private var allApiPokemonTypes: List<TypeInfo> = emptyList()
    private var selectedTypeNameFilter: String? = null
    private var selectedGeneration: Generation? = null

    // Define generations (can be moved to a companion object or a separate file if preferred)
    private val generations = listOf(
        Generation("Gen I", 1..151),
        Generation("Gen II", 152..251),
        Generation("Gen III", 252..386),
        Generation("Gen IV", 387..493),
        Generation("Gen V", 494..649),
        Generation("Gen VI", 650..721),
        Generation("Gen VII", 722..809),
        Generation("Gen VIII", 810..905), // As per Bulbapedia, up to Eternatus initially for Gen 8
        Generation("Gen IX", 906..1025)  // As per Bulbapedia, up to Pecharunt
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Standard window insets handling
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupRecyclerView()
        setupSearchView()
        setupFilterButtons()
        fetchInitialPokemonData() // Fetch all Pokémon to start
    }

    private fun setupRecyclerView() {
        pokemonAdapter = PokemonAdapter(emptyList()) { pokemon ->
            val intent = Intent(this, DetailActivity::class.java)
            // Pass data needed for DetailActivity
            intent.putExtra("POKEMON_NAME", pokemon.name) // For display before detail loads
            intent.putExtra("POKEMON_ID", pokemon.getPokemonId()) // To fetch details
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
                binding.searchView.clearFocus() // Hide keyboard
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
            binding.recyclerView.alpha = 0.5f // Indicate loading
            try {
                // Fetch a large list to allow for local filtering by generation
                // PokeAPI v2 /pokemon endpoint lists up to 1025 (Pecharunt as of latest check for Gen IX)
                // Total Pokémon might be higher, adjust limit as needed or implement paging for /pokemon
                val response = RetrofitInstance.api.getPokemonList(limit = 1025) 
                if (response.isSuccessful && response.body() != null) {
                    completeApiPokemonList = response.body()!!.results
                    currentBasePokemonList = completeApiPokemonList // Initially, base list is the complete list
                    applyAllFilters() // Apply any default or persisted filters
                } else {
                    Log.e("MainActivity", "Error fetching initial Pokemon list: ${response.code()} ${response.message()}")
                    Toast.makeText(this@MainActivity, "Error fetching Pokémon list", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "Exception fetching initial Pokemon list", e)
                Toast.makeText(this@MainActivity, "Exception: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                binding.recyclerView.alpha = 1.0f
            }
        }
    }

    private fun showTypeSelectionDialog() {
        if (allApiPokemonTypes.isEmpty()) {
            lifecycleScope.launch {
                try {
                    val response = RetrofitInstance.api.getPokemonTypes()
                    if (response.isSuccessful && response.body() != null) {
                        allApiPokemonTypes = response.body()!!.results.filterNot { it.name == "unknown" || it.name == "shadow" } // Filter out unusual types
                        displayTypeDialogInternal()
                    } else {
                        Toast.makeText(this@MainActivity, "Failed to load Pokémon types", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this@MainActivity, "Error loading types: ${e.message}", Toast.LENGTH_SHORT).show()
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
                if (which == 0) { // ALL TYPES (Clear Filter)
                    selectedTypeNameFilter = null
                    binding.filterByTypeButton.text = "Filter by Type"
                    currentBasePokemonList = completeApiPokemonList // Reset base list to complete API list
                    applyAllFilters() // Re-apply other filters like generation and search
                } else {
                    val selectedType = allApiPokemonTypes[which - 1] // Adjust index for "ALL TYPES"
                    selectedTypeNameFilter = selectedType.name
                    binding.filterByTypeButton.text = "Type: ${selectedType.name.uppercase(Locale.getDefault())}" // Corrected here
                    // Fetch Pokémon for this type and set it as the base list
                    fetchPokemonsByTypeAndUpdateBaseList(selectedType.name)
                    // applyAllFilters() will be called within fetchPokemonsByTypeAndUpdateBaseList upon completion
                }
                dialog.dismiss()
            }
            .show()
    }

    private fun fetchPokemonsByTypeAndUpdateBaseList(typeName: String) {
        lifecycleScope.launch {
            binding.recyclerView.alpha = 0.5f
            try {
                val response = RetrofitInstance.api.getPokemonsByType(typeName.lowercase(Locale.getDefault()))
                if (response.isSuccessful && response.body() != null) {
                    currentBasePokemonList = response.body()!!.pokemonSlots.map { it.pokemon }
                } else {
                    Log.e("MainActivity", "Error fetching type details for '$typeName': ${response.code()} ${response.message()}")
                    Toast.makeText(this@MainActivity, "Failed to load Pokémon of type '$typeName'", Toast.LENGTH_SHORT).show()
                    currentBasePokemonList = emptyList() // Fallback to empty if API fails
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "Exception fetching type '$typeName'", e)
                Toast.makeText(this@MainActivity, "Error loading type '$typeName': ${e.message}", Toast.LENGTH_SHORT).show()
                currentBasePokemonList = emptyList()
            }
            applyAllFilters() // Re-apply all filters as the base list has changed
            binding.recyclerView.alpha = 1.0f
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
                applyAllFilters() // Re-apply all filters after generation selection changes
                dialog.dismiss()
            }
            .show()
    }

    private fun applyAllFilters(searchQuery: String? = binding.searchView.query.toString()) {
        var listToFilter = currentBasePokemonList

        // Apply Generation Filter if a generation is selected
        selectedGeneration?.let { gen ->
            listToFilter = listToFilter.filter { pokemonItem ->
                val pokemonId = pokemonItem.getPokemonId().toIntOrNull()
                // Only include Pokémon that have a valid ID and fall within the selected generation's range
                pokemonId != null && pokemonId in gen.idRange
            }
        }
        
        // Apply Search Query Filter if there's a search query
        if (!searchQuery.isNullOrBlank()) {
            listToFilter = listToFilter.filter {
                it.name.contains(searchQuery, ignoreCase = true)
            }
        }

        pokemonAdapter.updateData(listToFilter)

        if (listToFilter.isEmpty() && (selectedTypeNameFilter != null || selectedGeneration != null || !searchQuery.isNullOrBlank())){
            // Only show "No Pokémon found" if there were active filters or a search query
             Toast.makeText(this, "No Pokémon found with current filters.", Toast.LENGTH_SHORT).show()
        }
    }
}