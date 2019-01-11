package com.example.rohan.kikoo;

import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.os.Bundle;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;
import org.opencv.android.*;
import org.opencv.core.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

import org.opencv.imgproc.Imgproc;
import org.tensorflow.Graph;
import org.tensorflow.Operation;
import org.tensorflow.contrib.android.TensorFlowInferenceInterface;

public class DetectLabel extends Activity implements CameraBridgeViewBase.CvCameraViewListener2 {

    private static final Logger LOGGER = new Logger();
    private static final int MAX_RESULTS = 100;
    private boolean logStats = false;

    private Mat mRgba;
    private Mat mGray;

    int count = 0;
    int height =360;
    int width = 640;
    int left,right,top,bottom;
    int memory,last_placed;
    Rect temp_rect;
    List<Rect> rects;


    int[] intValues = new int[height*width];
    byte[] byteValues = new byte[height*width*3];

    TensorFlowInferenceInterface inferenceInterface;

    AssetManager assetManager;
    final String modelFilename = "file:///android_asset/frozen_inference_graph14k.pb";
    String labelFilename = "file:///android_asset/labels";

    Vector<String> labels = new Vector<String>();

    String inputName = "image_tensor";

    String[] outputNames = new String[] {"detection_boxes", "detection_scores",
            "detection_classes", "num_detections"};

    private float[] outputLocations = new float[MAX_RESULTS*4];
    private float[] outputScores = new float[MAX_RESULTS];
    private float[] outputClasses = new float[MAX_RESULTS];
    private List<Integer> validOutputClasses = new ArrayList<>();
    private float[] outputNumDetections = new float[1];

    Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

    private CameraBridgeViewBase mOpenCvCameraView;



    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    System.out.println("OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();


                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };

    public void MainActivity() {

    }

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle extras) {
        System.out.println("called onCreate");
        super.onCreate(extras);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_detect);

        assetManager = getAssets();
        InputStream labelsInput = null;
        String actualFilename = labelFilename.split("file:///android_asset/")[1];
        try {
            labelsInput = assetManager.open(actualFilename);
            BufferedReader br = null;
            br = new BufferedReader(new InputStreamReader(labelsInput));
            String line;
            while ((line = br.readLine()) != null) {
                LOGGER.w(line);
                labels.add(line);
            }
            br.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        inferenceInterface = new TensorFlowInferenceInterface(assetManager, modelFilename);


        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.DetectActivity);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            System.out.println("Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            System.out.println("OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    public void onCameraViewStarted(int width, int height) {

    }

    public void onCameraViewStopped() {
    }


    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {

        count++;


        if (count == 20) {
            count = 0;

            validOutputClasses.clear();

            mRgba = inputFrame.rgba();
            Utils.matToBitmap(mRgba, bmp);

            byteValues = ByteValues(bmp,intValues);

            //for(int i = 0;i<byteValues.length;i++)
                //System.out.println(byteValues.);

            inferenceInterface.feed(inputName, byteValues, 1,height, width, 3);
            inferenceInterface.run(outputNames, logStats);

            outputLocations = new float[MAX_RESULTS * 4];
            outputScores = new float[MAX_RESULTS];
            outputClasses = new float[MAX_RESULTS];
            outputNumDetections = new float[1];
            inferenceInterface.fetch(outputNames[0], outputLocations);
            inferenceInterface.fetch(outputNames[1], outputScores);
            inferenceInterface.fetch(outputNames[2], outputClasses);
            inferenceInterface.fetch(outputNames[3], outputNumDetections);

            for (int i = 0; i < outputScores.length; ++i) {
                if (outputScores[i] > 0.80) {

                    validOutputClasses.add((int) outputClasses[i]);
                    left = (int) (outputLocations[(4*i)+1]*width);
                    right = (int) (outputLocations[(4*i)+3]*width);
                    top = (int) (outputLocations[4*i]*height);
                    bottom = (int) (outputLocations[(4*i)+2]*height);
                    System.out.println(left+" "+right+" "+top+" "+bottom);
                    temp_rect = new Rect(left,top,(right - left),(top - bottom));
                    Imgproc.rectangle(mRgba, new Point(temp_rect.x, temp_rect.y), new Point(temp_rect.x + temp_rect.width, temp_rect.y + temp_rect.height), new Scalar(255, 0, 0, 255), 3);

                }
            }

            /*for(int i = 0; i < validOutputClasses.toArray().length; i++)
            {
                System.out.println(labels.get(validOutputClasses.get(i)));
            }

            for (int i = 0; i < outputScores.length; ++i) {
                if (outputScores[i] > 0.60)
                   System.out.println(labels.get((int) outputClasses[i]));
            }*/
            final float classID = getClassID();
            if (classID != 0)
                start_audio(classID);


            return mRgba;
        }
        else
            return null;

    }



    public byte[] ByteValues(Bitmap bm, int[] intVal)
    {
        byte[] byteVal = new byte[height*width*3];
        bm.getPixels(intVal, 0, bm.getWidth(), 0, 0, bm.getWidth(), bm.getHeight());
        System.out.println("The bitmap size is"+bm.getWidth()+"and"+bm.getHeight());

        for (int i = 0; i < intVal.length; ++i) {
            byteVal[i * 3 + 2] = (byte) (intVal[i] & 0xFF);
            byteVal[i * 3 + 1] = (byte) ((intVal[i] >> 8) & 0xFF);
            byteVal[i * 3 + 0] = (byte) ((intVal[i] >> 16) & 0xFF);
        }

        return byteVal;
    }

    public void start_audio(float param)
    {
            Intent intent = new Intent(DetectLabel.this, AudioService.class);
            Bundle b = new Bundle();
            b.putFloat("key", param); //Your id
            intent.putExtras(b); //Put your id to your next Intent
            startService(intent);
            finish();
    }

    public float getClassID()
    {
        System.out.println(validOutputClasses.toArray().length);
        if (validOutputClasses.toArray().length == 3) {
            float temp = 0;
            for (int i = 0; i < validOutputClasses.toArray().length; i++)
                temp = temp + validOutputClasses.get(i);
            temp = temp/3;
            return temp;
        }
        else if (validOutputClasses.toArray().length > 3)
            return -1;

        else
            return 0;
    }
}