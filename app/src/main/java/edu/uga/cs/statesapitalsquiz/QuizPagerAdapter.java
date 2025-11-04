package edu.uga.cs.statesapitalsquiz;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.ArrayList;

/**
 *  QuizPagerAdapter extends FragmentStateAdapter to provide the appropriate
 *  fragment, or quiz question, depending on the given position.
 */
public class QuizPagerAdapter extends FragmentStateAdapter {

    // ArrayList of Fragments to represent each page or quiz question in the ViewPager
    private final ArrayList<Fragment> pages;

    /**
     * Default constructor for QuizPagerAdapter.
     * @param fm the FragmentManager to manage each fragment
     * @param lifecycle the LifeCycle for observing the lifecycle events of each fragment
     * @param pages the list of question fragments to be displayed in the ViewPager
     */
    public QuizPagerAdapter(@NonNull FragmentManager fm,
                            @NonNull Lifecycle lifecycle,
                            ArrayList<Fragment> pages) {
        super(fm, lifecycle);
        this.pages = pages;
    }

    /**
     * createFragment returns the appropriate fragment to be displayed at the given position.
     * @param position the index of the questions in the quiz
     * @return the fragment at the specified position index
     */
    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return pages.get(position);
    }

    /**
     * getItemCount returns the number of questions, or pages, needed for the quiz, or pager adapter.
     * @return the number of fragments to be included in the quiz, which is 6 questions
     */
    @Override
    public int getItemCount() {
        return pages.size();
    }
}
