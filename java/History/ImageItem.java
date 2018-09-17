package History;

import android.graphics.Bitmap;

public class ImageItem {
    private Bitmap image;
    private String title;
    private String date;
    private String size_shadow;

    public ImageItem(Bitmap image, String title, String date, String size_shadow) {
        super();
        this.image = image;
        this.title = title;
        this.date = date;
        this.size_shadow = size_shadow;
    }

    public Bitmap getImage() {
        return image;
    }

    public void setImage(Bitmap image) {
        this.image = image;
    }

    public String getTitle() { return title; }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDate() { return date; }

    public void setDate(String date) { this.date = date; }

    public String getSizeShadow() { return size_shadow; }

    public void setSizeShadow(String size) { this.size_shadow = size; }

}