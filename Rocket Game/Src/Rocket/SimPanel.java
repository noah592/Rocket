// File: src/rocket/SimPanel.java
package rocket;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class SimPanel extends JComponent implements ActionListener {
  private final State s;
  private final Timer timer;
  private long lastNs = System.nanoTime();

  private double camX = 0, camY = 0, pxPerM = Config.H / (3.0 * (Config.BODY_H + Config.CONE_H));
  private int followBodyIdx = -1; // -1 = none; else index in s.bodies

  public SimPanel(State s) {
    this.s = s;
    setPreferredSize(new Dimension(Config.W, Config.H));
    setBackground(Color.BLACK);
    setFocusable(true);
    setOpaque(true);
    timer = new Timer(1000 / Config.FPS, this);
    timer.start();
    s.followRocket = true;          // default to rocket follow
    snapCameraToRocket();
  }

  @Override protected void paintComponent(Graphics g) {
    super.paintComponent(g);
    Render.drawAll((Graphics2D) g, this, s, null);
  }

  @Override public void actionPerformed(ActionEvent e) {
    long now = System.nanoTime();
    double dtReal = (now - lastNs) / 1e9;
    lastNs = now;
    if (s.paused) { repaint(); return; }

    // Advance on-rails bodies
    s.advanceSimTime(dtReal);

    // Inputs & rotation
    if (s.upHeld)   s.throttle = Math.min(1.0, s.throttle + Config.THROTTLE_RATE * dtReal);
    if (s.downHeld) s.throttle = Math.max(0.0, s.throttle - Config.THROTTLE_RATE * dtReal);
    double turn = (s.leftHeld ? -1 : 0) + (s.rightHeld ? 1 : 0);
    s.ang += Config.ANG_SPEED_RAD * turn * dtReal;

    // Physics step
    double dt = dtReal * s.timeScale;
    double nx = Math.sin(s.ang), ny = Math.cos(s.ang);
    double ax = Config.MAX_THRUST_G * s.throttle * Config.G0 * nx;
    double ay = Config.MAX_THRUST_G * s.throttle * Config.G0 * ny;

    for (State.Body b : s.bodies) {
      double dx = s.rx - b.cx, dy = s.ry - b.cy;
      double r2 = dx*dx + dy*dy, r = Math.sqrt(r2);
      if (r > 1) { double invR3 = 1.0 / (r2 * r); ax += -b.mu * dx * invR3; ay += -b.mu * dy * invR3; }
    }

    s.vx += ax * dt; s.vy += ay * dt;
    s.rx += s.vx * dt; s.ry += s.vy * dt;

    // Ground collision in ground frame
    State.Body nb = s.nearestBody(s.rx, s.ry);
    double dx = s.rx - nb.cx, dy = s.ry - nb.cy, rr = Math.hypot(dx, dy);
    double surfR = Physics.surfaceRadiusAt(nb, s.rx, s.ry);
    if (rr < surfR) {
      double inv = 1.0 / Math.max(rr, 1e-6), nxn = dx * inv, nyn = dy * inv;
      s.rx = nb.cx + nxn * surfR; s.ry = nb.cy + nyn * surfR;
      double gvx = s.vx - nb.vcx, gvy = s.vy - nb.vcy;
      double vn = gvx * nxn + gvy * nyn;
      if (vn < 0) { gvx -= vn * nxn; gvy -= vn * nyn; }
      double gt2 = gvx*gvx + gvy*gvy;
      if (s.throttle < 0.02 && gt2 < 0.25) { gvx = 0; gvy = 0; } else { gvx *= 0.98; gvy *= 0.98; }
      s.vx = nb.vcx + gvx; s.vy = nb.vcy + gvy;
    }

    // Camera follow
    if (s.followRocket) { camX = s.rx; camY = s.ry; }
    else if (followBodyIdx >= 0 && followBodyIdx < s.bodies.size()) {
      State.Body b = s.bodies.get(followBodyIdx);
      camX = b.cx; camY = b.cy;
    }

    repaint();
  }

  // ---------- Transforms & camera ----------
  public double w2sX(double wx){ return (wx - camX) * pxPerM + getWidth() / 2.0; }
  public double w2sY(double wy){ return -(wy - camY) * pxPerM + getHeight() / 2.0; }
  public double s2wX(double sx){ return (sx - getWidth() / 2.0) / pxPerM + camX; }
  public double s2wY(double sy){ return -((sy - getHeight() / 2.0) / pxPerM) + camY; }
  public double pxPerM(){ return pxPerM; }
  public void setPxPerM(double v){
    pxPerM = Math.max(Config.MIN_PX_PER_M, Math.min(Config.MAX_PX_PER_M, v));
  }
  public double camX(){ return camX; }
  public double camY(){ return camY; }
  public void setCam(double x,double y){ camX=x; camY=y; }
  public void snapCameraToRocket(){ camX = s.rx; camY = s.ry; }
  public void snapCameraToBody(int idx){
    if (idx>=0 && idx<s.bodies.size()){ State.Body b = s.bodies.get(idx); camX = b.cx; camY = b.cy; }
  }

  /** NEW: follow body and auto-zoom to fit the whole planet (so it can't vanish when zoomed in). */
  public void setFollowBody(int idx){
    followBodyIdx = idx; s.followRocket = false;
    zoomToFitBody(idx); // adjust zoom first
    snapCameraToBody(idx);
  }

  public void clearFollowBody(){ followBodyIdx = -1; }

  /** Choose a px/m so that (radius + halo/padding) fits inside the shorter screen dimension. */
  private void zoomToFitBody(int idx){
    if (idx < 0 || idx >= s.bodies.size()) return;
    State.Body b = s.bodies.get(idx);
    double R = b.radius + Math.max(b.atmThick, b.terrainAmp);     // include halo/bumps
    double pad = Math.max(0.08 * R, 2000.0);                      // extra safety pad
    double wantPxPerM = (Math.min(getWidth(), getHeight()) * 0.9) / (2.0 * (R + pad));
    // Only zoom OUT if currently too zoomed in
    if (pxPerM > wantPxPerM) setPxPerM(wantPxPerM);
  }
}
