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

/**
 * QuizFragments initializes the quiz with a ViewPager to enable horizontal swiping
 * to the next question. The user will navigate through 6 questions with randomly picked
 * states & their cities before viewing their final score for the quiz.
 */
public class QuizFragment extends Fragment {

    // variables
    private DBHelper dbHelper;
    private ViewPager2 viewPager;
    private int quizId;
    private LoadQuizTask loadTask; // keep a reference to cancel if destroyed

    public QuizFragment() { }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_quiz, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewPager = view.findViewById(R.id.viewPager);
        dbHelper = new DBHelper(requireContext());
        loadTask = new LoadQuizTask();
        loadTask.execute();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // cancel async task if still running to avoid post-execute crash
        if (loadTask != null && !loadTask.isCancelled()) {
            loadTask.cancel(true);
        }
    }

    /* ===================== LoadQuizTask ===================== */
    private class LoadQuizTask extends AsyncTask<Void, Void, ArrayList<Bundle>> {

        @Override
        protected ArrayList<Bundle> doInBackground(Void... voids) {
            if (isCancelled()) return null;

            if (!dbHelper.checkDatabaseExists()) dbHelper.copyDatabaseFromAssets();
            SQLiteDatabase db = dbHelper.openDatabase();

            ArrayList<String[]> rows = new ArrayList<>();
            Cursor c = db.rawQuery(
                    "SELECT state, capital_city, second_city, third_city FROM states", null);
            while (c.moveToNext()) {
                rows.add(new String[]{
                        c.getString(0), // state
                        c.getString(1), // capital (correct)
                        c.getString(2), // second city
                        c.getString(3)  // third city
                });
            }
            c.close();

            Collections.shuffle(rows);
            ArrayList<String[]> chosen = new ArrayList<>(rows.subList(0, 6));

            quizId = createQuizRecord(db);
            db.close();

            ArrayList<Bundle> bundles = new ArrayList<>();
            for (String[] row : chosen) {
                if (isCancelled()) return null;

                String state = row[0];
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
         * Fixed onPostExecute to prevent crash on orientation change.
         */
        @Override
        protected void onPostExecute(ArrayList<Bundle> bundles) {
            if (bundles == null || isCancelled()) return;

            // 🚨 Prevent crash if fragment was destroyed during rotation
            if (!isAdded() || getActivity() == null || viewPager == null) {
                return;
            }

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

    /* ===================== Question Page ===================== */
    public static class QuestionPageFragment extends Fragment {
        private String stateName, correctCapital;
        private String[] choices;
        private int quizId;
        private DBHelper dbHelper;

        public QuestionPageFragment() { }

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

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            Bundle a = getArguments();
            if (a != null) {
                stateName = a.getString("state");
                correctCapital = a.getString("correct");
                choices = a.getStringArray("choices");
                quizId = a.getInt("quizId");
            }
            dbHelper = new DBHelper(requireContext());
        }

        @Override
        public View onCreateView(@NonNull LayoutInflater inflater,
                                 ViewGroup container,
                                 Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_question, container, false);
        }

        @Override
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
            TextView stateText = view.findViewById(R.id.textView5);
            RadioGroup group = view.findViewById(R.id.radioGroup);
            RadioButton rb1 = view.findViewById(R.id.radioButton);
            RadioButton rb2 = view.findViewById(R.id.radioButton2);
            RadioButton rb3 = view.findViewById(R.id.radioButton3);

            stateText.setText(stateName);
            rb1.setText("1. " + choices[0]);
            rb2.setText("2. " + choices[1]);
            rb3.setText("3. " + choices[2]);

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
                    group.setOnCheckedChangeListener(null);
                }
            });
        }
    }

    /* ===================== Results Page ===================== */
    public static class ResultsPageFragment extends Fragment {

        private static final int TOTAL_QUESTIONS = 6;
        private int quizId;
        private DBHelper dbHelper;

        public ResultsPageFragment() { }

        public static ResultsPageFragment newInstance(int quizId) {
            ResultsPageFragment f = new ResultsPageFragment();
            Bundle b = new Bundle();
            b.putInt("quizId", quizId);
            f.setArguments(b);
            return f;
        }

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            Bundle a = getArguments();
            if (a != null) quizId = a.getInt("quizId");
            dbHelper = new DBHelper(requireContext());
        }

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater,
                                 @Nullable ViewGroup container,
                                 @Nullable Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_results, container, false);
        }

        @Override
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
            SQLiteDatabase db = dbHelper.openDatabase();
            int correctCount = 0;
            Cursor c = db.rawQuery(
                    "SELECT COUNT(*) FROM question WHERE quiz_id=? AND correct=1",
                    new String[]{String.valueOf(quizId)}
            );
            if (c.moveToFirst()) correctCount = c.getInt(0);
            c.close();

            db.execSQL("UPDATE quiz SET score=? WHERE id=?",
                    new Object[]{correctCount, quizId});
            db.close();

            TextView scoreText = view.findViewById(R.id.scoreText);
            scoreText.setText("Score: " + correctCount + " / " + TOTAL_QUESTIONS);

            View backHome = view.findViewById(R.id.backHomeBtn);
            View viewHistory = view.findViewById(R.id.viewHistoryBtn);

            backHome.setOnClickListener(v -> {
                Intent i = new Intent(requireContext(), MainActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
                requireActivity().finish();
            });

            viewHistory.setOnClickListener(v -> {
                Intent i = new Intent(requireContext(), FragmentHostActivity.class);
                i.putExtra("fragmentType", "history");
                startActivity(i);
                requireActivity().finish();
            });
        }
    }
}
