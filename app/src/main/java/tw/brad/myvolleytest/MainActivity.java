package tw.brad.myvolleytest;

import android.graphics.Bitmap;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.android.volley.AuthFailureError;
import com.android.volley.Cache;
import com.android.volley.Network;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private final String url = "http://www.bradchao.com";
    private RequestQueue queue, mRequestQueue;
    private ImageView img;

    private File downloadPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        queue = Volley.newRequestQueue(this);

        // 進階調整 RequestQueue 的快取空間
        // Instantiate the cache
        Cache cache = new DiskBasedCache(getCacheDir(), 1024 * 1024); // 1MB cap

        // Set up the network to use HttpURLConnection as the HTTP client.
        Network network = new BasicNetwork(new HurlStack());

        // Instantiate the RequestQueue with the cache and network.
        mRequestQueue = new RequestQueue(cache, network);

        // Start the queue
        mRequestQueue.start();


        img = findViewById(R.id.img);

        downloadPath = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS
        );

    }

    // 透過 Volley 連接網際網路
    public void test1(View view) {
        StringRequest stringRequest = new StringRequest(
                Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.v("brad", response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.v("brad", "ERROR");
            }
        }
        );

        mRequestQueue.add(stringRequest);


    }

    // 範例: 擷取農委會的 OpenData 資料
    public void test2(View view) {
        StringRequest stringRequest = new StringRequest(
                Request.Method.GET,
                "http://data.coa.gov.tw/Service/OpenData/ODwsv/ODwsvTravelFood.aspx",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.v("brad", response);
                        parseJSON(response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.v("brad", "ERROR:" + error.getMessage());
            }
        }
        );

        mRequestQueue.add(stringRequest);

    }

    private void parseJSON(String stringJSON){

        try {
            JSONArray root = new JSONArray(stringJSON);
            for (int i=0; i<root.length(); i++){
                JSONObject data = root.getJSONObject(i);
                String name = data.getString("Name");
                String address = data.getString("Address");
                Log.v("brad", name + ":" + address);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }


    }

    // 擷取網際網路的影像資料
    public void test3(View view) {
        ImageRequest imageRequest =
                new ImageRequest("https://i2.wp.com/www.bradchao.com/wp-content/uploads/2018/01/IMG_20180108_113633-3.jpg?w=1024&ssl=1",
                        new Response.Listener<Bitmap>() {
                            @Override
                            public void onResponse(Bitmap response) {
                                img.setImageBitmap(response);
                            }
                        },0,0,
                        ImageView.ScaleType.CENTER,
                        Bitmap.Config.ARGB_8888, null);
        mRequestQueue.add(imageRequest);
    }

    // 以 POST 方式傳遞資料
    public void test4(View view) {
        String postUrl = "http://www.bradchao.com/iii/brad02.php";

        StringRequest stringRequest = new StringRequest(Request.Method.POST,
                postUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.v("brad", response);
                    }
                }, null) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String, String> params = new HashMap<>();
                params.put("account", "brad");
                params.put("passwd", "123456");

                return params;
            }
        };

        mRequestQueue.add(stringRequest);


    }

    // 透過 Volley 的 POST 上傳檔案
    public void test5(View view){
        String postUrl = "http://www.bradchao.com/iii/brad03.php";

        File uploadFile = new File(downloadPath, "www.bradchao.com.pdf");
        final byte[] data = new byte[(int)uploadFile.length()];
        try {
            FileInputStream fin = new FileInputStream(uploadFile);
            fin.read(data);
            fin.close();
        } catch (Exception e) {
            e.printStackTrace();
        }


        VolleyMultipartRequest multipartRequest =
                new VolleyMultipartRequest(Request.Method.POST,
                        postUrl, new Response.Listener<NetworkResponse>() {
                    @Override
                    public void onResponse(NetworkResponse response) {
                        Log.v("brad", "OK");
                    }
                }, null){

                    @Override
                    protected Map<String, DataPart> getByteData() throws AuthFailureError {
                        HashMap<String,DataPart> params = new HashMap<>();
                        params.put("upload", new DataPart("brad4.pdf", data));


                        return params;
                    }
                };

        mRequestQueue.add(multipartRequest);
    }

}
