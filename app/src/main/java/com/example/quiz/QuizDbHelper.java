package com.example.quiz;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.quiz.QuizContract.*;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class QuizDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "MyQuiz.db";
    private static final int DATABASE_VERSION = 2;

    private static QuizDbHelper instance;

    private SQLiteDatabase sqLiteDatabase;

    private QuizDbHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static synchronized QuizDbHelper getInstance(Context context){//synchronize is for access the method from multiple thread
        if(instance==null){
            instance=new QuizDbHelper(context.getApplicationContext());
        }
        return instance;
    }


    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        this.sqLiteDatabase = sqLiteDatabase;

        final String CREATE_CATEGORIES_TABLE = "CREATE TABLE " + CategoriesTable.TABLE_NAME + "( " +
                CategoriesTable._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                CategoriesTable.COLUMN_NAME + " TEXT " +
                ")";

        final String CREATE_TABLE = "CREATE TABLE " + QuestionsTable.TABLE_NAME + " ( " +
                QuestionsTable._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                QuestionsTable.COLUMN_QUESTION + " TEXT, " +
                QuestionsTable.COLUMN_OPTION1 + " TEXT, " +
                QuestionsTable.COLUMN_OPTION2 + " TEXT, " +
                QuestionsTable.COLUMN_OPTION3 + " TEXT, " +
                QuestionsTable.COLUMN_ANSWER_NR + " INTEGER, " +
                QuestionsTable.COLUMN_DIFFICULTY+" TEXT, "+
                QuestionsTable.COLUMN_CATEGORY_ID + " INTEGER, " +
                "FOREIGN KEY(" + QuestionsTable.COLUMN_CATEGORY_ID + ") REFERENCES " +
                CategoriesTable.TABLE_NAME + "(" + CategoriesTable._ID + ")" + "ON DELETE CASCADE" +
                ")";
        sqLiteDatabase.execSQL(CREATE_CATEGORIES_TABLE);
        sqLiteDatabase.execSQL(CREATE_TABLE);
        fillCategoriesTable();
       // fillQuestionsTable();
    }

    private void fillCategoriesTable() {
        Category c1 = new Category("JAVA");
        addCategory(c1);
        Category c2 = new Category("C");
        addCategory(c2);
        Category c3 = new Category("C++");
        addCategory(c3);
    }
    private void addCategory(Category category){
        ContentValues contentValues=new ContentValues();
        contentValues.put(CategoriesTable.COLUMN_NAME,category.getName());
        sqLiteDatabase.insert(CategoriesTable.TABLE_NAME,null,contentValues);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + CategoriesTable.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + QuestionsTable.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);//for invalid foreign key
    }

//    private void fillQuestionsTable() {
//        Question q1 = new Question("A is correct", "A", "B", "C", 1,Question.DIFFICULTY_EASY,Category.JAVA);
//        Question q2 = new Question("B is correct", "A", "B", "C", 2,Question.DIFFICULTY_MEDIUM,Category.C);
//        Question q3 = new Question("C is correct", "A", "B", "C", 3,Question.DIFFICULTY_MEDIUM,Category.C_Plus_Plus);
//        Question q4 = new Question("D is correct", "A", "D", "C", 2,Question.DIFFICULTY_HARD,Category.JAVA);
//        Question q5 = new Question("E is correct", "A", "E", "D", 2,Question.DIFFICULTY_EASY,Category.C);
//        Question q6 = new Question("F is correct", "A", "E", "F", 3,Question.DIFFICULTY_HARD,Category.JAVA);
//        insertQuestion(q1);
//        insertQuestion(q2);
//        insertQuestion(q3);
//        insertQuestion(q4);
//        insertQuestion(q5);
//        insertQuestion(q6);
//    }

    public void addQuestion(Question question){
        sqLiteDatabase=getWritableDatabase();
        insertQuestion(question);
    }

    public void addQuestions(List<Question>questions){
        sqLiteDatabase=getWritableDatabase();
        for(Question question:questions){
            insertQuestion(question);
        }
    }

    private void insertQuestion(Question question) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(QuestionsTable.COLUMN_QUESTION, question.getQuestion());
        contentValues.put(QuestionsTable.COLUMN_OPTION1, question.getOption1());
        contentValues.put(QuestionsTable.COLUMN_OPTION2, question.getOption2());
        contentValues.put(QuestionsTable.COLUMN_OPTION3, question.getOption3());
        contentValues.put(QuestionsTable.COLUMN_ANSWER_NR, question.getAnswerNumber());
        contentValues.put(QuestionsTable.COLUMN_DIFFICULTY, question.getDifficulty());
        contentValues.put(QuestionsTable.COLUMN_CATEGORY_ID,question.getCategoryId());
        sqLiteDatabase.insert(QuestionsTable.TABLE_NAME, null, contentValues);
    }

    @SuppressLint("Range")
    public List<Category> getAllCategories(){
        List<Category> categoryList=new ArrayList<>();
        sqLiteDatabase=getReadableDatabase();
        Cursor c=sqLiteDatabase.rawQuery("SELECT * FROM "+CategoriesTable.TABLE_NAME,null);
        if(c.moveToFirst()){
            do{
                Category category=new Category();
                category.setId(c.getInt(c.getColumnIndex(CategoriesTable._ID)));
                category.setName(c.getString(c.getColumnIndex(CategoriesTable.COLUMN_NAME)));
                categoryList.add(category);
            }while(c.moveToNext());
        }
        c.close();
        return categoryList;
    }

    @SuppressLint("Range")
    public ArrayList<Question> getAllQuestion() {
        ArrayList<Question> questionList = new ArrayList<>();
        sqLiteDatabase = getReadableDatabase();
        Cursor c = sqLiteDatabase.rawQuery("SELECT * FROM " + QuestionsTable.TABLE_NAME, null);
        if (c.moveToFirst()) {
            do {
                Question question = new Question();
                question.setId(c.getInt(c.getColumnIndex(QuestionsTable._ID)));
                question.setQuestion(c.getString(c.getColumnIndex(QuestionsTable.COLUMN_QUESTION)));
                question.setOption1(c.getString(c.getColumnIndex(QuestionsTable.COLUMN_OPTION1)));
                question.setOption2(c.getString(c.getColumnIndex(QuestionsTable.COLUMN_OPTION2)));
                question.setOption3(c.getString(c.getColumnIndex(QuestionsTable.COLUMN_OPTION3)));
                question.setAnswerNumber(c.getInt(c.getColumnIndex(QuestionsTable.COLUMN_ANSWER_NR)));
                question.setDifficulty(c.getString(c.getColumnIndex(QuestionsTable.COLUMN_DIFFICULTY)));
                question.setCategoryId(c.getInt(c.getColumnIndex(QuestionsTable.COLUMN_CATEGORY_ID)));
                questionList.add(question);
            } while (c.moveToNext());
        }
        c.close();
        return questionList;
    }

    @SuppressLint("Range")
    public ArrayList<Question> getQuestion(int categoryId,String difficulty) {
        ArrayList<Question> questionList = new ArrayList<>();
        sqLiteDatabase = getReadableDatabase();

        String selection=QuestionsTable.COLUMN_CATEGORY_ID+" = ? "+
                " AND "+QuestionsTable.COLUMN_DIFFICULTY+" = ? ";
        String [] selectionArgs=new String[] {String.valueOf(categoryId),difficulty};
        Cursor c=sqLiteDatabase.query(QuestionsTable.TABLE_NAME,null,selection,selectionArgs,null,null,null);

        if (c.moveToFirst()) {
            do {
                Question question = new Question();
                question.setId(c.getInt(c.getColumnIndex(QuestionsTable._ID)));
                question.setQuestion(c.getString(c.getColumnIndex(QuestionsTable.COLUMN_QUESTION)));
                question.setOption1(c.getString(c.getColumnIndex(QuestionsTable.COLUMN_OPTION1)));
                question.setOption2(c.getString(c.getColumnIndex(QuestionsTable.COLUMN_OPTION2)));
                question.setOption3(c.getString(c.getColumnIndex(QuestionsTable.COLUMN_OPTION3)));
                question.setAnswerNumber(c.getInt(c.getColumnIndex(QuestionsTable.COLUMN_ANSWER_NR)));
                question.setDifficulty(c.getString(c.getColumnIndex(QuestionsTable.COLUMN_DIFFICULTY)));
                question.setCategoryId(c.getInt(c.getColumnIndex(QuestionsTable.COLUMN_CATEGORY_ID)));
                questionList.add(question);
            } while (c.moveToNext());
        }
        c.close();
        return questionList;
    }
}