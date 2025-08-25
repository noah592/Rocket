// File: src/rocket/DrawHud.java
package rocket;
import java.awt.*;

public final class DrawHud {
    private DrawHud(){}
    private static final Font HUD_FONT   = new Font(Config.HUD_FONT_FAMILY, Font.PLAIN, Config.HUD_FONT_SIZE);
    private static final Font LABEL_FONT = new Font(Config.LABEL_FONT_FAMILY, Font.PLAIN, Config.LABEL_FONT_SIZE);

    public static void draw(Graphics2D g2, SimPanel v, State s, Config c){
        g2.setFont(HUD_FONT);
        g2.setColor(Color.WHITE);

        // Reference body for local metrics
        State.Body ref = s.nearestBody(s.rx, s.ry);
        double rxC = s.rx - ref.cx, ryC = s.ry - ref.cy, r = Math.hypot(rxC, ryC);
        double urx = rxC / Math.max(r, 1e-9), ury = ryC / Math.max(r, 1e-9), utx = -ury, uty = urx;
        double vr  = -(s.vx*urx + s.vy*ury);
        double vt  =  (s.vx*utx + s.vy*uty);

        // Net gravity magnitude
        double grav=0.0;
        for(State.Body b: s.bodies){
            double dx=s.rx-b.cx, dy=s.ry-b.cy, R2=dx*dx+dy*dy;
            if(R2>1) grav += b.mu / R2;
        }

        // Altitude (clamped non-negative)
        double surfR = Physics.surfaceRadiusAt(ref, s.rx, s.ry);
        double alt   = Math.max(0.0, r - surfR);

        // Camera mode label
        String followed = followedBodyName(v, s);
        String camMode = s.followRocket ? "ROCKET" : (followed != null ? "PLANET:" + followed : "FREE");

        // Build left-column lines with fixed precision
        String line0 = String.format("Cam: %s | %s", camMode, s.paused ? "PAUSED" : "RUN");
        String line1 = String.format("time× = %6.2f"   , s.timeScale);
        String line2 = String.format("Throttle = %5.1f%%", 100*s.throttle);
        String line3 = String.format("v_r (down) = %7.1f m/s", vr);
        String line4 = String.format("v_t (tan)  = %7.1f m/s", vt);
        String line5 = String.format("Alt = %9.1f m", alt);
        String line6 = String.format("g = %8.3f m/s²", grav);

        int x = 12;
        int y = 18;
        int lh = HUD_FONT.getSize() + 6; // line spacing

        g2.drawString(line0, x, y);            y += lh;
        g2.drawString(line1, x, y);            y += lh;
        g2.drawString(line2, x, y);            y += lh;
        g2.drawString(line3, x, y);            y += lh;
        g2.drawString(line4, x, y);            y += lh;
        g2.drawString(line5, x, y);            y += lh;
        g2.drawString(line6, x, y);

        // Controls legend stays at bottom
        g2.setFont(LABEL_FONT);
        g2.drawString(Config.CONTROLS_LEGEND, 12, v.getHeight()-18);
    }

    private static String followedBodyName(SimPanel v, State s){
        final double cx = v.camX(), cy = v.camY(), EPS = 1e-3;
        String bestName = null; double bestD2 = Double.POSITIVE_INFINITY;
        for (State.Body b : s.bodies) {
            double dx = cx - b.cx, dy = cy - b.cy, d2 = dx*dx + dy*dy;
            if (d2 < bestD2) { bestD2 = d2; bestName = b.name; }
            if (Math.abs(dx) < EPS && Math.abs(dy) < EPS) return b.name;
        }
        return (bestD2 <= EPS*EPS) ? bestName : null;
    }
}
