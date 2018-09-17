package Diagnose;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.CLAHE;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

public class Detection {
    private Mat image;
    private double shadow_size;

    public Detection(Bitmap frame) {
        this.image = BitmapToMat(frame);
    }

    private Mat BitmapToMat(Bitmap frame) {
        Mat mat = new Mat();
        Utils.bitmapToMat(frame, mat);
        return mat;
    }

    private Bitmap MatToBitmap(Mat frame) {
        Bitmap image = Bitmap.createBitmap(this.image.cols(), this.image.rows(),Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(frame, image);
        return image;
    }

    private void applyCLAHE(Mat srcArry, Mat dstArry) {
        //Function that applies the CLAHE algorithm to "dstArry".

        if (srcArry.channels() >= 3) {
            // READ RGB color image and convert it to Lab
            Mat channel = new Mat();
            Imgproc.cvtColor(srcArry, dstArry, Imgproc.COLOR_BGR2Lab);

            // Extract the L channel
            Core.extractChannel(dstArry, channel, 0);

            // apply the CLAHE algorithm to the L channel
            CLAHE clahe = Imgproc.createCLAHE();
            clahe.setClipLimit(4);
            clahe.apply(channel, channel);

            // Merge the the color planes back into an Lab image
            Core.insertChannel(channel, dstArry, 0);

            // convert back to RGB
            Imgproc.cvtColor(dstArry, dstArry, Imgproc.COLOR_Lab2BGR);

            // Temporary Mat not reused, so release from memory.
            channel.release();
        }

    }

    private Mat blackHatRun(Mat image) {
        Mat element = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(5, 5));
        Mat dst = new Mat();
        Imgproc.morphologyEx(image, dst, Imgproc.MORPH_BLACKHAT, element);
        return dst;
    }

    public Bitmap performDetection(){
        detectionAlgorithm(this.image);
        return MatToBitmap(this.image);
    }

    @SuppressLint("SetTextI18n")
    private void detectionAlgorithm(Mat frame) {
        //Preprocessing
        Mat light_corrected = new Mat();
        applyCLAHE(frame, light_corrected);
        Mat imageGray = new Mat();
        Imgproc.cvtColor(light_corrected, imageGray, Imgproc.COLOR_BGR2GRAY);

        //blackhat
        Mat blackHat = blackHatRun(imageGray);
        Mat contrast = new Mat();
        Core.subtract(imageGray, blackHat, contrast);

        // Blur image to enhance contrast
        Mat removeNoise = new Mat();
        Imgproc.medianBlur(contrast, removeNoise, 19);
        Imgproc.GaussianBlur(removeNoise, removeNoise, new Size(23, 23), 2, 2);

        //Apply canny edge
        int threshold = 20;
        Mat edges = new Mat();
        Imgproc.Canny(removeNoise, edges, threshold, threshold * 3);

        // Find circles inside canny edges
        int iCannyUpperThreshold = 50;
        Mat circles = new Mat();
        Imgproc.HoughCircles(edges, circles, Imgproc.CV_HOUGH_GRADIENT,
                2.0, removeNoise.rows() / 4, iCannyUpperThreshold, 50,
                frame.cols() / 5, frame.cols() / 3);


        if (circles.cols() > 0) {
            Point pt_iris = new Point(10000, 10000);
            int radius_iris = 0;
            //choose the circle more promising
            for (int x = 0; x < circles.cols(); x++) {
                double vCircle[] = circles.get(0, x);

                if (vCircle == null)
                    break;

                Point pt = new Point(Math.round(vCircle[0]), Math.round(vCircle[1]));
                int radius = (int) Math.round(vCircle[2]);
                if (Math.abs(pt.x - this.image.cols() / 2) < Math.abs(pt_iris.x - this.image.cols() / 2) &&
                        Math.abs(pt.y - this.image.rows() / 2) < Math.abs(pt_iris.y - this.image.rows() / 2)) {
                    pt_iris = pt;
                    radius_iris = radius;
                }

            }

            // draw the found circle
            int iLineThickness = 2;
            Imgproc.circle(frame, pt_iris, radius_iris, new Scalar(0, 255, 0), iLineThickness);

            // crop iris region
            Mat mask = new Mat(light_corrected.rows(), light_corrected.cols(), CvType.CV_8U, Scalar.all(0));
            Imgproc.circle(mask, pt_iris, radius_iris, new Scalar(255, 255, 255), -1, 8, 0);
            Mat masked = new Mat();
            light_corrected.copyTo(masked, mask);
            Mat thresh = new Mat();
            Imgproc.threshold(mask, thresh, 1, 255, Imgproc.THRESH_BINARY);
            List<MatOfPoint> contours = new ArrayList<>();
            Imgproc.findContours(thresh, contours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
            Rect rect = Imgproc.boundingRect(contours.get(0));
            Mat cropped = masked.submat(rect);

            //preprocess and apply binary threshold
            Mat cropped_gray = new Mat();
            Imgproc.cvtColor(cropped, cropped_gray, Imgproc.COLOR_BGR2GRAY);
            Imgproc.GaussianBlur(cropped_gray, cropped_gray, new Size(5, 5), 2, 2);
            Mat iris_thresh = new Mat();
            Imgproc.threshold(cropped_gray, iris_thresh, 100, 255, Imgproc.THRESH_BINARY);

            //remove noise from resulting mask
            int erosion_size = 1;
            int dilation_size = 1;
            Mat element = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(2 * erosion_size + 1, 2 * erosion_size + 1));
            Imgproc.erode(iris_thresh, iris_thresh, element);
            Imgproc.erode(iris_thresh, iris_thresh, element);
            Mat element1 = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(2 * dilation_size + 1, 2 * dilation_size + 1));
            Imgproc.dilate(iris_thresh, iris_thresh, element1);
            Imgproc.dilate(iris_thresh, iris_thresh, element1);

            //get left side of iris
            Rect roi_left = new Rect(0, 0, cropped.cols() / 2, cropped.rows() - 1);
            Mat left_side = new Mat(iris_thresh, roi_left);

            // get right side iris
            Rect roi_right = new Rect(Math.round(cropped.cols() / 2 - 1), 0, cropped.cols() / 2, cropped.rows() - 1);
            Mat right_side = new Mat(iris_thresh, roi_right);

            //count number of pixels in non-black region
            int non_shadow_left = Core.countNonZero(left_side);
            int non_shadow_right = Core.countNonZero(right_side);

            //compute the rate of the shadow
            double shadow_rate = Math.abs(
                    ((float) non_shadow_left - (float) non_shadow_right)
                            / ((float) non_shadow_left + (float) non_shadow_right) / 2)
                    * 100;

            this.shadow_size = shadow_rate;
            this.image = frame;
        }else{
            this.image = frame;
            shadow_size = 0.0;
        }
    }

    public double getShadowSize(){
        return this.shadow_size;
    }

}
