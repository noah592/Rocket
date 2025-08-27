// File: src/rocket/DrawHud.java
package rocket;
import java.awt.*;

public final class DrawHud {
    private DrawHud(){}

    private static final Font HUD_FONT   = new Font(Config.HUD_FONT_FAMILY, Font.PLAIN, Config.HUD_FONT_SIZE);
    private static final Font LABEL_FONT = new Font(Config.LABEL_FONT_FAMILY, Font.PLAIN, Config.LABEL_FONT_SIZE);

    // Cache of button bounds computed during draw pass (screen coords)
    private static Rectangle[] timeBtnBounds = new Rectangle[0];

    /** Returns the index of the time preset button under (mx,my), or -1 if none. */
    public static int hitTestTimeButton(int mx, int my){
        Rectangle[] arr = timeBtnBounds;
        if (arr == null) return -1;
        for (int i = 0; i < arr.length; i++){
            if (arr[i] != null && arr[i].contains(mx, my)) return i;
        }
        return -1;
    }

    /** Returns the preset value (e.g., 1, 5, 25, ...) at index, or 1 if OOB. */
    public static double presetValueAt(int idx){
        if (idx < 0 || idx >= Config.TIME_PRESETS.length) return 1.0;
        return Config.TIME_PRESETS[idx];
    }

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
        g2.drawString(line6, x, y);            y += lh;

        // --- Fuel readout (text + bar) ---
        double fuelFrac = s.fuelFrac();
        double fuelPct  = 100.0 * fuelFrac;
        double fuelSec  = s.fuelSec;
        String line7 = String.format("Fuel = %6.1f%%  |  %7.1f s", fuelPct, fuelSec);
        g2.drawString(line7, x, y);
        // Bar under the fuel line
        int barX = x, barY = y + 6;
        int barW = 200, barH = 10;
        g2.setColor(new Color(40,40,40,180)); g2.fillRect(barX, barY, barW, barH);
        g2.setColor(new Color(255,255,255,120)); g2.drawRect(barX, barY, barW, barH);
        int fillW = (int)Math.round(barW * fuelFrac);
        g2.setColor(new Color(90,200,90,220)); g2.fillRect(barX, barY, Math.max(0, fillW), barH);
        g2.setColor(Color.WHITE); // restore for later text

        // --- Right-side Time Preset Buttons ---
        drawTimeButtons(g2, v, s);

        // Controls legend stays at bottom
        g2.setFont(LABEL_FONT);
        g2.setColor(Color.WHITE);
        g2.drawString(Config.CONTROLS_LEGEND, 12, v.getHeight()-18);
    }

    private static void drawTimeButtons(Graphics2D g2, SimPanel v, State s){
        final int n = Config.TIME_PRESETS.length;
        if (timeBtnBounds.length != n) timeBtnBounds = new Rectangle[n];

        final int size = Config.HUD_BTN_SIZE;
        final int width = size * 2; // doubled width for text
        final int gap  = Config.HUD_BTN_GAP;
        final int margin = Config.HUD_MARGIN;

        int xRight = v.getWidth() - margin - width;
        int yTop   = margin;

        // Styles
        Color bg      = new Color(40, 40, 40, 160);
        Color bgActive= new Color(80, 130, 220, 220);
        Color outline = new Color(255, 255, 255, 120);
        Color fg      = Color.WHITE;

        g2.setFont(LABEL_FONT);

        for (int i = 0; i < n; i++){
            int y = yTop + i * (size + gap);
            Rectangle r = new Rectangle(xRight, y, width, size);
            timeBtnBounds[i] = r;

            // Decide fill based on active
            double preset = Config.TIME_PRESETS[i];
            boolean isActive = approxEqual(s.timeScale, preset, 1e-6) ||
                               Math.abs(Math.log((s.timeScale+1e-12)/(preset+1e-12))) < 1e-3;

            g2.setColor(isActive ? bgActive : bg);
            g2.fillRect(r.x, r.y, r.width, r.height);

            g2.setColor(outline);
            g2.drawRect(r.x, r.y, r.width, r.height);

            // Label text like "1x", "5x", ...
            String label = (preset % 1.0 == 0.0)
                    ? String.format("%.0fx", preset)
                    : String.format("%.2fx", preset);
            // Fit text centered
            FontMetrics fm = g2.getFontMetrics();
            int tx = r.x + (r.width - fm.stringWidth(label))/2;
            int ty = r.y + (r.height + fm.getAscent() - fm.getDescent())/2;

            g2.setColor(fg);
            g2.drawString(label, tx, ty);
        }
    }

    private static boolean approxEqual(double a, double b, double eps){ return Math.abs(a-b) <= eps; }

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
