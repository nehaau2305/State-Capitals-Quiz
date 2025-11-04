package edu.uga.cs.statesapitalsquiz;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

/**
 * The FragmentHostActivity manages which fragment is displayed in the fragment
 * container depending on user input.
 */
public class FragmentHostActivity extends AppCompatActivity {

    /**
     * onCreate determines which fragment to display based on the button clicked on the
     * splash screen.
     * @param savedInstanceState If the activity is being re-initialized after
     *     previously being shut down then this Bundle contains the data it most
     *     recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     */
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