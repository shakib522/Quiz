package com.example.quiz;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_CODE_QUIZ = 1;
    public static final String EXTRA_CATEGORY_ID = "extraCategoryID";
    public static final String EXTRA_CATEGORY_NAME = "extraCategoryName";
    public static final String EXTRA_DIFFICULTY = "extraDifficulty";

    public static final String SHARED_PREFS = "sharedPrefs";
    public static final String KEY_HIGHSCORE = "keyHighScore";
    TextView highScoreTextView;
    private Spinner spinnerDiffiCulty;
    private Spinner spinnerCategory;
    private int highScore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button startQuizButton = findViewById(R.id.startQuizButtonId);
        highScoreTextView = findViewById(R.id.highScoreTextViewId);
        spinnerDiffiCulty = findViewById(R.id.spinnerId);
        spinnerCategory = findViewById(R.id.spinnerCategoryId);


        loadCategories();
        loadDifficultyLevels();
        loadHighScore();
        startQuizButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startQuiz();
            }
        });

        QuizDbHelper.getInstance(this).addQuestion(new Question("Which of the following option leads to the portability and security of Java?","Bytecode is executed by JVM","The applet makes the Java code secure and portable","Use of exception handling",1,Question.DIFFICULTY_HARD,Category.JAVA));
        QuizDbHelper.getInstance(this).addQuestion(new Question("Which of the following is not a Java features?","Dynamic","Architecture Neutral","Use of pointers",3,Question.DIFFICULTY_EASY,Category.JAVA));
    }

    private void startQuiz() {

        Category selectedCategory=(Category)spinnerCategory.getSelectedItem();
        int categoryId=selectedCategory.getId();
        String categoryName=selectedCategory.getName();
        String difficulty = spinnerDiffiCulty.getSelectedItem().toString();
        Intent intent = new Intent(MainActivity.this, QuizActivity.class);
        intent.putExtra(EXTRA_CATEGORY_ID,categoryId);
        intent.putExtra(EXTRA_CATEGORY_NAME,categoryName);
        intent.putExtra(EXTRA_DIFFICULTY, difficulty);
        startActivityForResult(intent, REQUEST_CODE_QUIZ);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_QUIZ) {
            if (resultCode == RESULT_OK && data != null) {
                int score = data.getIntExtra(QuizActivity.EXTRA_SCORE, 0);
                if (score > highScore) {
                    updateHighScore(score);
                }
            }
        }
    }

    private void loadCategories() {
        QuizDbHelper dbHelper=QuizDbHelper.getInstance(this);
        List<Category> categories=dbHelper.getAllCategories();
        ArrayAdapter<Category> categoryArrayAdapter=new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,categories);
        categoryArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(categoryArrayAdapter);
        spinnerCategory.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                ((TextView) spinnerCategory.getSelectedView()).setTextSize(25);
            }
        });

    }

    private void loadDifficultyLevels() {
        String[] difficultyLevels = Question.getAllDifficultyLevels();
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, difficultyLevels);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDiffiCulty.setAdapter(spinnerAdapter);
        spinnerDiffiCulty.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                ((TextView) spinnerDiffiCulty.getSelectedView()).setTextSize(25);
            }
        });
    }

    private void loadHighScore() {
        SharedPreferences preferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        highScore = preferences.getInt(KEY_HIGHSCORE, 0);
        String s = "HighScore: " + highScore;
        highScoreTextView.setText(s);
    }

    private void updateHighScore(int score) {
        highScore = score;
        String s = "HighScore: " + highScore;
        highScoreTextView.setText(s);

        SharedPreferences preferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(KEY_HIGHSCORE, highScore);
        editor.apply();
    }
}