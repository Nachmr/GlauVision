package Diagnose;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.glauvision.ignacio.cropper.quick.start.MainActivity;
import com.theartofdev.edmodo.cropper.quick.start.R;

import java.math.RoundingMode;
import java.text.DecimalFormat;

public class newDiagnose extends Fragment{
    @Nullable

    ImageView eye_animation;
    ImageView show_diagnostic;
    TextView txt_shadow_size;
    TextView decision;
    FloatingActionButton saveBtn;
    AnimationDrawable anim;

    Double size_shadow;
    Bitmap detection;

    @SuppressLint("SetTextI18n")
    @Override
    public void onResume(){
        super.onResume();
        DataDiagnose data =((MainActivity)getContext()).current_diagnose;

        double shadow_size = data.getShadow_size();
        if(shadow_size == -1.0){
            show_diagnostic.setVisibility(View.INVISIBLE);
            anim = (AnimationDrawable)eye_animation.getBackground();
            anim.start();
        }else{
            eye_animation.setVisibility(View.INVISIBLE);

            show_diagnostic.setImageBitmap(data.getDetection());
            show_diagnostic.setVisibility(View.VISIBLE);
            Typeface type = Typeface.createFromAsset(getActivity().getAssets(),"fonts/CaviarDreams_Bold.ttf");
            txt_shadow_size.setTypeface(type);
            decision.setTypeface(type);


            if(shadow_size == 0){
                txt_shadow_size.setText("Detection failed. Please try again");
            }else{
                DecimalFormat df = new DecimalFormat("#.####");
                df.setRoundingMode(RoundingMode.CEILING);
                double size = data.getShadow_size();
                txt_shadow_size.setText("Shadow size: " + df.format(size));

                if(shadow_size <= 12){
                    decision.setTextColor(Color.parseColor("#388e3c"));
                    decision.setText("No glaucoma detected");
                }else if(shadow_size > 12 && shadow_size < 15){
                    decision.setTextColor(Color.parseColor("#ff9800"));
                    decision.setText("Doubtful to say");
                } else{
                    decision.setTextColor(Color.parseColor("#b61827"));
                    decision.setText("Glaucoma detected");
                }

                saveBtn.setVisibility(View.VISIBLE);
            }
            txt_shadow_size.setVisibility(View.VISIBLE);


        }

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_diagnose,container,false);

        eye_animation =  view.findViewById(R.id.eye_animation);
        if(eye_animation == null) throw new AssertionError();
        eye_animation.setBackgroundResource(R.drawable.background_animation);

        show_diagnostic =  view.findViewById(R.id.save_image);
        if(show_diagnostic == null) throw new AssertionError();

        txt_shadow_size = view.findViewById(R.id.size_shadow);
        if(txt_shadow_size == null) throw new AssertionError();

        decision = view.findViewById(R.id.decision);
        if(decision == null) throw new AssertionError();

        saveBtn = view.findViewById(R.id.fabSaveDiag);
        if(saveBtn == null) throw new AssertionError();

        return view;
    }


}
