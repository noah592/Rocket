// File: src/rocket/Physics.java
package rocket;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.*;

public final class Physics {
    private Physics(){}

    /** Elevation above spherical radius at polar angle around body center. */
    public static double elevationAtAngle(State.Body b, double theta){
        // Earth has no terrain bumps (smooth sphere)
        if ("Earth".equalsIgnoreCase(b.name)) return 0.0;

        double amp = b.terrainAmp;
        if (amp <= 0.0) return 0.0;

        double s = b.radius * theta;       // arc-length proxy
        double K1 = (2*PI) / b.terrainL;   // fundamental wavelength
        return amp * (
                0.60*sin(K1*s + 0.3) +
                0.30*sin(2*K1*s + 1.7) +
                0.10*sin(4*K1*s + 5.1)
        );
    }

    /** Public API: spherical radius + elevation at world point (x,y) using the body's center. */
    public static double surfaceRadiusAt(State.Body b, double x, double y){
        double theta = atan2(y - b.cy, x - b.cx);
        if (theta < 0) theta += 2*PI;
        return b.radius + elevationAtAngle(b, theta);
    }

    /** Internal helper: same as above, but using arbitrary center coords (e.g. orbiting body at time T). */
    private static double surfaceRadiusAtAtTime(State.Body b, double bodyCx, double bodyCy, double x, double y){
        double theta = atan2(y - bodyCy, x - bodyCx);
        if (theta < 0) theta += 2*PI;
        return b.radius + elevationAtAngle(b, theta);
    }

    /** On-rails orbital state (position + velocity) for body i at absolute sim time T. */
    private static OrbState onRailsAt(int i, double T){
        int mode = Config.ORBIT_MODE[i];
        int cIdx = Config.ORBIT_CENTER_IDX[i];
        if (mode == 0 || cIdx < 0){
            return new OrbState(Config.BODY_CX[i], Config.BODY_CY[i], 0.0, 0.0);
        } else {
            OrbState p = onRailsAt(cIdx, T);
            double A = Config.ORBIT_A_M[i], B = Config.ORBIT_B_M[i];
            double w = Config.ORBIT_OMEGA_RAD_PER_S[i];
            double th = Config.ORBIT_PHASE_RAD[i] + w * T;
            double wx = p.x + A * cos(th);
            double wy = p.y + B * sin(th);
            double vx = p.vx + (-A * w * sin(th));
            double vy = p.vy + ( B * w * cos(th));
            return new OrbState(wx, wy, vx, vy);
        }
    }

    private static final class OrbState {
        final double x, y, vx, vy;
        OrbState(double x, double y, double vx, double vy){ this.x=x; this.y=y; this.vx=vx; this.vy=vy; }
    }

    /** Find nearest body index at (x,y) using on-rails positions at time T. */
    private static int nearestBodyIdxAt(double x, double y, double T){
        int best=-1; double bestD2=Double.POSITIVE_INFINITY;
        for (int i=0;i<Config.BODY_COUNT;i++){
            OrbState bi = onRailsAt(i, T);
            double dx = x - bi.x, dy = y - bi.y, d2 = dx*dx + dy*dy;
            if (d2 < bestD2){ bestD2 = d2; best = i; }
        }
        return (best>=0)? best : 0;
    }

    /**
     * Predict ballistic trajectory (no thrust).
     * Returns world points that are already expressed relative to the
     * nearest body at t0. DrawPrediction just plots them.
     */
    public static List<Point2D.Double> predictBallistic(State s, int horizonSec, double baseDt){
        // Step size
        double dt = Math.max(baseDt, horizonSec / (double) Config.PRED_POINTS_MAX);
        int steps = Math.max(1, (int) ceil(horizonSec / dt));
        steps = Math.min(steps, Config.PRED_POINTS_MAX);

        ArrayList<Point2D.Double> pts = new ArrayList<>(steps + 1);

        // Lock reference body at t0
        final double T0 = s.simTimeSec;
        int refIdx = nearestBodyIdxAt(s.rx, s.ry, T0);
        OrbState ref0 = onRailsAt(refIdx, T0);

        // Start rocket state (world frame)
        double px = s.rx, py = s.ry, pvx = s.vx, pvy = s.vy;

        double T = T0;
        for (int i = 0; i < steps; i++) {
            T += dt;

            // Gravity sum
            double ax = 0.0, ay = 0.0;
            for (int bIdx = 0; bIdx < Config.BODY_COUNT; bIdx++){
                OrbState bi = onRailsAt(bIdx, T);
                double dx = px - bi.x, dy = py - bi.y;
                double r2 = dx*dx + dy*dy;
                if (r2 > 1.0){
                    double r = sqrt(r2);
                    double invR3 = 1.0 / (r2 * r);
                    double mu = Config.BODY_MU[bIdx];
                    ax += -mu * dx * invR3;
                    ay += -mu * dy * invR3;
                }
            }

            // Integrate rocket
            pvx += ax * dt;  pvy += ay * dt;
            px  += pvx * dt; py  += pvy * dt;

            // Collision check
            int nbIdx = nearestBodyIdxAt(px, py, T);
            OrbState nbState = onRailsAt(nbIdx, T);
            State.Body nb = s.bodies.get(nbIdx);
            double rr = hypot(px - nbState.x, py - nbState.y);
            double rSurf = surfaceRadiusAtAtTime(nb, nbState.x, nbState.y, px, py);
            if (rr < rSurf) break;

            // Convert to local frame of ref body
            OrbState refT = onRailsAt(refIdx, T);
            double relX = px - refT.x;
            double relY = py - refT.y;
            double plotX = ref0.x + relX;
            double plotY = ref0.y + relY;

            pts.add(new Point2D.Double(plotX, plotY));
        }

        return pts;
    }
}
