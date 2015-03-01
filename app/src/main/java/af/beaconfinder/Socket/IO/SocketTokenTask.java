package af.beaconfinder.Socket.IO;

import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;

/**
 * Created by hugo on 25/02/15.
 */

public class SocketTokenTask extends AsyncTask<String, Void, Boolean> {

    private static final String TAG = "SocketTokenTask";
    private String token = null;

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    private OnTaskComplete onTaskComplete;

    public interface OnTaskComplete {
        public void completed(String token);
    }

    public void setMyTaskCompleteListener(OnTaskComplete onTaskComplete) {
        this.onTaskComplete = onTaskComplete;
    }

    @Override
    protected Boolean doInBackground(String... urls) {
        try {

            final URL url = new URL(urls[0]);
            HttpPost httppost = new HttpPost(urls[0]);
            HttpClient httpclient = new DefaultHttpClient();

            httppost.setHeader("username", "android");
            httppost.setHeader("password", "android");
            HttpResponse response = httpclient.execute(httppost);

            // StatusLine stat = response.getStatusLine();
            int status = response.getStatusLine().getStatusCode();

            if (status == 200) {
                HttpEntity entity = response.getEntity();
                String data = EntityUtils.toString(entity);
                JSONObject json = new JSONObject(data);
                Log.d(TAG, json.toString());
                this.token = json.getString("token");
                return true;
            }


        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {

            e.printStackTrace();
        }
        return false;
    }

    protected void onPostExecute(Boolean result) {
        onTaskComplete.completed(token);
    }
}

