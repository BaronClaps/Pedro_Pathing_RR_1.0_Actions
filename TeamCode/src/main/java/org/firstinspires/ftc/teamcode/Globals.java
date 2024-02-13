package org.firstinspires.ftc.teamcode;

import com.acmerobotics.dashboard.config.Config;
import com.acmerobotics.roadrunner.Pose2d;

import org.firstinspires.ftc.teamcode.pipeline.AlliancePosition;
import org.firstinspires.ftc.teamcode.pipeline.FieldPosition;
import org.firstinspires.ftc.teamcode.pipeline.Side;

@Config
public class Globals {

    public static AlliancePosition COLOR = AlliancePosition.RED;
    public static FieldPosition FIELD = FieldPosition.NEAR;
    /**
     * Match constants.
     */
    public static Side SIDE = Side.LEFT;
    public static boolean IS_AUTO = false;
    public static boolean IS_USING_IMU = true;
    public static boolean USING_DASHBOARD = false;

    public static int OUTTAKE_SLIDE_POSITION = 0;

    public static boolean RUN_AUTO = false;

    public static String FRONT_WEBCAM_NAME = "Webcam 1";
    public static String REAR_WEBCAM_NAME = "Webcam 2";

    public static Side PRELOAD = Side.LEFT;

    /**
     * Robot state constants.
     */

    public static Pose2d drivePose = new Pose2d(0,0,0);
}
