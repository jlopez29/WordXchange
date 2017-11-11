package com.example.jesse.test;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.firebase.auth.FirebaseAuth;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Random;
import java.util.Set;
/**
 * Created by jesse on 11/1/2017.
 * Main activity of wordXchange which deals with main game loop
 */

public class MainActivity extends AppCompatActivity {

    String TAG = "*** Xchange -  Main ***";
    String word;
    String ogWord;

    boolean retry = false;
    boolean paused = false;
    boolean checkTimer = false;
    boolean add = false;

    LinearLayout la;
    ImageView iv;
    Button currButton;

    Counter countDown;
    Context mContext;
    InputMethodManager imm;
    SharedPreferences prefs;

    int time;
    int currIndex = 0;
    int score = 0;
    long currTime;
    Integer[] scores;

    Set<String> wordSet;
    ArrayList<String> wordList;
    ArrayList<String> usedWords;

    public static boolean logout = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.e(TAG,"*** OnCreate ***");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;

        /*

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("message");

        myRef.setValue("Hello, World!");

        // Read from the database
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                String value = dataSnapshot.getValue(String.class);
                Log.d(TAG, "Value is: " + value);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });

        */

        // Toolbar that contains drop down settings icon
        Toolbar myToolbar = findViewById(R.id.my_toolbar);
        myToolbar.setTitleTextColor(Color.WHITE);
        myToolbar.showOverflowMenu();
        setSupportActionBar(myToolbar);

            try{
                AssetManager assetManager = getAssets();
                InputStream is = assetManager.open("dictionary.txt");
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                String read = reader.readLine();
                wordSet = new HashSet<>();
                wordList = new ArrayList<>();
                //Collections.addAll(wordsSet, words);
                while(read != null)
                {
                    read = read.toLowerCase();
                    wordSet.add(read);
                    wordList.add(read);
                    read = reader.readLine();
                }

                //pb.setVisibility(View.INVISIBLE);
            }catch(IOException e)
            {
                e.printStackTrace();
            }
        usedWords = new ArrayList<>();

        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        scores = new Integer[3];

        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        randomWord();
        ogWord = word;

        usedWords.add(word);

        la = findViewById(R.id.boardLayout);
        int sum = (word.length() * 2) + 1;
        la.setWeightSum(sum);
        iv = findViewById(R.id.wordStatus);

        writeBoard();
        time = Integer.valueOf(prefs.getString("timerPref","30"));
        time = time * 1000;
        countDown = new Counter(time,1000);
        countDown.start();

        Log.i(TAG,"Original word: " + word);

        Button resetBoard = findViewById(R.id.newBoard);

        resetBoard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("Reset Board","Clicked");
                newGame();
            }
        });

    }
    public void newGame()
    {
        Log.i("New Game","Entered");
        usedWords = new ArrayList<>();
        add = false;
        score = 0;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextView tv = findViewById(R.id.scoreValue);
                tv.setText(String.format(Locale.getDefault(),"%d", score));
            }
        });
        deleteBoard();
        if(!retry)
        {
            randomWord();
            ogWord = word;
            Log.i(TAG,"New word: " + word);
        }
        else
            word = ogWord;
        int sum = (word.length() * 2) + 1;
        la.setWeightSum(sum);
        writeBoard();
        iv.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.green_check_mark, null));
        retry = false;

        countDown.cancel();
        countDown = new Counter(time,1000);
        countDown.start();
    }

    public boolean contains(String word)
    {
        return wordSet.contains(word);
    }

    public void validateWord(String letter)
    {
        Log.i(TAG,"letter: " + letter);
        String newWord = "";

        int length = (word.length() * 2) + 1;

        if(add)
            Log.i(TAG,"Add letter");
        else
            Log.i(TAG,"Change letter");


        StringBuilder sb = new StringBuilder();

        for(int i = 0; i < length; i++)
        {
            if(!add)
            {
                if((i % 2) != 0) {
                    if (i != currIndex) {
                        Button b = la.findViewWithTag("Button" + i);
                        newWord = sb.append(b.getText()).toString();
                    } else
                        newWord = sb.append(letter).toString();
                }
            }
            else
            {
                if(((i % 2) != 0) || (i == currIndex)) {
                    if (i != currIndex) {
                        Button b = la.findViewWithTag("Button" + i);
                        newWord = sb.append(b.getText()).toString();
                    } else
                    {
                        newWord = sb.append(letter).toString();
                    }

                }
            }

        }
        Log.i(TAG,"new word : " + newWord);

        if(contains(newWord) && !usedWords.contains(newWord))
        {
            Log.i(TAG,"VALID WORD");
            countDown.cancel();
            countDown.start();
            score++;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    TextView tv = findViewById(R.id.scoreValue);
                    tv.setText(String.format(Locale.getDefault(),"%d", score));
                }
            });
            word = newWord;
            usedWords.add(word);
            iv.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.green_check_mark, null));
            if(add)
            {
                int sum = (word.length() * 2) + 1;
                la.setWeightSum(sum);
            }
            else
                currButton.setText(letter);

            deleteBoard();
            writeBoard();
        }
        else {
            deleteBoard();
            writeBoard();
            iv.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.red_x_mark, null));
            Handler h = new Handler();
            h.postDelayed(new Runnable() {
                @Override
                public void run() {
                    iv.setImageDrawable(ResourcesCompat.getDrawable(getResources(),R.drawable.green_check_mark,null));
                }
            },1750);
            Log.i(TAG, "INVALID WORD");
            if(usedWords.contains(newWord))
                Toast.makeText(MainActivity.this, "Duplicate Word!", Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(MainActivity.this, "Invalid Word!", Toast.LENGTH_SHORT).show();
        }

        add = false;
    }

    public void deleteLetter()
    {
        Log.i(TAG,"letter: " + currButton.getText());
        String newWord = "";

        StringBuilder sb = new StringBuilder();

        int length = (word.length() * 2) + 1;
        for(int i = 0; i < length; i++)
        {
            if((i%2) != 0)
            {
                if(i != currIndex)
                {
                    Button b = la.findViewWithTag("Button"+i);
                    newWord = sb.append(b.getText()).toString();
                }
            }


        }

        final String finalNew = newWord;
        Log.i(TAG,"new word : " + newWord);

        if(contains(newWord)&& !usedWords.contains(newWord))
        {
            currButton.setBackgroundColor(Color.RED);
            Handler h = new Handler();
            h.postDelayed(new Runnable() {
                @Override
                public void run() {
                    countDown.cancel();
                    countDown.start();
                    deleteBoard();
                    score++;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            TextView tv = findViewById(R.id.scoreValue);
                            tv.setText(String.format(Locale.getDefault(),"%d", score));
                        }
                    });
                    word = finalNew;
                    usedWords.add(word);
                    Log.i(TAG,"VALID WORD");
                    iv.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.green_check_mark, null));
                    int sum = (word.length() * 2) + 1;
                    la.setWeightSum(sum);
                    writeBoard();
                }
            },650);

        }
        else {
            iv.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.red_x_mark, null));
            Handler h = new Handler();
            h.postDelayed(new Runnable() {
                  @Override
                  public void run() {
                            iv.setImageDrawable(ResourcesCompat.getDrawable(getResources(),R.drawable.green_check_mark,null));
                  }
            },1750);
            Log.i(TAG, "INVALID WORD");
            if(usedWords.contains(newWord))
                Toast.makeText(MainActivity.this, "Duplicate Word!", Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(MainActivity.this, "Invalid Word!", Toast.LENGTH_SHORT).show();
        }
    }

    public void deleteBoard()
    {

        int length = (word.length() * 2) + 1;
        for(int i = 0; i < length; i++)
        {
            Button b = la.findViewWithTag("Button" + i);
            la.removeView(b);
        }
    }

    public void writeBoard()
    {
        int length = (word.length() * 2) + 1;
        int lIndex = 0;
        for(int i = 0; i < length; i++)
        {
            Button btnTag = new Button(this);

            if((i % 2) == 0)
            {
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
                lp.setMargins(0,120,0,0);
                btnTag.setLayoutParams(lp);
                btnTag.setText("^");
                btnTag.setTag("Button" + i);
                final int z = i;
                btnTag.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        Log.i("TAG", "Click " + z);
                        add = true;
                        currIndex = z;
                        currButton = la.findViewWithTag("Button"+z);
                        currButton.setBackgroundColor(Color.YELLOW);
                        //builder.show();
                        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,0);
                    }
                });
                la.addView(btnTag);
            }
            else
            {
                btnTag.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
                Character c = word.charAt(lIndex);
                btnTag.setText(c.toString());
                btnTag.setTag("Button" + i);
                final int z = i;
                btnTag.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        Log.i("TAG", "Click " + z);
                        currIndex = z;
                        currButton = la.findViewWithTag("Button"+z);
                        currButton.setBackgroundColor(Color.YELLOW);
                        //builder.show();
                        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,0);
                    }
                });
                btnTag.setOnLongClickListener(new View.OnLongClickListener() {
                    public boolean onLongClick(View v) {
                        Log.i("TAG", "Long Click " + z);
                        currIndex = z;
                        currButton = la.findViewWithTag("Button"+z);
                        deleteLetter();
                        return true;
                    }
                });
                la.addView(btnTag);
                lIndex++;
            }

        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {

        String chosenLetter = "";

        if(event.getAction() == KeyEvent.ACTION_UP)
        {
            switch (event.getKeyCode()) {
                case KeyEvent.KEYCODE_A:
                    Log.i(TAG, "A Pressed");
                    chosenLetter = "a";
                    break;
                case KeyEvent.KEYCODE_B:
                    Log.i(TAG, "B Pressed");
                    chosenLetter = "b";
                    break;
                case KeyEvent.KEYCODE_C:
                    Log.i(TAG, "C Pressed");
                    chosenLetter = "c";
                    break;
                case KeyEvent.KEYCODE_D:
                    Log.i(TAG, "D Pressed");
                    chosenLetter = "d";
                    break;
                case KeyEvent.KEYCODE_E:
                    Log.i(TAG, "E Pressed");
                    chosenLetter = "e";
                    break;
                case KeyEvent.KEYCODE_F:
                    Log.i(TAG, "F Pressed");
                    chosenLetter = "f";
                    break;
                case KeyEvent.KEYCODE_G:
                    Log.i(TAG, "G Pressed");
                    chosenLetter = "g";
                    break;
                case KeyEvent.KEYCODE_H:
                    Log.i(TAG, "H Pressed");
                    chosenLetter = "h";
                    break;
                case KeyEvent.KEYCODE_I:
                    Log.i(TAG, "I Pressed");
                    chosenLetter = "i";
                    break;
                case KeyEvent.KEYCODE_J:
                    Log.i(TAG, "J Pressed");
                    chosenLetter = "j";
                    break;
                case KeyEvent.KEYCODE_K:
                    Log.i(TAG, "K Pressed");
                    chosenLetter = "k";
                    break;
                case KeyEvent.KEYCODE_L:
                    Log.i(TAG, "L Pressed");
                    chosenLetter = "l";
                    break;
                case KeyEvent.KEYCODE_M:
                    Log.i(TAG, "M Pressed");
                    chosenLetter = "m";
                    break;
                case KeyEvent.KEYCODE_N:
                    Log.i(TAG, "N Pressed");
                    chosenLetter = "n";
                    break;
                case KeyEvent.KEYCODE_O:
                    Log.i(TAG, "O Pressed");
                    chosenLetter = "o";
                    break;
                case KeyEvent.KEYCODE_P:
                    Log.i(TAG, "P Pressed");
                    chosenLetter = "p";
                    break;
                case KeyEvent.KEYCODE_Q:
                    Log.i(TAG, "Q Pressed");
                    chosenLetter = "q";
                    break;
                case KeyEvent.KEYCODE_R:
                    Log.i(TAG, "R Pressed");
                    chosenLetter = "r";
                    break;
                case KeyEvent.KEYCODE_S:
                    Log.i(TAG, "S Pressed");
                    chosenLetter = "s";
                    break;
                case KeyEvent.KEYCODE_T:
                    Log.i(TAG, "T Pressed");
                    chosenLetter = "t";
                    break;
                case KeyEvent.KEYCODE_U:
                    Log.i(TAG, "U Pressed");
                    chosenLetter = "u";
                    break;
                case KeyEvent.KEYCODE_V:
                    Log.i(TAG, "V Pressed");
                    chosenLetter = "v";
                    break;
                case KeyEvent.KEYCODE_W:
                    Log.i(TAG, "W Pressed");
                    chosenLetter = "w";
                    break;
                case KeyEvent.KEYCODE_X:
                    Log.i(TAG, "X Pressed");
                    chosenLetter = "x";
                    break;
                case KeyEvent.KEYCODE_Y:
                    Log.i(TAG, "Y Pressed");
                    chosenLetter = "y";
                    break;
                case KeyEvent.KEYCODE_Z:
                    Log.i(TAG, "Z Pressed");
                    chosenLetter = "z";
                    break;
                default:
                    Log.i(TAG,"Default press");
                    imm.hideSoftInputFromWindow(iv.getWindowToken(), 0);
                    break;
            }
        }

        imm.hideSoftInputFromWindow(iv.getWindowToken(),0);

        if(!chosenLetter.equals("")) {
            validateWord(chosenLetter);
            return true;
        }

        return super.dispatchKeyEvent(event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        Log.i(TAG,"On touch event");
        if(add)
            add = false;

        deleteBoard();
        writeBoard();

        imm.hideSoftInputFromWindow(iv.getWindowToken(),0);
        return true;
    }

    public void randomWord()
    {
        int min = 0;
        int max = wordSet.size();

        Random r = new Random();
        int rand = r.nextInt(max - min) + min;

        word = wordList.get(rand);

        while(word.length() > 4)
        {
            r = new Random();
            rand = r.nextInt(max - min) + min;

            word = wordList.get(rand);
        }
    }

    class Counter extends CountDownTimer {
        Counter(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }
        @Override
        public void onFinish(){
            iv.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.red_x_mark, null));
            Toast.makeText(MainActivity.this, "Time is up!", Toast.LENGTH_SHORT).show();
            String scoreText = "Score : " + score;
            scores = new Integer[3];
            boolean highScore = false;

            imm.hideSoftInputFromWindow(iv.getWindowToken(),0);

            for(int i = 0; i < scores.length; i++)
            {

                scores[i] = prefs.getInt("score" + i,0);
            }

            Arrays.sort(scores);

            for(int i = 0; i < scores.length; i++)
            {
                if(!highScore && score > 0)
                {
                    if(score > scores[i])
                    {
                        prefs.edit().putInt("score" + i,score).apply();
                        highScore = true;
                        scores[i] = score;
                    }
                }
            }

            Arrays.sort(scores);

            for(int i = 0; i < scores.length; i++)
            {

                prefs.edit().putInt("score" + i,scores[i]).apply();
                Log.i(TAG," score " + i + " : " + scores[i]);
            }



            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder .setCancelable(false)
                    .setNeutralButton("Retry" , new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            retry = true;
                            newGame();
                        }
                    }).setPositiveButton("New Game" , new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            newGame();
                        }
                    }).setNegativeButton("Exit" , new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            finishAndRemoveTask();
                        }
                    });

            if(highScore)
            {
                Log.i(TAG,"High score");
                Arrays.sort(scores, Collections.reverseOrder());
                builder.setTitle("High Score!")
                        .setMessage(" 1.) " +scores[0] + "\n 2.) " + scores[1] + "\n 3.) " + scores[2])
                        .show();
            }
            else
            {
                Log.i(TAG,"Game over");
                builder.setTitle("Game Over!")
                        .setMessage(scoreText)
                        .show();
            }


        }
        @Override
        public void onTick(long millisUntilFinished){
            if((millisUntilFinished / 1000) == 5)
                Toast.makeText(mContext, "Five Seconds!", Toast.LENGTH_SHORT).show();
            currTime = millisUntilFinished;
            Log.i("1/2 min counter: ", String.valueOf(millisUntilFinished / 1000));
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();

        Log.e("ONDESTROY", "**************STARTED*************");

        usedWords = new ArrayList<>();
        add = false;
        paused = false;
        checkTimer = false;
        score = 0;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextView tv = findViewById(R.id.scoreValue);
                tv.setText(String.format(Locale.getDefault(),"%d", score));
            }
        });
        deleteBoard();
        retry = false;
        countDown.cancel();

    }

    @Override
    public void onResume() {
        super.onResume();
        Log.e(TAG,"*** On Resume ***");
        if(paused) {
            if (checkTimer) {
                int newTime = Integer.valueOf(prefs.getString("timerPref", "30"));
                newTime *= 1000;

                if (newTime != time) {

                    if(currTime > newTime)
                        countDown = new Counter(newTime,1000);
                    else
                        countDown = new Counter(currTime,1000);

                    time = newTime;
                    countDown.start();
                }
                else
                {
                    countDown = new Counter(currTime,1000);
                    countDown.start();
                    paused = false;
                }
                checkTimer = false;
            } else
            {
                countDown = new Counter(currTime,1000);
                countDown.start();
                paused = false;
            }

        }
    }
    @Override
    public void onPause() {
        super.onPause();
        Log.e(TAG,"*** On Pause ***");
        countDown.cancel();
        paused = true;
    }

    // Handles the drop down settings menu and what activity to start when an item is pressed
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.settings:
                Intent settingsIntent = new Intent(MainActivity.this, SettingsActivity.class);
                countDown.cancel();
                checkTimer = true;
                startActivity(settingsIntent);
                break;
            case R.id.logout:

                Log.e(TAG,"*** Logout ***");
                logout = true;

                Intent login = new Intent(this,GoogleSignInActivity.class);
                login.putExtra("startup",false);
                startActivity(login);

                finish();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }

        return true;
    }
    // Creates menu to crete drop down settings menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }
}
