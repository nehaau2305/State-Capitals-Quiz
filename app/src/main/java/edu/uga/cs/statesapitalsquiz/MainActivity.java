package edu.uga.cs.statesapitalsquiz;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

/**
 * MainActivity initializes the initial screen displayed to users. Users can navigate
 * to the next screen be selecting 1 of the 3 buttons displayed.
 */
public class MainActivity extends AppCompatActivity {

    // define variables
    Button startQuizB;
    Button viewHistoryB;
    Button helpB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // connect buttons to appropriate UI components
        startQuizB = findViewById(R.id.button);
        viewHistoryB = findViewById(R.id.button2);
        helpB = findViewById(R.id.button3);
        // button click listener
        ButtonClickListener listener = new ButtonClickListener();
        startQuizB.setOnClickListener(listener);
        viewHistoryB.setOnClickListener(listener);
        helpB.setOnClickListener(listener);
    } //onCreate

    private class ButtonClickListener implements View.OnClickListener {
        public void onClick(View v) {
            Intent intent = new Intent(MainActivity.this, FragmentHostActivity.class);

            // do not use switch case bc R.id.button is not a final constant, which is necessary for case
            if (v.getId() == R.id.button) {
                intent.putExtra("fragmentType", "quiz");
            } else if (v.getId() == R.id.button2) {
                intent.putExtra("fragmentType", "history");
            } else if (v.getId() == R.id.button3) {
                intent.putExtra("fragmentType", "help");
            }
            startActivity(intent);
        } //onClick
    } //ButtonClickListener
}