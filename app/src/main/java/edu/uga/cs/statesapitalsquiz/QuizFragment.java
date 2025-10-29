package edu.uga.cs.statesapitalsquiz;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

/**
 * QuizFragment uses the DBHelper class to inflate the quiz fragment with the quiz.
 */
public class QuizFragment extends Fragment {
    public QuizFragment() {

    }

    public static QuizFragment newInstance( int questionNum ) {
        QuizFragment fragment = new QuizFragment();
        Bundle args = new Bundle();
        args.putInt( "questionNum", questionNum );
        fragment.setArguments( args );
        return fragment;
    }

}
