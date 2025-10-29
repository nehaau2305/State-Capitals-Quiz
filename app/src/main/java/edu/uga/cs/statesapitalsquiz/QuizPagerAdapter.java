package edu.uga.cs.statesapitalsquiz;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

/**
 * QuizPagerAdapter will be used to implement the horizontal swiping
 * layout for the quiz questions. FragmentStateAdapter is an abstract
 * class which requires QuizPagerAdapter to implement the abstract method
 * createFragment. The number of fragments needed must also be specified
 * by the getItemCount method.
 */
public class QuizPagerAdapter extends FragmentStateAdapter {

    public QuizPagerAdapter(FragmentManager fragmentManager, Lifecycle lifecycle) {
        super(fragmentManager, lifecycle);
    }

    @Override
    public Fragment createFragment(int position){
        return QuizFragment.newInstance(position);
    }
    
    @Override
    public int getItemCount() {
        return 6;
    }
}
