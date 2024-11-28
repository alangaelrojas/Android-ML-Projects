

package com.example.mlprojects.content_recommendation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.mlprojects.R
import com.google.android.material.tabs.TabLayoutMediator
import com.example.mlprojects.content_recommendation.adapters.LIKED_MOVIES_PAGE_INDEX
import com.example.mlprojects.content_recommendation.adapters.MOVIE_LIST_PAGE_INDEX
import com.example.mlprojects.content_recommendation.adapters.FireFlixPagerAdapter
import com.example.mlprojects.content_recommendation.adapters.RECOMMENDED_MOVIES_PAGE_INDEX
import com.example.mlprojects.databinding.FragmentViewPagerBinding

/**
 * The main ViewPager of the app, showing the three app tabs: Movies, Liked, and Recommendations.
 */
class HomeViewPagerFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentViewPagerBinding.inflate(inflater, container, false)
        val tabLayout = binding.tabs
        val viewPager = binding.viewPager

        viewPager.adapter = FireFlixPagerAdapter(this)

        // Set the icon and text for each tab
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.setIcon(getTabIcon(position))
            tab.text = getTabTitle(position)
        }.attach()

        (activity as AppCompatActivity).setSupportActionBar(binding.toolbar)

        return binding.root
    }

    private fun getTabIcon(position: Int): Int {
        return when (position) {
            LIKED_MOVIES_PAGE_INDEX -> R.drawable.ic_baseline_favorite_24
            MOVIE_LIST_PAGE_INDEX -> R.drawable.ic_baseline_local_movies_24
            RECOMMENDED_MOVIES_PAGE_INDEX -> R.drawable.ic_baseline_whatshot_24
            else -> throw IndexOutOfBoundsException()
        }
    }

    private fun getTabTitle(position: Int): String? {
        return when (position) {
            LIKED_MOVIES_PAGE_INDEX -> getString(R.string.liked_movies_title)
            MOVIE_LIST_PAGE_INDEX -> getString(R.string.movie_list_title)
            RECOMMENDED_MOVIES_PAGE_INDEX -> getString(R.string.recommended_list_title)
            else -> null
        }
    }
}