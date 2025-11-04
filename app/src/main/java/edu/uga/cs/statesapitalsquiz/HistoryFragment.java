package edu.uga.cs.statesapitalsquiz;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

/**
 * The HistoryFragment extends Fragment to query the database to display all of the user's
 * past quizzes ordered from most recent to oldest. It will include each quiz's date
 * & time of completion as well as score.
 */

public class HistoryFragment extends Fragment {

    private DBHelper dbHelper;
    private ArrayList<String> quizHistory;
    private ArrayAdapter<String> adapter;

    // empty constructor
    public HistoryFragment() {
    }

    /**
     * onCreateView inflates the layout for hte quiz history fragment.
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
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_history, container, false);
    }

    /**
     * onViewCreated initializes a new array list for all the past quizzes, an array adapter,
     * & database helper.
     * @param view The View returned by {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ListView historyListView = view.findViewById(R.id.historyListView);
        quizHistory = new ArrayList<>();

        adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_list_item_1,
                quizHistory
        );
        historyListView.setAdapter(adapter);

        dbHelper = new DBHelper(requireContext());
        new LoadHistoryTask().execute();
    }

    /**
     * LoadHistoryTask extends AsyncTask to asynchronously load the user's past quizzes
     * from the database.
     */
    private class LoadHistoryTask extends AsyncTask<Void, Void, ArrayList<String>> {

        /**
         * doInBackground executes the database query of retrieving all past quizzes
         * in the background.
         * @param voids The parameters of the task.
         *
         * @return list of formatted strings of quiz information
         */
        @Override
        protected ArrayList<String> doInBackground(Void... voids) {
            ArrayList<String> historyList = new ArrayList<>();
            SQLiteDatabase db = dbHelper.openDatabase();

            Cursor cursor = db.rawQuery(
                    "SELECT date_time, score FROM quiz ORDER BY id DESC",
                    null
            );

            if (cursor.moveToFirst()) {
                do {
                    String date = cursor.getString(0);
                    int score = cursor.getInt(1);
                    historyList.add("Date: " + date + "  |  Score: " + score + "/6");
                } while (cursor.moveToNext());
            }

            cursor.close();
            db.close();
            return historyList;
        }

        /**
         * onPostExecute clears the existing quiz hisotry list, adds the newly loaded
         * results, & notifies the adapter of changes. This method displays the
         * updated quiz history.
         * @param result The result of the operation computed by {@link #doInBackground}.
         *
         */
        @Override
        protected void onPostExecute(ArrayList<String> result) {
            quizHistory.clear();
            quizHistory.addAll(result);
            adapter.notifyDataSetChanged();
        }
    }
}