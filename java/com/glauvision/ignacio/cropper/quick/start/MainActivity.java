// "Therefore those skilled at the unorthodox
// are infinite as heaven and earth,
// inexhaustible as the great rivers.
// When they come to an end,
// they begin again,
// like the days and months;
// they die and are reborn,
// like the four seasons."
//
// - Sun Tsu,
// "The Art of War"

package com.glauvision.ignacio.cropper.quick.start;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;
import com.theartofdev.edmodo.cropper.quick.start.R;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import Communication.ServerCommunication;
import Diagnose.Detection;
import Diagnose.DataDiagnose;
import Diagnose.newDiagnose;
import History.DiagHistory;

public class MainActivity extends AppCompatActivity {

    private final String TAG = "MainActivity";

    private SectionsPageAdapter mSectionsPageAdapter;

    private ViewPager mViewPager;

    public ArrayList<DataDiagnose> total_diagnoses;
    public DataDiagnose current_diagnose;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i("OpenCV", "OpenCV loaded successfully");
                    Mat imageMat =new Mat();
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    public void onResume()
    {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d("OpenCV", "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_4_0, this, mLoaderCallback);
        } else {
            Log.d("OpenCV", "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG,"onCreate: Starting");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.mSectionsPageAdapter = new SectionsPageAdapter(getSupportFragmentManager());
        total_diagnoses = new ArrayList<DataDiagnose>();
        this.current_diagnose = new DataDiagnose();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mViewPager = (ViewPager) findViewById(R.id.container);
        setupViewPager(mViewPager);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);
    }

    private void setupViewPager(ViewPager viewPager){

        mSectionsPageAdapter.addFragment(new newDiagnose() , "New Diagnose", 0);
        mSectionsPageAdapter.addFragment(new DiagHistory(), "History", 1);
        viewPager.setAdapter(mSectionsPageAdapter);
    }





    public void isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
        }
    }

    public void onSaveImageClick(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Please insert the diagnosis' title");
        isStoragePermissionGranted();
        // Set up the input
        final EditText input = new EditText(this);
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String title = input.getText().toString();
                File sdCardDirectory = Environment.getExternalStorageDirectory();
                File image = new File(sdCardDirectory, title);
                boolean success = false;
                // Encode the file as a PNG image.

                FileOutputStream outStream;
                Bitmap compressed = current_diagnose.getDetection();
                try {

                    outStream = new FileOutputStream(image);
                    compressed.compress(Bitmap.CompressFormat.PNG, 100, outStream);
                    /* 100 to keep full quality of the image */

                    outStream.flush();
                    outStream.close();
                    success = true;
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (!success) {
                    Toast.makeText(getApplicationContext(),
                            "Error saving. Don't forget to give a name!", Toast.LENGTH_LONG).show();
                }else{
                    //Local storage
                    ((ImageView) findViewById(R.id.fabSaveDiag)).setVisibility(View.INVISIBLE);
                    createImageFromBitmap(compressed, title);
                    current_diagnose.setName_detection(title);


                    total_diagnoses.add(current_diagnose);

                    mSectionsPageAdapter.removeFragment(new DiagHistory(), 1);
                    mSectionsPageAdapter.addFragment(new DiagHistory(), "History", 1);
                    mSectionsPageAdapter.notifyDataSetChanged();

                    //Remote upload
                    DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
                    String current_date = df.format(Calendar.getInstance().getTime());
                    String diagnose_report =
                            "Date: " +current_date + "\n" +
                            "Size of the shadow: " + current_diagnose.getShadow_size() + "\n";
                    if (current_diagnose.getShadow_size() > 5){
                        diagnose_report = diagnose_report + "Decision: Needs further attention";
                    }else{
                        diagnose_report = diagnose_report + "Decision: Not glaucoma risk detected";
                    }

                    ServerCommunication upload = new ServerCommunication(current_diagnose.getDetection(), current_diagnose.getName_detection(), diagnose_report);
                    upload.execute();
                    Toast.makeText(getApplicationContext(),
                            "Image shared with specialist!", Toast.LENGTH_LONG).show();
                }

            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();

    }

    /** Start pick image activity with chooser. */
    public void onSelectNewDiagnosis(View view) {
        CropImage.activity()
                //        .setBackgroundColor(77ff0000)
                .setGuidelines(CropImageView.Guidelines.ON)
                .setActivityTitle("Crop eye")
                .setCropShape(CropImageView.CropShape.RECTANGLE)
                .setCropMenuCropButtonTitle("Done")
                .setRequestedSize(400, 400)
                .setCropMenuCropButtonIcon(R.drawable.crop_icon)
                .start(this);


    }

    public void createImageFromBitmap(Bitmap bitmap, String name) {
        String fileName = name;
        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
            FileOutputStream fo = openFileOutput(fileName, Context.MODE_PRIVATE);
            fo.write(bytes.toByteArray());
            // remember close file output
            fo.close();
        } catch (Exception e) {
            e.printStackTrace();
            fileName = null;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        current_diagnose = new DataDiagnose();
        // handle result of CropImageActivity
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {
                Uri imageUri = result.getUri();
                Bitmap bitmap = null;
                try {
                  bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                } catch (IOException e) {
                  e.printStackTrace();
                }

                //perform the detection algorithm
                Detection dec = new Detection(bitmap);
                current_diagnose.setDetection(dec.performDetection());
                current_diagnose.setShadow_size(dec.getShadowSize());

                mSectionsPageAdapter.removeFragment(new newDiagnose(), 0);
                mSectionsPageAdapter.addFragment(new newDiagnose(), "New Diagnose", 0);
                mSectionsPageAdapter.notifyDataSetChanged();

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Toast.makeText(this, "Cropping failed: " + result.getError(), Toast.LENGTH_LONG).show();
            }
        }
    }



}


