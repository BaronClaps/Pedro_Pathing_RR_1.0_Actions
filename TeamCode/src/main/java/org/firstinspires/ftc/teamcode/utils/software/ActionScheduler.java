package org.firstinspires.ftc.teamcode.utils.software;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.canvas.Canvas;
import com.acmerobotics.dashboard.telemetry.TelemetryPacket;
import com.acmerobotics.roadrunner.Action;
import com.acmerobotics.roadrunner.ftc.Actions;

import java.util.LinkedList;
import java.util.Queue;

public class ActionScheduler {
   final Queue<Action> actions = new LinkedList<>();
   final FtcDashboard dash = FtcDashboard.getInstance();
   final Canvas canvas = new Canvas();

   public void queueAction(Action action) {
      if (actions.peek() == null) {
         action.preview(canvas);
      }
      actions.add(action);
   }

   public void update() {
      if (actions.peek() == null) {
         return;
      }

      TelemetryPacket packet = new TelemetryPacket();
      packet.fieldOverlay().getOperations().addAll(canvas.getOperations());

      boolean running = actions.peek().run(packet);
      dash.sendTelemetryPacket(packet);

      if (!running) {
         actions.remove();
         if (actions.peek() != null) {
            actions.peek().preview(canvas);
         }
      }
   }
}
