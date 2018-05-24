package com.example.andre.grafar;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PointF;
import android.graphics.drawable.BitmapDrawable;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.dlazaro66.qrcodereaderview.QRCodeReaderView;

import org.json.JSONException;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;


public class DecoderActivity extends AppCompatActivity implements QRCodeReaderView.OnQRCodeReadListener {

    private RequestQueue requestQueue;
    private ViewGroup mainLayout;
    private TextView resultTextView;
    private QRCodeReaderView qrCodeReaderView;
    private CheckBox flashlightCheckBox;
    private CheckBox enableDecodingCheckBox;
    private PointsOverlayView pointsOverlayView;
    private String urlPost = "https://grafar.herokuapp.com/api/data";
    private String[] arrayInput;
    private Bitmap bitmap;
    private byte[] byteImage;
    private ImageView resultFunc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_decoder);

        mainLayout = findViewById(R.id.main_layout);
        resultFunc = findViewById(R.id.cameraView);
        initQRCodeReaderView();


    }

    private void initQRCodeReaderView() {
        View content = getLayoutInflater().inflate(R.layout.content_decoder, mainLayout, true);

        qrCodeReaderView = content.findViewById(R.id.qrdecoderview);
        resultTextView = content.findViewById(R.id.result_text_view);
        flashlightCheckBox = content.findViewById(R.id.flashlight_checkbox);
        enableDecodingCheckBox = content.findViewById(R.id.enable_decoding_checkbox);
        pointsOverlayView = content.findViewById(R.id.points_overlay_view);

        qrCodeReaderView.setAutofocusInterval(2000L);
        qrCodeReaderView.setOnQRCodeReadListener(this);
        qrCodeReaderView.setBackCamera();
        flashlightCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                qrCodeReaderView.setTorchEnabled(isChecked);
            }
        });
        enableDecodingCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                qrCodeReaderView.setQRDecodingEnabled(isChecked);
            }
        });
        qrCodeReaderView.startCamera();

    }

    @Override
    public void onQRCodeRead(String text, PointF[] points) {
        resultTextView.setText(text);

        pointsOverlayView.setPoints(points);
        qrCodeReaderView.setQRDecodingEnabled(false);
        qrCodeReaderView.stopCamera();
        sendRequest(text);
        //Intent intent = new Intent(getApplicationContext(),MainActivity.class);
        //intent.putExtra("data",text);
        //Intent camIntent = new Intent(getApplicationContext(),CameraView.class);
        //startActivity(camIntent);
        //startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        qrCodeReaderView.startCamera();
    }

    @Override
    protected void onPause() {
        super.onPause();
        qrCodeReaderView.stopCamera();
        Log.d("pause", "camera paused");
    }

    public void sendRequest(String text) {
        arrayInput = text.split(",");
        StringRequest graphRequest = null;
        JSONObject jsonBody = new JSONObject();
        jsonBody.put("function",arrayInput[0]);
        jsonBody.put("a",Integer.parseInt(arrayInput[1]));
        jsonBody.put("b",Integer.parseInt(arrayInput[2]));
        final String requestBody = jsonBody.toString();
        requestQueue = Volley.newRequestQueue(this);
        graphRequest = new StringRequest(Request.Method.POST, urlPost,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("res", response);

                        JSONParser parser = new JSONParser();
                        JSONObject json = null;
                        String message = "";
                        try {
                            json = (JSONObject) parser.parse(response);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        message = (String) json.get("message");

                        Log.d("tagm",message);
                        byteImage = decodeBase64Image(message);
                        //bitmap = decodeBase64Image(message);
                        Log.d("tagbitmap",byteImage.toString());
                        Intent intent = new Intent(getApplicationContext(),CameraView.class);
                        intent.putExtra("byteArray",byteImage);
                        startActivity(intent);

                        //resultFunc.setImageBitmap(bitmap);
                        //Toast.makeText(getApplicationContext(),response,Toast.LENGTH_LONG).show();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("error", error.toString());
            }
        }
        ) {
            @Override
            public String getBodyContentType() {
                return "application/json";
            }

            @Override
            public byte[] getBody() throws AuthFailureError {
                try {
                    Log.d("tag",requestBody.toString());
                    return requestBody == null ? null : requestBody.getBytes("utf-8");

                }catch (UnsupportedEncodingException uee){
                    VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s", requestBody, "utf-8");
                    return null;
                }
            }
        };

        requestQueue.add(graphRequest);
    }



    private String encodeBase64Image(ImageView image){
        BitmapDrawable bitmapDrawable = (BitmapDrawable) image.getDrawable();
        Bitmap bitmap = bitmapDrawable.getBitmap();

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        return Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.DEFAULT);
    }

    private byte[] decodeBase64Image(String image){
        byte[] decodeImage = Base64.decode(image, Base64.DEFAULT);
        return decodeImage;
        //return BitmapFactory.decodeByteArray(decodeImage, 0, decodeImage.length);
    }

}
