package edu.uga.cs.statesapitalsquiz;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;

/** QuizFragments initializes the quiz with a ViewPager to enable horizontal swiping
 * to the next question. The user will navigate through 6 questions with randomly picked
 * states & their cities before viewing their final score for the quiz. */
public class QuizFragment extends Fragment {

    // initialize variables
    private DBHelper dbHelper;
    private ViewPager2 viewPager;
    private int quizId;

    // default QuizFragment constructor
    public QuizFragment() { }

    /**
     * onCreateView inflates the quiz fragment layout
     * @param inflater The LayoutInflater object that can be used to inflate
     * any views in the fragment
     * @param container If non-null, this is the parent view that the fragment's
     * UI should be attached to.  The fragment should not add the view itself,
     * but this can be used to generate the LayoutParams of the view
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here
     *
     * @return the inflated quiz fragment view
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_quiz, container, false);
    }

    /**
     * onViewCreated initializes the quiz view pager and database helper.
     * @param view The View returned by {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewPager = view.findViewById(R.id.viewPager);
        dbHelper  = new DBHelper(requireContext());
        new LoadQuizTask().execute();
    }

    /* Background: pick states + create quiz row. Foreground: build pages. */
    private class LoadQuizTask extends AsyncTask<Void, Void, ArrayList<Bundle>> {

        @Override
        protected ArrayList<Bundle> doInBackground(Void... voids) {
            // ensure DB exists (just in case of first-run race)
            if (!dbHelper.checkDatabaseExists()) dbHelper.copyDatabaseFromAssets();

            SQLiteDatabase db = dbHelper.openDatabase();

            // 1) Read all states and thei three cities
            ArrayList<String[]> rows = new ArrayList<>();
            Cursor c = db.rawQuery(
                    "SELECT state, capital_city, second_city, third_city FROM states", null);
            while (c.moveToNext()) {
                rows.add(new String[] {
                        c.getString(0), // state
                        c.getString(1), // capital (correct)
                        c.getString(2), // second city
                        c.getString(3)  // third city
                });
            }
            c.close();

            // 2) Pick 6 random states
            Collections.shuffle(rows);
            ArrayList<String[]> chosen = new ArrayList<>(rows.subList(0, 6));

            // 3) Create quiz record and remember its id
            quizId = createQuizRecord(db);
            db.close();

            // 4) Prepare each question page
            ArrayList<Bundle> bundles = new ArrayList<>();
            for (String[] row : chosen) {
                String state   = row[0];
                String correct = row[1];

                ArrayList<String> options = new ArrayList<>();
                options.add(row[1]);
                options.add(row[2]);
                options.add(row[3]);
                Collections.shuffle(options);

                Bundle b = new Bundle();
                b.putString("state", state);
                b.putString("correct", correct);
                b.putStringArray("choices", options.toArray(new String[0]));
                b.putInt("quizId", quizId);
                bundles.add(b);
            }
            return bundles;
        }

        /**
         * onPostExecute initializes the quiz with all 6 questions and the results page.
         * @param bundles The result of the operation computed by {@link #doInBackground}.
         */
        @Override
        protected void onPostExecute(ArrayList<Bundle> bundles) {
            // Build fragments on the main thread using public static inner classes
            ArrayList<Fragment> pages = new ArrayList<>();
            for (Bundle args : bundles) {
                QuestionPageFragment f = QuestionPageFragment.newInstance(
                        args.getString("state"),
                        args.getString("correct"),
                        args.getStringArray("choices"),
                        args.getInt("quizId")
                );
                pages.add(f);
            }

            // Append a final results page (page 7)
            ResultsPageFragment results = ResultsPageFragment.newInstance(quizId);
            pages.add(results);

            QuizPagerAdapter adapter = new QuizPagerAdapter(
                    requireActivity().getSupportFragmentManager(),
                    getLifecycle(),
                    pages
            );
            viewPager.setAdapter(adapter);
        }
    }

    /* Insert a quiz row and return its id. */
    private int createQuizRecord(SQLiteDatabase db) {
        String ts = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                .format(new Date());
        db.execSQL("INSERT INTO quiz (date_time, score) VALUES (?, ?)",
                new Object[]{ts, 0});
        Cursor c = db.rawQuery("SELECT last_insert_rowid()", null);
        int id = 0;
        if (c.moveToFirst()) id = c.getInt(0);
        c.close();
        return id;
    }

    /* ===================== Question page ===================== */

    /**
     * QuestionPageFragment extends Fragment to represent a single question page in the quiz.
     */
    public static class QuestionPageFragment extends Fragment {

        private String stateName, correctCapital;
        private String[] choices;
        private int quizId;
        private DBHelper dbHelper;

        // default empty constructor
        public QuestionPageFragment() { }

        /**
         * QuestionPageFragment is a factory method to create a new instance of QuestionPageFragment.
         * @param state randomly selected state
         * @param correct capital city of given state
         * @param options other cities for given state
         * @param quizId the id of the current quiz
         * @return a new instance of QuestionPageFragment with the provided arguments
         */
        public static QuestionPageFragment newInstance(String state,
                                                       String correct,
                                                       String[] options,
                                                       int quizId) {
            QuestionPageFragment f = new QuestionPageFragment();
            Bundle b = new Bundle();
            b.putString("state", state);
            b.putString("correct", correct);
            b.putStringArray("choices", options);
            b.putInt("quizId", quizId);
            f.setArguments(b);
            return f;
        }

        /**
         * onCreate initializes the fragment using the saved instance state & initializes
         * the database helper to save the quiz results.
         * @param savedInstanceState If the fragment is being re-created from
         * a previous saved state, this is the state.
         */
        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            Bundle a = getArguments();
            if (a != null) {
                stateName      = a.getString("state");
                correctCapital = a.getString("correct");
                choices        = a.getStringArray("choices");
                quizId         = a.getInt("quizId");
            }
            dbHelper = new DBHelper(requireContext());
        }

        /**
         * onCreateView inflates the question fragment.
         * @param inflater The LayoutInflater object that can be used to inflate
         * any views in the fragment.
         * @param container If non-null, this is the parent view that the fragment's
         * UI should be attached to.  The fragment should not add the view itself,
         * but this can be used to generate the LayoutParams of the view.
         * @param savedInstanceState If non-null, this fragment is being re-constructed
         * from a previous saved state as given here.
         *
         * @return the inflated fragment layout
         */
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater,
                                 ViewGroup container,
                                 Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_question, container, false);
        }

        /**
         * onViewCreated sets up the UI elements to handle answer selection & immediately updates
         * the database after the user selects their response.
         * @param view The View returned by {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}.
         * @param savedInstanceState If non-null, this fragment is being re-constructed
         * from a previous saved state as given here.
         */
        @Override
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
            TextView stateText  = view.findViewById(R.id.textView5);
            RadioGroup group    = view.findViewById(R.id.radioGroup);
            RadioButton rb1     = view.findViewById(R.id.radioButton);
            RadioButton rb2     = view.findViewById(R.id.radioButton2);
            RadioButton rb3     = view.findViewById(R.id.radioButton3);

            stateText.setText(stateName);
            rb1.setText(choices[0]);
            rb2.setText(choices[1]);
            rb3.setText(choices[2]);

            group.setOnCheckedChangeListener((g, id) -> {
                RadioButton sel = view.findViewById(id);
                if (sel != null) {
                    int correct = sel.getText().toString().equals(correctCapital) ? 1 : 0;
                    SQLiteDatabase db = dbHelper.openDatabase();
                    android.content.ContentValues vals = new android.content.ContentValues();
                    vals.put("quiz_id", quizId);
                    vals.put("state", stateName);
                    vals.put("correct", correct);
                    db.insert("question", null, vals);
                    db.close();
                    group.setOnCheckedChangeListener(null); // save once
                }
            });
        }
    }

    /* ===================== Results page ===================== */

    /**
     * ResultsPageFragment extends Fragment to display the user's final score from
     * the quiz immediately after hte user answers the final question of the quiz.
     */
    public static class ResultsPageFragment extends Fragment {

        private static final int TOTAL_QUESTIONS = 6;

        private int quizId;
        private DBHelper dbHelper;

        public ResultsPageFragment() { }

        /**
         * ResultsPageFragment is a factory method to create a new instance of the ResultsPageFragment
         * @param quizId the unique id, the primary key, of the quiz
         * @return the new instance of the results page fragment
         */
        public static ResultsPageFragment newInstance(int quizId) {
            ResultsPageFragment f = new ResultsPageFragment();
            Bundle b = new Bundle();
            b.putInt("quizId", quizId);
            f.setArguments(b);
            return f;
        }

        /**
         * onCreate initializes the fragment by retrieving the quiz id & creating the
         * database helper.
         * @param savedInstanceState If the fragment is being re-created from
         * a previous saved state, this is the state.
         */
        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            Bundle a = getArguments();
            if (a != null) quizId = a.getInt("quizId");
            dbHelper = new DBHelper(requireContext());
        }

        /**
         * onCreateView inflates the results page layout.
         * @param inflater The LayoutInflater object that can be used to inflate
         * any views in the fragment,
         * @param container If non-null, this is the parent view that the fragment's
         * UI should be attached to.  The fragment should not add the view itself,
         * but this can be used to generate the LayoutParams of the view.
         * @param savedInstanceState If non-null, this fragment is being re-constructed
         * from a previous saved state as given here.
         *
         * @return the inflated view for the results page layout
         */
        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater,
                                 @Nullable ViewGroup container,
                                 @Nullable Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_results, container, false);
        }

        /**
         * onViewCreated populates the results screen with the user's quiz score & updates the database.
         * From here, the user can navigate to the home or splash page, or the quiz history page.
         * @param view The View returned by {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}.
         * @param savedInstanceState If non-null, this fragment is being re-constructed
         * from a previous saved state as given here.
         */
        @Override
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
            // compute score
            SQLiteDatabase db = dbHelper.openDatabase();
            int correctCount = 0;
            Cursor c = db.rawQuery(
                    "SELECT COUNT(*) FROM question WHERE quiz_id=? AND correct=1",
                    new String[]{ String.valueOf(quizId) }
            );
            if (c.moveToFirst()) correctCount = c.getInt(0);
            c.close();

            // update quiz table with score
            db.execSQL("UPDATE quiz SET score=? WHERE id=?",
                    new Object[]{ correctCount, quizId });
            db.close();

            // update UI
            TextView scoreText = view.findViewById(R.id.scoreText);
            scoreText.setText("Score: " + correctCount + " / " + TOTAL_QUESTIONS);

            // buttons
            View backHome   = view.findViewById(R.id.backHomeBtn);
            View viewHistory= view.findViewById(R.id.viewHistoryBtn);

            backHome.setOnClickListener(v -> {
                // Go back to MainActivity
                Intent i = new Intent(requireContext(), MainActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
                requireActivity().finish();
            });

            viewHistory.setOnClickListener(v -> {
                // Open History via FragmentHostActivity
                Intent i = new Intent(requireContext(), FragmentHostActivity.class);
                i.putExtra("fragmentType", "history");
                startActivity(i);
                requireActivity().finish();
            });
        }
    }
}
