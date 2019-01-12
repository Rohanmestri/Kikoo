package com.example.rohan.kikoo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.WindowManager;
import org.opencv.android.*;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import static android.content.ContentValues.TAG;

public class MainActivity extends Activity implements CameraBridgeViewBase.CvCameraViewListener2 {

    private CameraBridgeViewBase mOpenCvCameraView;


    // These variables are used (at the moment) to fix camera orientation from 270degree to 0degree
    private Mat mRgba;
    private Mat mGray;

    Mat hierarchy;

    private File mCascadeFile;
    private CascadeClassifier mJavaDetector;

    int index;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i(TAG, "OpenCV loaded successfully");


                    try {
                        // load cascade file from application resources
                        InputStream is = getResources().openRawResource(
                                R.raw.kikoo_e6);
                        File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
                        mCascadeFile = new File(cascadeDir,
                                "kikoo_e6.xml");
                        FileOutputStream os = new FileOutputStream(mCascadeFile);

                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = is.read(buffer)) != -1) {
                            os.write(buffer, 0, bytesRead);
                        }
                        is.close();
                        os.close();


                        mJavaDetector = new CascadeClassifier(
                                mCascadeFile.getAbsolutePath());
                        if (mJavaDetector.empty()) {
                            Log.e(TAG, "Failed to load cascade classifier");
                            mJavaDetector = null;
                        } else
                            Log.i(TAG, "Loaded cascade classifier from "
                                    + mCascadeFile.getAbsolutePath());



                        cascadeDir.delete();

                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.e(TAG, "Failed to load cascade. Exception thrown: " + e);
                    }
                    mOpenCvCameraView.setCameraIndex(0);
                    mOpenCvCameraView.enableFpsMeter();
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
    public MainActivity() {

    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        System.out.println("called onCreate");
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_opencv);
        Intent myIntent = getIntent();
        index = myIntent.getIntExtra("key", 0);
        System.out.println("index value "+index);


        mOpenCvCameraView = (JavaCameraView) findViewById(R.id.HelloOpenCvView);

        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);

        mOpenCvCameraView.setCvCameraViewListener(this);

    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume()
    {
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
        System.out.println("Opencv is Destroyed");
    }

    public void onCameraViewStarted(int width, int height) {

    }

    public void onCameraViewStopped() {

    }


    int count = 0, boxes;
    int flag = 0;
    double area, rect_area;
    double extent, contourWidth, contourHeight,aspectratio;
    Rect rect,temp_rect;
    String text, num_string,temp;
    Mat crop, temp_frame;
    int[] correctAnswer = new int[]{2,3,1,1,2,1,2,1,3,1,1,3,3,2,2,2};



    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {

        mRgba = inputFrame.rgba();
        System.out.println("MAT "+ mRgba);
        mGray = inputFrame.gray();

        MatOfRect kikoo = new MatOfRect();
        List<Rect> sorted_boxes = new ArrayList();

        boxes = 0;
        count++;


        if (count == 20) {
            count = 0;

            //Imgproc.resize(mGray, mGray, new Size(mGray.cols() / scale, mGray.rows() / scale));
            //Imgproc.resize(mRgba, mRgba, new Size(mRgba.cols() / scale, mRgba.rows() / scale));
            temp_frame = mGray.clone();
            //Imgproc.Canny(mGray, mGray, 0, 100);
            Imgproc.adaptiveThreshold(mGray, mGray, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C,
                    Imgproc.THRESH_BINARY, 11, 2);

            sorted_boxes = define_boxes(mGray);
            detect_kikoo(sorted_boxes,kikoo);

            //Imgproc.resize(mGray, mGray, new Size(mRgba.cols() * scale, mRgba.rows() * scale));
            //Imgproc.resize(mRgba, mRgba, new Size(mRgba.cols() * scale, mRgba.rows() * scale));
            return mRgba;
        }
        else
            return null;


    }


    int minX,maxX,minY,maxY;
    public List<Rect> define_boxes(Mat temp_gray) {
        hierarchy = new Mat();
        List<MatOfPoint> contours = new ArrayList<>();
        List<Rect> rects = new ArrayList();
        List<Rect> Maxrect = new ArrayList();



        Imgproc.findContours(temp_gray, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);

        for (int contourIdx = 0; contourIdx < contours.size(); contourIdx++) {
            // Minimum size allowed for consideration
            MatOfPoint2f approxCurve = new MatOfPoint2f();
            MatOfPoint2f contour2f = new MatOfPoint2f(contours.get(contourIdx).toArray());
            //Processing on mMOP2f1 which is in type MatOfPoint2f
            double approxDistance = Imgproc.arcLength(contour2f, true) * 0.04;
            Imgproc.approxPolyDP(contour2f, approxCurve, approxDistance, true);

            if (approxCurve.total() == 4) {
                area = Imgproc.contourArea(contour2f);
                rect = Imgproc.boundingRect(contours.get(contourIdx));
                rect_area = rect.area();
                if(rect_area>50000 && rect_area<100000) {
                    Maxrect.add(rect);
                    minX = rect.x;
                    maxX = rect.x + rect.width;
                    minY = rect.y;
                    maxY = rect.y + rect.height;
                    System.out.println("coor "+minX+" "+maxX+" "+minY+" "+maxY);
                    Imgproc.drawContours(mRgba, contours, contourIdx, new Scalar(0, 0, 255), 1);
                }
            }

        }

        for (int contourIdx = 0; contourIdx < contours.size(); contourIdx++) {
            // Minimum size allowed for consideration
            MatOfPoint2f approxCurve = new MatOfPoint2f();
            MatOfPoint2f contour2f = new MatOfPoint2f(contours.get(contourIdx).toArray());
            //Processing on mMOP2f1 which is in type MatOfPoint2f
            double approxDistance = Imgproc.arcLength(contour2f, true) * 0.04;
            Imgproc.approxPolyDP(contour2f, approxCurve, approxDistance, true);

            if (approxCurve.total() == 4) {
                area = Imgproc.contourArea(contour2f);
                rect = Imgproc.boundingRect(contours.get(contourIdx));
                rect_area = rect.area();
                if((rect.x>minX)&&(rect.y>minY)&&(rect.x<maxX)&&(rect.y<maxY)) {

                    if (rect_area > 4000 && rect_area < 8000) {
                        rects.add(rect);
                        Imgproc.drawContours(mRgba, contours, contourIdx, new Scalar(0, 0, 255), 2);
                        String areatext = "" + rect_area;
                        Imgproc.putText(mRgba, areatext, new Point(rect.x, rect.y), 3, 1, new Scalar(255, 0, 0, 255), 1);
                    }
                }
            }

        }

        //sorting all the contours
        for (int i = 0; i < rects.size()-1; i++) {
            for (int j = 0; j < rects.size()-1; j++) {
                if (rects.get(j).x < rects.get(j + 1).x) {
                    temp_rect = rects.get(j);
                    rects.set(j, rects.get(j + 1));
                    rects.set(j + 1, temp_rect);
                }
            }
        }

        //System.out.println("rect detail "+rects);
        //System.out.println("nonDup detail "+nonDupRects);
        //System.out.println("boxes "+ nonDupRects.size());
        return rects;

    }

    public void detect_kikoo(List<Rect> temp, MatOfRect detect)
    {
        int detectedAnswer = 0;
        boolean foundKikoo = false;
        if (temp.size() == 3)
        {
            for (int i = 0; i < 3; i++) {
                crop = new Mat  (temp_frame, temp.get(i));

                if (mJavaDetector != null) {
                    mJavaDetector.detectMultiScale(crop, detect, 1.05, 0,
                            0, new Size(4, 4), new Size(100, 100));
                }

                num_string = String.valueOf(i + 1);

                if (detect.toArray().length != 0) {
                    foundKikoo = true;
                    text = new String("Kikoo was found in option ".concat(num_string));
                    System.out.println("no of detection="+detect.toArray().length);
                    detectedAnswer = (i+1);
                    Imgproc.putText(mRgba, text, new Point(10, 50), 3, 1, new Scalar(255, 255, 255, 255), 2);

                    if((detectedAnswer < 4) && (foundKikoo)) {
                        foundKikoo = false;
                        if (index <= 0) {
                            return;
                        }
                        if (correctAnswer[index - 1] == detectedAnswer) {
                            System.out.println("found kikoo in " + num_string);
                            Imgproc.putText(mRgba, "Correct Answer", new Point(10, 300), 3, 1, new Scalar(255, 255, 255, 255), 2);
                            Intent intent = new Intent(MainActivity.this, FeedbackService.class);
                            Bundle b = new Bundle();
                            b.putFloat("Key", 2); //Your id
                            b.putInt("label", index);
                            intent.putExtras(b); //Put your id to your next Intent
                            startService(intent);
                            //finish();
                        } else {
                            System.out.println("kikoo not found in " + num_string);
                            Imgproc.putText(mRgba, "Wrong Answer", new Point(10, 300), 3, 1, new Scalar(255, 255, 255, 255), 2);
                            Intent intent = new Intent(MainActivity.this, FeedbackService.class);
                            Bundle b = new Bundle();
                            b.putFloat("Key", 1);
                            b.putInt("label", index);//Your id
                            intent.putExtras(b); //Put your id to your next Intent
                            startService(intent);
                            //finish();

                        }
                    }
                }
                if (detect.toArray().length == 0){
                    System.out.println("value inside0 "+(i+1));
                    Imgproc.putText(mRgba, "Kikoo not Found", new Point(10, 50), 3, 1, new Scalar(255, 255, 255, 255), 2);

                }
            }
        }
    }
}



