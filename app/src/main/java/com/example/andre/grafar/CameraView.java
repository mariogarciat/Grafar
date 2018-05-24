package com.example.andre.grafar;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.widget.ImageView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

public class CameraView extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2{

    private static String TAG = "MainActivity";
    private JavaCameraView javaCameraView;
    private Mat mRgba, matBipmap, mRgbaF, mRgbaT;
    private Bitmap bitmap;
    private byte[] byteImage;
    BaseLoaderCallback baseLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status){
                case BaseLoaderCallback.SUCCESS:{
                    javaCameraView.enableView();
                    break;
                }
                default:{
                    super.onManagerConnected(status);
                    break;
                }
            }
            super.onManagerConnected(status);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_view);

        javaCameraView = findViewById(R.id.cameraView);
        javaCameraView.setVisibility(SurfaceView.VISIBLE);
        javaCameraView.setCvCameraViewListener(this);
        byteImage = getIntent().getByteArrayExtra("byteArray");
        Log.d("tagbyte",byteImage.toString());
        bitmap = BitmapFactory.decodeByteArray(byteImage,0,byteImage.length);
        Log.d("tagbm",bitmap.toString());
        matBipmap = new Mat();
        Utils.bitmapToMat(bitmap,matBipmap);
        Log.d("cols",""+matBipmap.cols());
        Log.d("rows",""+matBipmap.rows());
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(javaCameraView != null){
            javaCameraView.disableView();

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(javaCameraView != null){
            javaCameraView.disableView();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(OpenCVLoader.initDebug()){
            Log.i(TAG,"Opencv loaded succesfully");
            baseLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }else{
            Log.i(TAG,"Opencv not loaded");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_4_0,this,baseLoaderCallback);
        }
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        mRgba = new Mat(height,width, CvType.CV_8UC4);

        //mRgbaF = new Mat(height,width, CvType.CV_8UC4);
        //mRgbaT = new Mat(height,width, CvType.CV_8UC4);
    }

    @Override
    public void onCameraViewStopped() {
        mRgba.release();

        //mRgbaF.release();
        //mRgbaT.release();
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {

        mRgba = inputFrame.rgba();
        Log.d("colsback",""+mRgba.cols());
        Log.d("rowsback",""+mRgba.rows());
        Mat newmRgba = overlap(mRgba,matBipmap);
        Log.d("mRgba",mRgba.toString());
        return newmRgba;
        /*Core.transpose(mRgba, mRgbaT);
        Imgproc.resize(mRgbaT, mRgbaF, mRgbaF.size(), 0,0, 0);
        Core.flip(mRgbaF, mRgba, 1 );*/
    }

    private Mat overlap(Mat matBack, Mat matFore){

        Mat mask2
                = new Mat( new Size( matFore.cols(), matFore.rows() ),
                CvType.CV_8U);
        mask2.setTo( new Scalar( 0 ) );

        Size sizeMask = mask2.size();
        double[] data;

        for (int i=0;i<sizeMask.height/2;i++ )  // making the upper left quarter
            for (int j=0;j<sizeMask.width/2;j++ )  //of the whole mask image
                mask2.put(i, j, 10);

        /*for (int i=0;i<sizeMask.height;i++ )
            for (int j=0;j<sizeMask.width;j++ ) {
                data = matFore.get(i, j);
                if (data[0] + data[1] + data[2] > 0)
                    mask2.put(i, j, 10);
            }*/
        matFore.copyTo(matBack, mask2);
        return matBack;

    }
}
