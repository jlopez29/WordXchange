package com.example.jesse.test;

import android.content.res.AssetManager;
import android.graphics.Path;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
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
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by jesse on 11/1/2017.
 */

public class MainActivity extends AppCompatActivity {
    String TAG = "*** Main ***";
    Set<String> wordsSet;

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
                wordsSet = new HashSet<>();
                //Collections.addAll(wordsSet, words);
                while(read != null)
                {
                    read = read.toLowerCase();
                    wordsSet.add(read);
                    read = reader.readLine();
                }

                //pb.setVisibility(View.INVISIBLE);
            }catch(IOException e)
            {
                e.printStackTrace();
            }

    }

    public boolean contains(String word)
    {
        return wordsSet.contains(word);
    }
}
