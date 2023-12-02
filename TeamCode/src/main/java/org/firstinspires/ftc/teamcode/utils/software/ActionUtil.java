package org.firstinspires.ftc.teamcode.utils.software;

import android.util.Log;

import com.acmerobotics.dashboard.canvas.Canvas;
import com.acmerobotics.dashboard.telemetry.TelemetryPacket;
import com.acmerobotics.roadrunner.Action;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.RobotLog;

public class ActionUtil {
   public static class DcMotorExPowerAction implements Action {
      double power;
      DcMotorEx motor;

      public DcMotorExPowerAction(DcMotorEx motor, double power) {
         this.power = power;
         this.motor = motor;
      }

      @Override
      public boolean run(TelemetryPacket packet) {
         motor.setPower(power);
         return false;
      }
   }

   public static class ServoPositionAction implements Action {
      double position;
      Servo servo;
      String servoName = "unknown";

      public ServoPositionAction(Servo servo, double position, String servoName) {
         this.servo = servo;
         this.position = position;
         this.servoName = servoName;
      }

      public ServoPositionAction(Servo servo, double position) {
         this.servo = servo;
         this.position = position;
      }

      @Override
      public boolean run(TelemetryPacket packet) {
         servo.setPosition(position);
         String message = "Servo " + this.servoName + " set to position: " + position;
//         packet.addLine(message);
         Log.d("ActionUtil.ServoAction", message);
         return false;
      }
   }

   public static class CRServoAction implements Action {
      double power;
      CRServo servo;

      public CRServoAction(CRServo servo, double power) {
         this.servo = servo;
         this.power = power;
      }

      @Override
      public boolean run(TelemetryPacket packet) {
         servo.setPower(power);
         return false;
      }
   }
}
