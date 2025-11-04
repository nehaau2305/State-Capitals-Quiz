package edu.uga.cs.statesapitalsquiz;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 * MainActivity is the splash page from which the user can navigate to the help fragment,
 * start a quiz, or view their past quizzes. This class connects the buttons to their
 * appropriate UI components & ensures that the prebuilt database exists before the
 * user moves on to any new fragment.
 */
public class MainActivity extends AppCompatActivity {

    Button startQuizB, viewHistoryB, helpB;
    DBHelper dbHelper;

    /**
     * onCreate initializes the starting activity and attaches button listeners to the start quiz,
     * view quiz history, and help buttons.
     * @param savedInstanceState If the activity is being re-initialized after
     *     previously being shut down then this Bundle contains the data it most
     *     recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     *
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        startQuizB    = findViewById(R.id.button);
        viewHistoryB  = findViewById(R.id.button2);
        helpB         = findViewById(R.id.button3);

        // Ensure the prebuilt DB exists before user navigates
        dbHelper = new DBHelper(this);
        new CopyDbTask().execute();

        View.OnClickListener listener = v -> {
            Intent intent = new Intent(MainActivity.this, FragmentHostActivity.class);
            if (v.getId() == R.id.button)       intent.putExtra("fragmentType", "quiz");
            else if (v.getId() == R.id.button2) intent.putExtra("fragmentType", "history");
            else if (v.getId() == R.id.button3) intent.putExtra("fragmentType", "help");
            startActivity(intent);
        };
        startQuizB.setOnClickListener(listener);
        viewHistoryB.setOnClickListener(listener);
        helpB.setOnClickListener(listener);
    }

    /**
     * CopyDbTask extends AsyncTask to ensure the database exists. If it does not,
     * it copies the database from the project asynchronously.
     */
    private class CopyDbTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            if (!dbHelper.checkDatabaseExists()) {
                dbHelper.copyDatabaseFromAssets();
            }
            return null;
        }
    }
}
