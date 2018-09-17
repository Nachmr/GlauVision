package History;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import com.glauvision.ignacio.cropper.quick.start.MainActivity;
import com.theartofdev.edmodo.cropper.quick.start.R;

import java.io.FileNotFoundException;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import Diagnose.DataDiagnose;

public class DiagHistory extends Fragment {
    private GridView gridView;
    private GridViewAdapter gridAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_history, container,    false);
        gridView = (GridView) rootView.findViewById(R.id.gridView);


        return rootView;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            try {
                gridAdapter = new GridViewAdapter(this.getContext(), R.layout.grid_item_layout, getData());
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            gridView.setAdapter(gridAdapter);
            gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                    ImageItem item = (ImageItem) parent.getItemAtPosition(position);

                    //Create intent
                    Intent intent = new Intent(getActivity(), DetailsActivity.class);
                    intent.putExtra("title", item.getTitle());
                    intent.putExtra("image", item.getImage());
                    intent.putExtra("date", item.getDate());
                    intent.putExtra("shadow", item.getSizeShadow());

                    //Start details activity
                    startActivity(intent);
                }
            });
        }
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    // Prepare for gridview
    private ArrayList<ImageItem> getData() throws FileNotFoundException {
        @SuppressLint("SimpleDateFormat") DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
        final ArrayList<ImageItem> imageItems = new ArrayList<>();

        ArrayList<DataDiagnose> diagnoses = ((MainActivity)getContext()).total_diagnoses;

//        Toast.makeText(getContext(), "diagnoses size: " + String.valueOf(diagnoses.size()), Toast.LENGTH_LONG).show();

        for (int i = 0; i < diagnoses.size(); i++) {
            Bitmap bitmap = BitmapFactory.decodeStream(getActivity()
                    .openFileInput(diagnoses.get(i).getName_detection()));
            String reportDate = df.format(Calendar.getInstance().getTime());
            DecimalFormat decf = new DecimalFormat("#.####");
            decf.setRoundingMode(RoundingMode.CEILING);
            double size = diagnoses.get(i).getShadow_size();
            String shadow_msg = "Shadow size: " + decf.format(size);
            if (bitmap != null)
                imageItems.add(new ImageItem(bitmap, diagnoses.get(i).getName_detection(), "Date: \n" + reportDate, shadow_msg));
        }


        return imageItems;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }
}
