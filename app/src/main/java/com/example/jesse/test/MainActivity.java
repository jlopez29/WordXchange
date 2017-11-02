package com.example.jesse.test;

import android.content.res.AssetManager;
import android.graphics.Path;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
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

        int min = 0;
        int max = wordSet.size();

        Random r = new Random();
        int rand = r.nextInt(max - min) + min;

        String word = wordList.get(rand);

        LinearLayout la = (LinearLayout)findViewById(R.id.boardLayout);
        la.setWeightSum(word.length());

        for(int i = 0; i < word.length(); i++)
        {
            //set the properties for button
            Button btnTag = new Button(this);
            btnTag.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
            Character c = word.charAt(i);
            btnTag.setText(c.toString());
            btnTag.setTag("Button" + i);

            //add button to the layout
            la.addView(btnTag);
        }

    }

    public boolean contains(String word)
    {
        return wordSet.contains(word);
    }
}
