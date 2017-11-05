package com.example.jesse.test;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

/**
 * Created by jesse on 11/1/2017.
 * Main activity of wordXchange which deals with main game loop
 */

public class MainActivity extends AppCompatActivity {
    String TAG = "*** Main ***";
    Set<String> wordSet;
    ArrayList<String> wordList;
    ArrayList<String> usedWords;
    String[] alphabet = {"a","b","c","d","e","f","g","h","i","j","k","l","m","n","o","p","q","r","s","t","u","v","w","x","y","z"};
    Button currButton;
    int currIndex = 0;
    boolean add = false;
    String word;
    String ogWord;
    boolean retry = false;
    LinearLayout la;
    AlertDialog.Builder builder;
    ImageView iv;
    int score = 0;
    Counter countDown;
    Context mContext;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
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

        randomWord();
        ogWord = word;

        usedWords.add(word);

        la = findViewById(R.id.boardLayout);
        int sum = (word.length() * 2) + 1;
        la.setWeightSum(sum);
        iv = findViewById(R.id.wordStatus);

        builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Letters")
                .setItems(alphabet, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // The 'which' argument contains the index position
                        // of the selected item
                        Log.i(TAG,"letter: " + alphabet[which]);
                        String newWord = "";

                        int length = (word.length() * 2) + 1;

                        if(add)
                            Log.i(TAG,"Add letter");
                        else
                            Log.i(TAG,"Change letter");

                        for(int i = 0; i < length; i++)
                        {
                            if(!add)
                            {
                                if((i % 2) != 0) {
                                    if (i != currIndex) {
                                        Button b = la.findViewWithTag("Button" + i);
                                        newWord += b.getText();
                                    } else
                                        newWord += alphabet[which];
                                }
                            }
                            else
                            {
                                if(((i % 2) != 0) || (i == currIndex)) {
                                    if (i != currIndex) {
                                        Button b = la.findViewWithTag("Button" + i);
                                        newWord += b.getText();
                                    } else
                                    {
                                        newWord += alphabet[which];
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
                                    tv.setText(Integer.toString(score));
                                }
                            });
                            word = newWord;
                            usedWords.add(word);
                            iv.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.green_check_mark, null));
                            if(add)
                            {
                                deleteBoard();
                                int sum = (word.length() * 2) + 1;
                                la.setWeightSum(sum);
                                writeBoard();
                            }
                            else
                                currButton.setText(alphabet[which]);
                        }
                        else {
                            iv.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.red_x_mark, null));
                            Handler h = new Handler();
                            h.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    iv.setImageDrawable(ResourcesCompat.getDrawable(getResources(),R.drawable.green_check_mark,null));
                                }
                            },1500);
                            Log.i(TAG, "INVALID WORD");
                            if(usedWords.contains(newWord))
                                Toast.makeText(MainActivity.this, "Duplicate Word!", Toast.LENGTH_SHORT).show();
                            else
                                Toast.makeText(MainActivity.this, "Invalid Word!", Toast.LENGTH_SHORT).show();
                        }

                        add = false;
                    }
                });


        writeBoard();

        countDown = new Counter(30000,1000);
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
                tv.setText(Integer.toString(score));
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
        countDown.start();
    }

    public boolean contains(String word)
    {
        return wordSet.contains(word);
    }

    public void deleteLetter()
    {
        Log.i(TAG,"letter: " + currButton.getText());
        String newWord = "";

        int length = (word.length() * 2) + 1;
        for(int i = 0; i < length; i++)
        {
            if((i%2) != 0)
            {
                if(i != currIndex)
                {
                    Button b = la.findViewWithTag("Button"+i);
                    newWord += b.getText();
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
                            tv.setText(Integer.toString(score));
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
            },750);

        }
        else {
            iv.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.red_x_mark, null));
            Handler h = new Handler();
            h.postDelayed(new Runnable() {
                  @Override
                  public void run() {
                            iv.setImageDrawable(ResourcesCompat.getDrawable(getResources(),R.drawable.green_check_mark,null));
                  }
            },1500);
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
                        builder.show();
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
                        builder.show();
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

            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("Game Over!")
                    .setCancelable(false)
                    .setMessage(scoreText)
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
                    }).show();


        }
        @Override
        public void onTick(long millisUntilFinished){
            if((millisUntilFinished / 1000) == 10)
                Toast.makeText(mContext, "Ten Seconds!", Toast.LENGTH_SHORT).show();

            Log.i("1/2 min counter: ", String.valueOf(millisUntilFinished / 1000));
        }
    }
}
