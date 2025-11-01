package edu.uga.cs.statesapitalsquiz;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.ArrayList;

public class QuizPagerAdapter extends FragmentStateAdapter {

    private final ArrayList<Fragment> pages;

    public QuizPagerAdapter(@NonNull FragmentManager fm,
                            @NonNull Lifecycle lifecycle,
                            ArrayList<Fragment> pages) {
        super(fm, lifecycle);
        this.pages = pages;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return pages.get(position);
    }

    @Override
    public int getItemCount() {
        return pages.size();
    }
}
