package Diagnose;

import android.graphics.Bitmap;

import java.io.Serializable;

public class DataDiagnose {

    private Bitmap detection;
    private String name_detection;
    private double shadow_size;

    public DataDiagnose() {
        detection = null;
        shadow_size = -1;
        name_detection = "";
    }

    public Bitmap getDetection() {
        return detection;
    }

    public void setDetection(Bitmap detection) {
        this.detection = detection;
    }

    public String getName_detection() {
        return name_detection;
    }

    public void setName_detection(String name_detection) {
        this.name_detection = name_detection;
    }

    public double getShadow_size() {
        return shadow_size;
    }

    public void setShadow_size(double shadow_size) {
        this.shadow_size = shadow_size;
    }
}
