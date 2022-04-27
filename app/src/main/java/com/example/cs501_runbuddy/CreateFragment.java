package com.example.cs501_runbuddy;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.skydoves.balloon.ArrowOrientation;
import com.skydoves.balloon.ArrowPositionRules;
import com.skydoves.balloon.Balloon;
import com.skydoves.balloon.BalloonAnimation;
import com.skydoves.balloon.BalloonSizeSpec;
import com.skydoves.balloon.overlay.BalloonOverlayAnimation;

import java.util.ArrayList;


public class CreateFragment extends Fragment {

    private CheckBox mile1BoxCreate;
    private CheckBox mile5BoxCreate;
    private CheckBox mile10BoxCreate;
    private CheckBox privateBtn;
    private CheckBox isAsyncBtn;

    private Button Createbtn;


    private CreateGame listener;

    private int selectedDistance;
    private ImageView privateInfoImage;
    private ImageView makeRaceInfoImage;

    public interface CreateGame{
        void startGame(String ID, boolean isPrivate, boolean isAsync, int totalDistance);
    }


    public CreateFragment() {
        // Required empty public constructor
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v =  inflater.inflate(R.layout.fragment_create, container, false);

        privateBtn = v.findViewById(R.id.privateBtn);
        isAsyncBtn = v.findViewById(R.id.isAsyncBtn);
        Createbtn = v.findViewById(R.id.Createbtn);
        privateInfoImage = (ImageView) v.findViewById(R.id.privateInfoImage);
        makeRaceInfoImage = (ImageView) v.findViewById(R.id.makeInfoImage);





        Balloon balloon = new Balloon.Builder(getActivity())
                .setArrowSize(10)
                .setArrowOrientation(ArrowOrientation.TOP)
                .setArrowPositionRules(ArrowPositionRules.ALIGN_ANCHOR)
                .setArrowPosition(0.5f)
                .setWidth(BalloonSizeSpec.WRAP)
                .setHeight(BalloonSizeSpec.WRAP)
                .setTextSize(15f)
                .setCornerRadius(4f)
                .setAlpha(0.9f)
                .setIsVisibleOverlay(true)
                .setBalloonOverlayAnimation(BalloonOverlayAnimation.FADE)
                .setText("If your race is private, share your game ID for your friend to join directly")
                .setTextColor(Color.WHITE)
                .setOverlayPadding(6f)
                .setOverlayColor(Color.parseColor("#9900203F"))
                .setTextIsHtml(true)
                .setBackgroundColor(Color.parseColor("#242526"))
                .setMargin(10)
                .setPadding(10)
                .setBalloonAnimation(BalloonAnimation.FADE).build();
                //.setOnBalloonClickListener(onBalloonClickListener)
                //.setLifecycleOwner(lifecycleOwner)

        Balloon balloon2 = new Balloon.Builder(getActivity())
                .setArrowSize(10)
                .setArrowOrientation(ArrowOrientation.TOP)
                .setArrowPositionRules(ArrowPositionRules.ALIGN_ANCHOR)
                .setArrowPosition(0.5f)
                .setWidth(BalloonSizeSpec.WRAP)
                .setHeight(BalloonSizeSpec.WRAP)
                .setTextSize(15f)
                .setCornerRadius(4f)
                .setAlpha(0.9f)
                .setIsVisibleOverlay(true)
                .setBalloonOverlayAnimation(BalloonOverlayAnimation.FADE)
                .setText("When checked, you can race each other at different starting times")
                .setTextColor(Color.WHITE)
                .setOverlayPadding(6f)
                .setOverlayColor(Color.parseColor("#9900203F"))
                .setTextIsHtml(true)
                .setBackgroundColor(Color.parseColor("#242526"))
                .setMargin(10)
                .setPadding(10)
                .setBalloonAnimation(BalloonAnimation.FADE).build();

        privateInfoImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                balloon.showAlignBottom(privateInfoImage);
            }
        });

        makeRaceInfoImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                balloon2.showAlignBottom(makeRaceInfoImage);
            }
        });





        Spinner spinner = (Spinner) v.findViewById(R.id.spinner);
        ArrayList<String> arrayList = new ArrayList<>();
        arrayList.add("1 Mile");
        arrayList.add("5 Miles");
        arrayList.add("10 Miles");
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getActivity(), R.layout.spinner_white_colors, arrayList);
        arrayAdapter.setDropDownViewResource(R.layout.spinner_white_colors);
        spinner.setAdapter(arrayAdapter);




        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if(spinner.getItemAtPosition(i).equals("1 Mile")){
                    selectedDistance = 1;
                }else if(spinner.getItemAtPosition(i).equals("5 Miles")){
                    selectedDistance = 5;
                }else{
                    selectedDistance = 10;
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                selectedDistance = 1;
            }
        });


        // Create Game Button on click listener
        Createbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String input1 = String.valueOf(getRandomNumber(100000,999999));// Lobby ID generated by random between 100000 and 999999

                boolean input2 = privateBtn.isChecked();//boolean value of private room or not

                boolean input3 = isAsyncBtn.isChecked();

                int input4 = selectedDistance;

                listener.startGame(input1,input2,input3, input4);
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
