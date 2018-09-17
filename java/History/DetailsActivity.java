package History;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;

import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.TextView;

import com.theartofdev.edmodo.cropper.quick.start.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DetailsActivity extends AppCompatActivity {


    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.details_activity);

        String title = getIntent().getStringExtra("title");
        Bitmap image = getIntent().getParcelableExtra("image");
        String date = getIntent().getStringExtra("date");
        String shadow = getIntent().getStringExtra("shadow");

        TextView titleTextView = (TextView) findViewById(R.id.title);
        titleTextView.setText(title);

        TextView titleDateView = (TextView) findViewById(R.id.date);
        titleDateView.setText(date);

        ImageView imageView = (ImageView) findViewById(R.id.image);
        imageView.setImageBitmap(image);

        TextView shadowTitleView = (TextView) findViewById(R.id.shadow);
        shadowTitleView.setText(shadow);

        TextView decision = (TextView) findViewById(R.id.decision);


        Pattern pattern = Pattern.compile(": *");
        Matcher matcher = pattern.matcher(shadow);
        String shadow_float = "";
        if (matcher.find()) {
            shadow_float = shadow.substring(matcher.end());
        }
        float shadow_size = Float.parseFloat(shadow_float);
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
        shadowTitleView.setText(shadow);
    }
}
