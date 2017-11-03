package com.example.jesse.test;

import android.content.DialogInterface;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

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
 */

public class MainActivity extends AppCompatActivity {
    String TAG = "*** Main ***";
    Set<String> wordSet;
    ArrayList<String> wordList;
    String[] alphabet = {"a","b","c","d","e","f","g","h","i","j","k","l","m","n","o","p","q","r","s","t","u","v","w","x","y","z"};
    Button currButton;
    int currIndex = 0;
    String word;
    LinearLayout la;
    AlertDialog.Builder builder;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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

        randomWord();

        la = (LinearLayout)findViewById(R.id.boardLayout);
        la.setWeightSum(word.length());

        builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Letters")
                .setItems(alphabet, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // The 'which' argument contains the index position
                        // of the selected item
                        Log.i(TAG,"letter: " + alphabet[which]);
                        String newWord = "";
                        ImageView iv = (ImageView)findViewById(R.id.wordStatus);

                        for(int i = 0; i < word.length(); i++)
                        {
                            if(i != currIndex)
                            {
                                Button b = (Button)la.findViewWithTag("Button"+i);
                                newWord += b.getText();
                            }
                            else
                                newWord += alphabet[which];

                        }
                        Log.i(TAG,"new word : " + newWord);

                        if(contains(newWord))
                        {
                            word = newWord;
                            Log.i(TAG,"VALID WORD");
                            iv.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.green_check_mark, null));
                            currButton.setText(alphabet[which]);
                        }
                        else {
                            iv.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.red_x_mark, null));
                            Log.i(TAG, "INVALID WORD");
                        }
                    }
                });


        writeBoard();

        Button resetBoard = (Button)findViewById(R.id.newBoard);

        resetBoard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("Reset Board","Clicked");
                deleteBoard();
                randomWord();
                writeBoard();
            }
        });

    }

    public boolean contains(String word)
    {
        return wordSet.contains(word);
    }

    public boolean deleteLetter(int index)
    {
        Log.i(TAG,"letter: " + currButton.getText());
        String newWord = "";
        ImageView iv = (ImageView)findViewById(R.id.wordStatus);

        for(int i = 0; i < word.length(); i++)
        {
            if(i != currIndex)
            {
                Button b = (Button)la.findViewWithTag("Button"+i);
                newWord += b.getText();
            }

        }
        Log.i(TAG,"new word : " + newWord);

        if(contains(newWord))
        {
            deleteBoard();
            word = newWord;
            Log.i(TAG,"VALID WORD");
            iv.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.green_check_mark, null));
            la.setWeightSum(word.length());
            writeBoard();

            return true;
        }
        else {
            iv.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.red_x_mark, null));
            Log.i(TAG, "INVALID WORD");
            return false;
        }
    }

    public void deleteBoard()
    {
        for(int i = 0; i < word.length(); i++)
        {
            Button b = (Button)la.findViewWithTag("Button" + i);
            la.removeView(b);
        }
    }

    public void writeBoard()
    {
        for(int i = 0; i < word.length(); i++)
        {
            Button btnTag = new Button(this);
            btnTag.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
            Character c = word.charAt(i);
            btnTag.setText(c.toString());
            btnTag.setTag("Button" + i);
            final int z = i;
            btnTag.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Log.i("TAG", "Click " + z);
                    currIndex = z;
                    currButton = (Button)la.findViewWithTag("Button"+z);
                    builder.show();
                }
            });
            btnTag.setOnLongClickListener(new View.OnLongClickListener() {
                public boolean onLongClick(View v) {
                    Log.i("TAG", "Long Click " + z);
                    currIndex = z;
                    currButton = (Button)la.findViewWithTag("Button"+z);
                    deleteLetter(currIndex);
                    return true;
                }
            });
            la.addView(btnTag);
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
}
