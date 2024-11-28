

package com.example.mlprojects.content_recommendation.adapters

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.mlprojects.content_recommendation.MovieListFragment
import com.example.mlprojects.content_recommendation.LikedMoviesFragment
import com.example.mlprojects.content_recommendation.RecommendationsFragment

const val MOVIE_LIST_PAGE_INDEX = 0
const val LIKED_MOVIES_PAGE_INDEX = 1
const val RECOMMENDED_MOVIES_PAGE_INDEX = 2

/**
 * Maintains a list of fragments used in the app's navigation pager.
 */
class FireFlixPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    /**
     * Mapping of the ViewPager page indexes to their respective Fragments
     */
    private val tabFragmentsCreators: Map<Int, () -> Fragment> = mapOf(
        MOVIE_LIST_PAGE_INDEX to { MovieListFragment() },
        LIKED_MOVIES_PAGE_INDEX to { LikedMoviesFragment() },
        RECOMMENDED_MOVIES_PAGE_INDEX to { RecommendationsFragment() }
    )

    override fun getItemCount() = tabFragmentsCreators.size

    override fun createFragment(position: Int): Fragment {
        return tabFragmentsCreators[position]?.invoke() ?: throw IndexOutOfBoundsException()
    }
}