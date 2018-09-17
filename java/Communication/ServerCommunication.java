package Communication;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Base64;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

public class ServerCommunication extends AsyncTask<Void, Void, Void> {

    private Bitmap image;
    private String name;
    private String diagnose_report;
    private boolean success = false;
    private static final String SERVER_ADDRESS = "https://glauvision.000webhostapp.com/";

    public ServerCommunication(Bitmap image, String name, String diagnose_report) {
        this.image = image;
        this.name = name;
        this.diagnose_report = diagnose_report;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        this.success = true;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        String encodedImage = Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.DEFAULT);

        ArrayList<NameValuePair> dataToSend = new ArrayList<>();
        dataToSend.add(new BasicNameValuePair("image", encodedImage));
        dataToSend.add(new BasicNameValuePair("name", name));
        dataToSend.add(new BasicNameValuePair("diagnose_report", diagnose_report));

        HttpParams httpRequestParams = getHttpRequestParams();

        HttpClient client = new DefaultHttpClient(httpRequestParams);
        HttpPost post = new HttpPost(SERVER_ADDRESS + "SavePicture.php");

        try{
            post.setEntity(new UrlEncodedFormEntity(dataToSend));
            client.execute(post);
        }catch (Exception e){
            e.printStackTrace();
            //Failmsg
        }

        return null;
    }


    private HttpParams getHttpRequestParams(){
        HttpParams httpRequestParams = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpRequestParams, 1000);
        HttpConnectionParams.setSoTimeout(httpRequestParams, 1000);
        return httpRequestParams;
    }

    public boolean isSuccess() {
        return success;
    }
}