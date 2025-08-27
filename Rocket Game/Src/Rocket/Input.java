// File: src/rocket/Input.java
package rocket;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class Input implements KeyListener, MouseWheelListener, MouseListener, MouseMotionListener {
  private final SimPanel v;
  private final State s;
  private Point lastDrag = null;

  // Track whether we're currently following a body (planet/sun/moon).
  private boolean followingBody = false;

  public Input(SimPanel v, State s){ this.v=v; this.s=s; }

  // ---------- Keys ----------
  @Override public void keyPressed(KeyEvent e){
    switch(e.getKeyCode()){
      case KeyEvent.VK_UP:
      case KeyEvent.VK_W: s.upHeld=true; break;
      case KeyEvent.VK_DOWN:
      case KeyEvent.VK_S: s.downHeld=true; break;
      case KeyEvent.VK_LEFT:
      case KeyEvent.VK_A: s.leftHeld=true; break;
      case KeyEvent.VK_RIGHT:
      case KeyEvent.VK_D: s.rightHeld=true; break;

      case KeyEvent.VK_SPACE: s.paused=!s.paused; break;

      case KeyEvent.VK_R:
        s.reset();
        v.snapCameraToRocket();
        s.followRocket=true;
        v.clearFollowBody();
        followingBody=false;
        break;

      case KeyEvent.VK_OPEN_BRACKET:  s.nudgePredHorizon(e.isShiftDown()? -300 : -30); break;
      case KeyEvent.VK_CLOSE_BRACKET: s.nudgePredHorizon(e.isShiftDown()?  300 :  30); break;
      case KeyEvent.VK_MINUS:  s.scaleTime(0.5); break;
      case KeyEvent.VK_EQUALS: s.scaleTime(2.0); break;
      case KeyEvent.VK_0: s.throttle=0.0; break;
      case KeyEvent.VK_1: s.throttle=1.0; break;

      // F behavior:
      // - If in Planet Follow -> switch to FreeCam
      // - If in FreeCam      -> switch to Rocket Follow
      // - If in Rocket Follow-> switch to FreeCam
      case KeyEvent.VK_F:
        if (followingBody) {
          // Planet Follow -> FreeCam
          followingBody = false;
          s.followRocket = false;
          v.clearFollowBody();
          // leave camera where it is
        } else if (!s.followRocket) {
          // FreeCam -> Rocket Follow
          s.followRocket = true;
          v.clearFollowBody();
          v.snapCameraToRocket();
        } else {
          // Rocket Follow -> FreeCam
          s.followRocket = false;
          v.clearFollowBody();
        }
        break;
    }
  }

  @Override public void keyReleased(KeyEvent e){
    switch(e.getKeyCode()){
      case KeyEvent.VK_UP:
      case KeyEvent.VK_W: s.upHeld=false; break;
      case KeyEvent.VK_DOWN:
      case KeyEvent.VK_S: s.downHeld=false; break;
      case KeyEvent.VK_LEFT:
      case KeyEvent.VK_A: s.leftHeld=false; break;
      case KeyEvent.VK_RIGHT:
      case KeyEvent.VK_D: s.rightHeld=false; break;
    }
  }
  @Override public void keyTyped(KeyEvent e){}

  // ---------- Mouse wheel zoom (anchor under cursor) ----------
  @Override public void mouseWheelMoved(MouseWheelEvent e){
    int notches = e.getWheelRotation();
    double zoom = Math.pow(1.1, -notches);
    double bx = v.s2wX(e.getX()), by = v.s2wY(e.getY());
    v.setPxPerM(v.pxPerM() * zoom);
    double ax = v.s2wX(e.getX()), ay = v.s2wY(e.getY());
    v.setCam(v.camX() + (bx-ax), v.camY() + (by-ay));
    v.repaint();
  }

  // ---------- Mouse drag pan (only meaningful in FreeCam) ----------
  @Override public void mousePressed(MouseEvent e){
    if (SwingUtilities.isLeftMouseButton(e)) lastDrag = e.getPoint();
  }
  @Override public void mouseReleased(MouseEvent e){ lastDrag = null; }
  @Override public void mouseDragged(MouseEvent e){
    if (lastDrag != null && !s.followRocket && !followingBody) {
      Point p = e.getPoint();
      double dx = (p.x - lastDrag.x) / v.pxPerM();
      double dy = (p.y - lastDrag.y) / v.pxPerM();
      v.setCam(v.camX() - dx, v.camY() + dy);
      lastDrag = p;
      v.repaint();
    }
  }
  // ---------- Click-to-follow body OR click HUD buttons ----------
  @Override public void mouseClicked(MouseEvent e){
    if (!SwingUtilities.isLeftMouseButton(e)) return;

    int mx = e.getX(), my = e.getY();

    // 1) HUD time buttons take priority
    int idx = DrawHud.hitTestTimeButton(mx, my);
    if (idx >= 0){
      double val = DrawHud.presetValueAt(idx);
      s.timeScale = State.clamp(val, Config.TIME_SCALE_MIN, Config.TIME_SCALE_MAX);
      v.repaint();
      return;
    }

    // 2) World picking (bodies / rocket follow)
    int pick = -1; double best = Double.POSITIVE_INFINITY;

    for (int i = 0; i < s.bodies.size(); i++) {
      State.Body b = s.bodies.get(i);
      double cx = v.w2sX(b.cx), cy = v.w2sY(b.cy), rpx = b.radius * v.pxPerM();
      double d = Math.hypot(mx - cx, my - cy);
      double thresh = Math.max(30.0, Math.min(rpx, 120.0)); // within disk or within ~120px
      if (d <= Math.max(thresh, rpx) && d < best) { best = d; pick = i; }
    }

    if (pick >= 0) {
      // Follow that body and auto-zoom to fit (SimPanel handles zoom+snap)
      s.followRocket = false;
      v.setFollowBody(pick);
      followingBody = true;
    } else {
      // Clicked empty space -> follow rocket
      s.followRocket = true;
      v.clearFollowBody();
      v.snapCameraToRocket();
      followingBody = false;
    }
  }

  // Unused
  @Override public void mouseEntered(MouseEvent e) {}
  @Override public void mouseExited(MouseEvent e) {}
  @Override public void mouseMoved(MouseEvent e) {}
}
