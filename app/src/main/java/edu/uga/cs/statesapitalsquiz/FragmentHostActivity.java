package edu.uga.cs.statesapitalsquiz;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

public class FragmentHostActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment_host);

        // get fragment type from MainActivity
        String fragmentType = getIntent().getStringExtra("fragmentType");
        // initialize fragment
        Fragment fragment = null;
        switch(fragmentType) {
            case "quiz":
                fragment = new QuizFragment();
                break;
            case "history":
                fragment = new HistoryFragment();
                break;
            case "help":
                fragment = new HelpFragment();
                break;
        }
        // update fragment container view
        if (fragment != null) {
            getSupportFragmentManager().beginTransaction().replace( R.id.fragmentContainerView, fragment).commit();
        }
    }
}