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
 * The HistoryFragment will query the database to display all of the user's
 * past quizzes.
 */

public class HistoryFragment extends Fragment {

    private DBHelper dbHelper;
    private ArrayList<String> quizHistory;
    private ArrayAdapter<String> adapter;

    public HistoryFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_history, container, false);
    }

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

    private class LoadHistoryTask extends AsyncTask<Void, Void, ArrayList<String>> {

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

        @Override
        protected void onPostExecute(ArrayList<String> result) {
            quizHistory.clear();
            quizHistory.addAll(result);
            adapter.notifyDataSetChanged();
        }
    }
}