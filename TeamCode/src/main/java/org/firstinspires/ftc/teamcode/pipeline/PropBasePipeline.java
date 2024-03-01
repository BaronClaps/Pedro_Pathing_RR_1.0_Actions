package org.firstinspires.ftc.teamcode.pipeline;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.Log;

import com.acmerobotics.dashboard.config.Config;

import org.firstinspires.ftc.robotcore.external.function.Consumer;
import org.firstinspires.ftc.robotcore.external.function.Continuation;
import org.firstinspires.ftc.robotcore.external.stream.CameraStreamSource;
import org.firstinspires.ftc.robotcore.internal.camera.calibration.CameraCalibration;
import org.firstinspires.ftc.teamcode.Globals;
import org.firstinspires.ftc.teamcode.utils.software.MovingArrayList;
import org.firstinspires.ftc.vision.VisionProcessor;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.concurrent.atomic.AtomicReference;

@Config
public class PropBasePipeline implements VisionProcessor, CameraStreamSource {
    private final AtomicReference<Bitmap> lastFrame = new AtomicReference<>(Bitmap.createBitmap(1, 1, Bitmap.Config.RGB_565));

    private Side location = Side.CENTER;

    public Rect sideZoneArea;
    public Rect centerZoneArea;

    private Mat finalMat = new Mat();

    public int blueLeftX = 280;
    public int blueLeftY = 40;

    public int blueCenterX = 545;
    public int blueCenterY = 0;

    public int redLeftX = 110;
    public int redLeftY = 25;

    public int redCenterX = 400;
    public int redCenterY = 0;

    public int width = 170;
    public int height = 128;

    public double blueThreshold = 1.75;
    public double redThreshold = 1.75;

    public double threshold = 0.0;

    public double redDeltaThreshold = 0.8;

    public double blueDeltaThreshold = 0.8;

    public double sideColor = 0.0;
    public double centerColor = 0.0;

    public double meanSideColor = 0.0;
    public double meanCenterColor = 0.0;

    public double maxSideColor = 0.0;
    public double maxCenterColor = 0.0;

    public Scalar side = new Scalar(0,0,0);
    public Scalar center = new Scalar(0,0,0);

    protected MovingArrayList sideZoneColorList;
    protected MovingArrayList centerZoneColorList;

    private long startTime;
    public long elapsedTime;

    public int arraySize = 0;

    public AlliancePosition allianceColor;

    // BGR
    private final Scalar
            YELLOW  = new Scalar(255, 255, 0),
            BLUE  = new Scalar(255, 0, 0),
            GREEN = new Scalar(0, 255, 0),
            RED = new Scalar(0, 255, 0);

    @Override
    public void init(int width, int height, CameraCalibration calibration) {
        if(allianceColor ==null) allianceColor = Globals.COLOR;
        sideZoneColorList = new MovingArrayList(10);
        centerZoneColorList = new MovingArrayList(10);

        startTime = System.currentTimeMillis();
    }

    @Override
    public Mat processFrame(Mat frame, long captureTimeNanos) {
        allianceColor = Globals.COLOR;
        if (allianceColor == AlliancePosition.RED) {
            threshold = redThreshold;
        } else {
            threshold = blueThreshold;
        }

        frame.copyTo(finalMat);
        Imgproc.GaussianBlur(finalMat, finalMat, new Size(5, 5), 0.0);

        sideZoneArea = new Rect(allianceColor == AlliancePosition.RED? redLeftX : blueLeftX,
                allianceColor == AlliancePosition.RED? redLeftY : blueLeftY, width, height);
        centerZoneArea = new Rect(Globals.COLOR == AlliancePosition.RED?redCenterX:blueCenterX,
                allianceColor == AlliancePosition.RED?redCenterY:blueCenterY, width, height);

        Mat sideZone = finalMat.submat(sideZoneArea);
        Mat centerZone = finalMat.submat(centerZoneArea);

        side = Core.sumElems(sideZone);
        center = Core.sumElems(centerZone);

        if(allianceColor == AlliancePosition.RED) {
            sideColor = side.val[2] / 1000000.0;
            centerColor = center.val[2] / 1000000.0;
        } else {
            sideColor = side.val[0] / 1000000.0;
            centerColor = center.val[0] / 1000000.0;
        }

        if(centerZoneColorList.getArrayList().isEmpty()) {
            startTime = System.currentTimeMillis();
        }
        sideZoneColorList.add(sideColor);
        centerZoneColorList.add(centerColor);

        arraySize = centerZoneColorList.getArrayList().size();
        if(arraySize == 8) {
            elapsedTime = System.currentTimeMillis() - startTime;
        }

        meanSideColor = sideZoneColorList.getMean();
        meanCenterColor = centerZoneColorList.getMean();

        if(meanSideColor > maxSideColor) {
            maxSideColor = meanSideColor;
        }

        if(meanCenterColor > maxCenterColor) {
            maxCenterColor = meanCenterColor;
        }

        if(allianceColor == AlliancePosition.BLUE){
            if ( meanSideColor < threshold ||
                    (meanCenterColor - meanSideColor > blueDeltaThreshold &&
                            meanSideColor < 7.0 && meanCenterColor < 7.0)) {
                // left zone has it
                location = Side.LEFT;
                Imgproc.rectangle(frame, sideZoneArea, GREEN, 8);
                Imgproc.rectangle(frame, centerZoneArea, YELLOW, 3);
            } else if (meanCenterColor < threshold ||
                    (meanSideColor - meanCenterColor > blueDeltaThreshold &&
                            meanSideColor < 7.0 && meanCenterColor < 7.0) ) {
                // center zone has it
                location = Side.CENTER;
                Imgproc.rectangle(frame, centerZoneArea, GREEN, 8);
                Imgproc.rectangle(frame, sideZoneArea, YELLOW, 3);
            } else {
                // right zone has it
                location = Side.RIGHT;
                Imgproc.rectangle(frame, sideZoneArea, YELLOW, 3);
                Imgproc.rectangle(frame, centerZoneArea, YELLOW, 3);
            }
        } else {
            if ( (meanCenterColor < threshold && meanSideColor >threshold) ||
                    (meanCenterColor < threshold && meanCenterColor < meanSideColor) ||
                    (meanSideColor - meanCenterColor > redDeltaThreshold &&
                    meanSideColor < 7.0 && meanCenterColor < 7.0) //||
//                    (meanCenterColor < maxCenterColor - threshold )
            ) {
                // center zone has it
                location = Side.CENTER;
                Imgproc.rectangle(frame, centerZoneArea, GREEN, 8);
                Imgproc.rectangle(frame, sideZoneArea, YELLOW, 3);
            }
            else if ( (meanSideColor < threshold && meanCenterColor >threshold) ||
                    (meanSideColor < threshold && meanSideColor < meanCenterColor)  ||
                    (meanCenterColor - meanSideColor > redDeltaThreshold
                    && meanSideColor < 7.0 && meanCenterColor < 7.0 ) //||
//                    (meanSideColor < maxSideColor - threshold)
            ) {
                // left zone has it
                location = Side.LEFT;
                Imgproc.rectangle(frame, sideZoneArea, GREEN,8);
                Imgproc.rectangle(frame, centerZoneArea, YELLOW, 3);
            } else {
                // right zone has it
                location = Side.RIGHT;
                Imgproc.rectangle(frame, sideZoneArea, YELLOW, 3);
                Imgproc.rectangle(frame, centerZoneArea, YELLOW, 3);
            }
        }

//        Log.d("PropDetection_logger", "meanCenter: " + meanCenterColor + " | " + meanSideColor + " | " + (meanSideColor - meanCenterColor) + " | " + redDeltaThreshold
//        + " | " + (meanSideColor - meanCenterColor > redDeltaThreshold &&
//                        meanSideColor < 7.0 && meanCenterColor < 7.0)
//         );

        sideZone.release();
        centerZone.release();

        return frame;
    }

    @Override
    public void onDrawFrame(Canvas canvas, int onscreenWidth, int onscreenHeight, float scaleBmpPxToCanvasPx, float scaleCanvasDensity, Object userContext) {

    }

    public Side getLocation() {
        return this.location;
    }

    @Override
    public void getFrameBitmap(Continuation<? extends Consumer<Bitmap>> continuation) {
        continuation.dispatch(bitmapConsumer -> bitmapConsumer.accept(lastFrame.get()));
    }
}
