package edu.uga.cs.statesapitalsquiz;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.text.HtmlCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * The HelpFragment is displayed when the user selects the question mark button
 * on the initial screen. This fragment loads a text describing the app's purpose
 * & how to navigate it.
 */
public class HelpFragment extends Fragment {

    // empty  default constructor
    public HelpFragment() {
    }

    /**
     * HelpFragment is a factory method to create a new instance of the help fragment.
     * @return new instance of the help fragment
     */
    public static HelpFragment newInstance() {
        HelpFragment fragment = new HelpFragment();
        return fragment;
    }

    /**
     * onCreate recreates the view based on the saved instance state bundle.
     * @param savedInstanceState If the fragment is being re-created from
     * a previous saved state, this is the state.
     */
    @Override
    public void onCreate( Bundle savedInstanceState ) {
        super.onCreate(savedInstanceState);
    }

    /**
     * onCreateView inflates the layout view for the help fragment.
     * @param inflater The LayoutInflater object that can be used to inflate
     * any views in the fragment,
     * @param container If non-null, this is the parent view that the fragment's
     * UI should be attached to.  The fragment should not add the view itself,
     * but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     *
     * @return
     */
    @Override
    public View onCreateView( LayoutInflater inflater, ViewGroup container,
                              Bundle savedInstanceState ) {
        // Inflate the layout for this fragment
        return inflater.inflate( R.layout.fragment_help, container, false );
    }

    /**
     * onViewCreated initializes the text to be displayed by the help fragment.
     * @param view The View returned by {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     */
    @Override
    public void onViewCreated( @NonNull View view, Bundle savedInstanceState ) {
        super.onViewCreated(view,savedInstanceState);

        TextView textView = getView().findViewById( R.id.helpTextView );
        String text = getResources().getString( R.string.help_fragment_text );
        textView.setText( HtmlCompat.fromHtml( text, HtmlCompat.FROM_HTML_MODE_LEGACY ) );
    }
}