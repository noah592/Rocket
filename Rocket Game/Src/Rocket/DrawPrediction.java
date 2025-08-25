// File: src/rocket/DrawPrediction.java
package rocket;
import java.awt.*; import java.awt.geom.Point2D; import java.util.List;

public final class DrawPrediction {
    private DrawPrediction(){}

    /** Just draw the ballistic prediction points returned by Physics. */
    public static void draw(Graphics2D g2, SimPanel v, State s, Config c){
        final double dt = Config.BASE_PRED_DT;
        List<Point2D.Double> pts = Physics.predictBallistic(s, s.predHorizonSec, dt);
        if (pts.size() < 2) return;

        g2.setColor(new Color(Config.COL_PRED_ARGB, true));
        g2.setStroke(new BasicStroke(1f));

        Point2D.Double prev = pts.get(0);
        for (int i = 1; i < pts.size(); i++){
            Point2D.Double p = pts.get(i);
            int x1 = (int)Math.round(v.w2sX(prev.x)), y1 = (int)Math.round(v.w2sY(prev.y));
            int x2 = (int)Math.round(v.w2sX(p.x)),    y2 = (int)Math.round(v.w2sY(p.y));
            g2.drawLine(x1, y1, x2, y2);
            prev = p;
        }
    }
}
