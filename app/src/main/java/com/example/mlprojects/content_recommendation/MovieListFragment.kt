

package com.example.mlprojects.content_recommendation

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.example.mlprojects.content_recommendation.adapters.FilterType
import com.example.mlprojects.content_recommendation.adapters.ItemClickListener
import com.example.mlprojects.content_recommendation.adapters.MoviesAdapter
import com.example.mlprojects.content_recommendation.data.Movie
import com.example.mlprojects.content_recommendation.viewmodels.LikedMoviesViewModel
import com.example.mlprojects.databinding.FragmentMovieListBinding


/**
 * A fragment containing the list of available movies recognized in our model.
 */
class MovieListFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentMovieListBinding.inflate(inflater, container, false)
        context ?: return binding.root

        val viewModel: LikedMoviesViewModel =
            ViewModelProvider(requireActivity()).get(LikedMoviesViewModel::class.java)
        val movieClickListener = object : ItemClickListener() {
            override fun onLike(movie: Movie) {
                viewModel.onMovieLiked(movie)
            }

            override fun onRemoveLike(movie: Movie) {
                viewModel.onMovieLikeRemoved(movie)
            }
        }
        val adapter = MoviesAdapter(movieClickListener, FilterType.NONE)
        binding.list.adapter = adapter
        viewModel.movies.observe(viewLifecycleOwner) {
            adapter.submitList(it.toList())
            adapter.notifyDataSetChanged()
        }
        setHasOptionsMenu(true)
        return binding.root
    }
}