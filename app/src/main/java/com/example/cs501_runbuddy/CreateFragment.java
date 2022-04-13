package com.example.cs501_runbuddy;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;


public class CreateFragment extends Fragment {

    private CheckBox mile1BoxCreate;
    private CheckBox mile5BoxCreate;
    private CheckBox mile10BoxCreate;
    private CheckBox privateBtn;

    private Button Createbtn;
    private TextView ExplainRoomType;

    private CreateGame listener;

    public interface CreateGame{
        void startGame(String ID, boolean type, int totalDistance);
    }


    public CreateFragment() {
        // Required empty public constructor
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v =  inflater.inflate(R.layout.fragment_create, container, false);

        mile1BoxCreate = v.findViewById(R.id.mile1BoxCreate);
        mile5BoxCreate = v.findViewById(R.id.mile5BoxCreate);
        mile10BoxCreate = v.findViewById(R.id.mile10BoxCreate);
        privateBtn = v.findViewById(R.id.privateBtn);
        Createbtn = v.findViewById(R.id.Createbtn);
        ExplainRoomType = v.findViewById(R.id.ExplainRoomType);

        mile1BoxCreate.setChecked(true);

        //make user only choose one certain mile game
        mile1BoxCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mile1BoxCreate.setChecked(true);
                mile5BoxCreate.setChecked(false);
                mile10BoxCreate.setChecked(false);

            }
        });
        mile5BoxCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mile1BoxCreate.setChecked(false);
                mile5BoxCreate.setChecked(true);
                mile10BoxCreate.setChecked(false);

            }
        });
        mile10BoxCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mile1BoxCreate.setChecked(false);
                mile5BoxCreate.setChecked(false);
                mile10BoxCreate.setChecked(true);
            }
        });

        // Create Game Button on click listener
        Createbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String input1 = String.valueOf(getRandomNumber(100000,999999));
                boolean input2 = privateBtn.isChecked();
                int input3 = 1;
                if(mile1BoxCreate.isChecked()){input3 = 1;}
                else if (mile5BoxCreate.isChecked()){input3 = 5;}
                else{input3 = 10;}
                listener.startGame(input1,input2,input3);
            }
        });

        return v;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if(context instanceof CreateGame){
            listener = (CreateGame) context;
        }else{
            throw new RuntimeException(context.toString() + "must implement CreateGame");
        }
    }


    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    public int getRandomNumber(int min, int max) {
        return (int) ((Math.random() * (max - min)) + min);
    }
}