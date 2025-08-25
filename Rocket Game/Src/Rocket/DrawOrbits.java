// File: src/rocket/DrawOrbits.java
package rocket;

import java.awt.*;

public final class DrawOrbits {
    private DrawOrbits(){}

    public static void draw(Graphics2D g2, SimPanel v, State s) {
        // Keep opacity strong, reduce thickness ~50% (from 2.5 to 1.25)
        final int alpha = 160;
        final Color orbitColor = new Color(
            (Config.COL_PRED_ARGB >> 16) & 0xFF,
            (Config.COL_PRED_ARGB >>  8) & 0xFF,
            (Config.COL_PRED_ARGB      ) & 0xFF,
            alpha
        );
        final float thickness = 1.25f;

        Stroke oldStroke = g2.getStroke();
        g2.setColor(orbitColor);
        g2.setStroke(new BasicStroke(thickness, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        final int N = Math.max(192, Config.ARC_SAMPLES);
        final double TWO_PI = Math.PI * 2.0;

        for (int i = 0; i < Config.BODY_COUNT; i++) {
            int mode = Config.ORBIT_MODE[i], centerIdx = Config.ORBIT_CENTER_IDX[i];
            if (mode == 0 || centerIdx < 0) continue;

            State.Body c = s.bodies.get(centerIdx);
            double A = Config.ORBIT_A_M[i], B = Config.ORBIT_B_M[i];
            if (A <= 0.0 && B <= 0.0) continue;

            double prevSX = 0, prevSY = 0;
            for (int k = 0; k <= N; k++) {
                double t = (k / (double) N) * TWO_PI;
                double wx = c.cx + A * Math.cos(t);
                double wy = c.cy + B * Math.sin(t);
                double sx = v.w2sX(wx), sy = v.w2sY(wy);
                if (k > 0) {
                    g2.drawLine((int)Math.round(prevSX), (int)Math.round(prevSY),
                                (int)Math.round(sx),     (int)Math.round(sy));
                }
                prevSX = sx; prevSY = sy;
            }
        }
        g2.setStroke(oldStroke);
    }
}
