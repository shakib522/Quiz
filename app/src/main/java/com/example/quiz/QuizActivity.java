package com.example.quiz;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;

public class QuizActivity extends AppCompatActivity {

    public static final String EXTRA_SCORE = "extraScore";
    private static final long COUNTDOWN_IN_MILLIS = 31000;

    public static final String KEY_SCORE = "keyScore";
    public static final String KEY_QUESTION_COUNT = "keyQuestionCount";
    public static final String KEY_MILLIS_LEFT = "keyMillisLeft";
    public static final String KEY_ANSWERED = "keyAnswered";
    public static final String KEY_QUESTION_LIST = "keyQuestionList";

    private TextView questionTextView;
    private TextView scoreTextView;
    private TextView questionCountTextView;
    private TextView countDownTextView;
    private RadioGroup radioGroup;
    private RadioButton radioButton1, radioButton2, radioButton3;
    private Button confirmButton;
    private ColorStateList textColorDefaultRb;
    private ColorStateList textColorDefaultCd;

    private CountDownTimer countDownTimer;
    private long timeLeftInMillis;
    private ArrayList<Question> questionList;
    private int questionCounter;
    private int questionCountTotal;
    private Question currentQuestion;
    private int score;
    private boolean answered;
    private long backPressedTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);
        questionTextView = findViewById(R.id.questionTextViewId);
        scoreTextView = findViewById(R.id.scoreTextViewId);
        questionCountTextView = findViewById(R.id.questionCountTextViewId);
        TextView textViewCategory = findViewById(R.id.categoryTextViewId);
        TextView difficultyTextView = findViewById(R.id.difficultyTextViewId);
        countDownTextView = findViewById(R.id.countdownTextView);
        radioGroup = findViewById(R.id.radioGroupId);
        radioButton1 = findViewById(R.id.radioButton1Id);
        radioButton2 = findViewById(R.id.radioButton2Id);
        radioButton3 = findViewById(R.id.radioButton3Id);
        confirmButton = findViewById(R.id.confirmButtonId);

        textColorDefaultRb = radioButton1.getTextColors();
        textColorDefaultCd = countDownTextView.getTextColors();

        Intent intent=getIntent();
        int categoryId=intent.getIntExtra(MainActivity.EXTRA_CATEGORY_ID,0);
        String categoryName=intent.getStringExtra(MainActivity.EXTRA_CATEGORY_NAME);
        String difficulty=intent.getStringExtra(MainActivity.EXTRA_DIFFICULTY);
        String setLevel="Difficulty: "+difficulty;
        String setCategory="Category: "+categoryName;
        textViewCategory.setText(setCategory);
        difficultyTextView.setText(setLevel);

        if (savedInstanceState == null) {
            QuizDbHelper quizDbHelper = QuizDbHelper.getInstance(this);
            questionList = quizDbHelper.getQuestion(categoryId,difficulty);
            //questionList = quizDbHelper.getAllQuestion();
            questionCountTotal = questionList.size();
            Collections.shuffle(questionList);
            showNextQuestion();
        }else{
            questionList=savedInstanceState.getParcelableArrayList(KEY_QUESTION_LIST);
            questionCountTotal=questionList.size();
            questionCounter=savedInstanceState.getInt(KEY_QUESTION_COUNT);
            currentQuestion =questionList.get(questionCounter-1);
            score=savedInstanceState.getInt(KEY_SCORE);
            timeLeftInMillis=savedInstanceState.getLong(KEY_MILLIS_LEFT);
            answered=savedInstanceState.getBoolean(KEY_ANSWERED);

            if(!answered){
                startCountDown();
            }else{
                updateCountDownText();
                showSolution();
            }
        }
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!answered) {
                    if (radioButton1.isChecked() || radioButton2.isChecked() || radioButton3.isChecked()) {
                        checkAnswer();
                    } else {
                        Toast.makeText(QuizActivity.this, "Please select an answer", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    showNextQuestion();
                }
            }
        });
    }

    private void showNextQuestion() {
        radioButton1.setTextColor(textColorDefaultRb);
        radioButton2.setTextColor(textColorDefaultRb);
        radioButton3.setTextColor(textColorDefaultRb);
        radioGroup.clearCheck(); //for deselect all radioButton

        if (questionCounter < questionCountTotal) {
            currentQuestion = questionList.get(questionCounter);
            questionTextView.setText(currentQuestion.getQuestion());
            radioButton1.setText(currentQuestion.getOption1());
            radioButton2.setText(currentQuestion.getOption2());
            radioButton3.setText(currentQuestion.getOption3());

            questionCounter++;
            String s = "Question: " + questionCounter + "/" + questionCountTotal;
            questionCountTextView.setText(s);
            answered = false;
            confirmButton.setText(R.string.confirm);
            timeLeftInMillis = COUNTDOWN_IN_MILLIS;
            startCountDown();
        } else {
            finishQuiz();
        }
    }

    private void startCountDown() {
        countDownTimer = new CountDownTimer(timeLeftInMillis, 1000) {
            @Override
            public void onTick(long l) {
                timeLeftInMillis = l;
                updateCountDownText();
            }

            @Override
            public void onFinish() {
                timeLeftInMillis = 0;
                updateCountDownText();
                checkAnswer();
            }
        }.start();
    }

    private void updateCountDownText() {
        int minutes = (int) ((timeLeftInMillis / 1000) / 60);
        int seconds = (int) (timeLeftInMillis / 1000) % 60;
        String timeFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
        countDownTextView.setText(timeFormatted);
        if (timeLeftInMillis < 10000) {
            countDownTextView.setTextColor(Color.RED);
        } else {
            countDownTextView.setTextColor(textColorDefaultCd);
        }
    }

    private void finishQuiz() {
        Intent intent = new Intent();
        intent.putExtra(EXTRA_SCORE, score);
        setResult(RESULT_OK, intent);
        finish();
    }

    private void checkAnswer() {
        answered = true;
        countDownTimer.cancel();
        RadioButton rbSelected = findViewById(radioGroup.getCheckedRadioButtonId());
        int answerNr = radioGroup.indexOfChild(rbSelected) + 1;

        if (answerNr == currentQuestion.getAnswerNumber()) {
            score++;
            String s = "Score: " + score;
            scoreTextView.setText(s);
        }
        showSolution();
    }

    private void showSolution() {
        radioButton1.setTextColor(Color.RED);
        radioButton2.setTextColor(Color.RED);
        radioButton3.setTextColor(Color.RED);

        if (currentQuestion.getAnswerNumber() == 1) {
            radioButton1.setTextColor(Color.GREEN);
            questionTextView.setText(R.string.ans_1_correct);
        } else if (currentQuestion.getAnswerNumber() == 2) {
            radioButton2.setTextColor(Color.GREEN);
            questionTextView.setText(R.string.ans_2_correct);
        } else if (currentQuestion.getAnswerNumber() == 3) {
            radioButton3.setTextColor(Color.GREEN);
            questionTextView.setText(R.string.ans_3_correct);
        }
        if (questionCounter < questionCountTotal) {
            confirmButton.setText(R.string.next);
        } else {
            confirmButton.setText(R.string.finish);
        }
    }

    @Override
    public void onBackPressed() {
        if (backPressedTime + 2000 > System.currentTimeMillis()) {
            finishQuiz();
        } else {
            Toast.makeText(this, "Press back again to finish", Toast.LENGTH_SHORT).show();
        }
        backPressedTime = System.currentTimeMillis();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_SCORE, score);
        outState.putInt(KEY_QUESTION_COUNT, questionCounter);
        outState.putLong(KEY_MILLIS_LEFT, timeLeftInMillis);
        outState.putBoolean(KEY_ANSWERED, answered);
        outState.putParcelableArrayList(KEY_QUESTION_LIST, questionList);
    }
}