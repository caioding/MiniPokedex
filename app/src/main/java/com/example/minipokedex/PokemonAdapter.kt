package com.example.minipokedex

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.minipokedex.databinding.PokemonListItemBinding // Using ViewBinding
import java.util.Locale

class PokemonAdapter(
    private var pokemonList: List<PokemonListItem>,
    private val onItemClicked: (PokemonListItem) -> Unit
) : RecyclerView.Adapter<PokemonAdapter.PokemonViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PokemonViewHolder {
        val binding = PokemonListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PokemonViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PokemonViewHolder, position: Int) {
        val currentPokemon = pokemonList[position]
        holder.bind(currentPokemon)
        holder.itemView.setOnClickListener {
            onItemClicked(currentPokemon)
        }
    }

    override fun getItemCount(): Int = pokemonList.size

    @SuppressLint("NotifyDataSetChanged") // For simplicity. Consider DiffUtil for better performance.
    fun updateData(newPokemonList: List<PokemonListItem>) {
        pokemonList = newPokemonList
        notifyDataSetChanged()
    }

    inner class PokemonViewHolder(private val binding: PokemonListItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(pokemon: PokemonListItem) {
            binding.pokemonNameTextView.text = pokemon.name.replaceFirstChar { 
                if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() 
            }
            binding.pokemonImageView.load(pokemon.getImageUrl()) {
                crossfade(true)
                placeholder(R.mipmap.ic_launcher) // Generic placeholder
                error(R.drawable.ic_error_placeholder) // Custom error placeholder
            }
        }
    }
}