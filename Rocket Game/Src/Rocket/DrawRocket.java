// File: src/rocket/DrawRocket.java
package rocket;

import java.awt.*;

public final class DrawRocket {
    private DrawRocket(){}

    public static void draw(Graphics2D g2, SimPanel v, State s, Config c) {
        double nx = Math.sin(s.ang), ny = Math.cos(s.ang);
        double tx = Math.cos(s.ang), ty = -Math.sin(s.ang);
        double halfW = Config.BODY_W/2.0;

        double blx = s.rx + (-halfW)*tx, bly = s.ry + (-halfW)*ty;
        double brx = s.rx + ( halfW)*tx, bry = s.ry + ( halfW)*ty;
        double tlx = blx + Config.BODY_H*nx, tly = bly + Config.BODY_H*ny;
        double trx = brx + Config.BODY_H*nx, try_ = bry + Config.BODY_H*ny;
        double apexX = (tlx + trx)/2.0 + Config.CONE_H*nx;
        double apexY = (tly + try_)/2.0 + Config.CONE_H*ny;

        double bcx = v.w2sX(s.rx), bcy = v.w2sY(s.ry);
        double tcx = (v.w2sX(tlx) + v.w2sX(trx))*0.5, tcy = (v.w2sY(tly) + v.w2sY(try_))*0.5;
        double bodyPix = Math.hypot(tcx - bcx, tcy - bcy);
        double conePix = Math.hypot(v.w2sX(apexX)-tcx, v.w2sY(apexY)-tcy);
        double fullHeightPx = bodyPix + conePix;

        if (fullHeightPx < Config.ARROW_MIN_HEIGHT_PX) {
            double dsx =  Math.sin(s.ang), dsy = -Math.cos(s.ang);
            double len = Math.hypot(dsx, dsy); if (len < 1e-9) return;
            dsx/=len; dsy/=len; double pxv = -dsy, pyv = dsx;
            double tipX = bcx + dsx*Config.ARROW_SIZE_PX, tipY = bcy + dsy*Config.ARROW_SIZE_PX;
            double baseHalfW = 0.35 * Config.ARROW_SIZE_PX;
            int[] ax = new int[]{ (int)Math.round(tipX),
                                  (int)Math.round(bcx + pxv*baseHalfW),
                                  (int)Math.round(bcx - pxv*baseHalfW) };
            int[] ay = new int[]{ (int)Math.round(tipY),
                                  (int)Math.round(bcy + pyv*baseHalfW),
                                  (int)Math.round(bcy - pyv*baseHalfW) };
            g2.setColor(new Color(200,220,255)); g2.fillPolygon(ax, ay, 3);
            g2.setColor(new Color(0,0,0,120)); g2.drawPolygon(ax, ay, 3);
            return;
        }

        int[] fx = new int[]{ (int)Math.round(v.w2sX(tlx)), (int)Math.round(v.w2sX(trx)),
                              (int)Math.round(v.w2sX(brx)), (int)Math.round(v.w2sX(blx)) };
        int[] fy = new int[]{ (int)Math.round(v.w2sY(tly)), (int)Math.round(v.w2sY(try_)),
                              (int)Math.round(v.w2sY(bry)), (int)Math.round(v.w2sY(bly)) };
        g2.setColor(new Color(200,220,255)); g2.fillPolygon(fx, fy, 4);
        g2.setColor(new Color(0,0,0,120)); g2.setStroke(new BasicStroke(1.5f)); g2.drawPolygon(fx, fy, 4);

        int[] cxp = new int[]{ (int)Math.round(v.w2sX(apexX)), (int)Math.round(v.w2sX(trx)), (int)Math.round(v.w2sX(tlx)) };
        int[] cyp = new int[]{ (int)Math.round(v.w2sY(apexY)), (int)Math.round(v.w2sY(try_)), (int)Math.round(v.w2sY(tly)) };
        g2.setColor(new Color(200,220,255)); g2.fillPolygon(cxp, cyp, 3);
        g2.setColor(new Color(0,0,0,120)); g2.drawPolygon(cxp, cyp, 3);

        int baseMidX = (int)Math.round(v.w2sX(s.rx));
        int baseMidY = (int)Math.round(v.w2sY(s.ry + 0.5));
        int[] lfx = new int[]{ (int)Math.round(v.w2sX(blx)),
                               (int)Math.round(v.w2sX(blx - Config.FIN_LEN*tx - Config.FIN_DROP*nx)),
                               baseMidX };
        int[] lfy = new int[]{ (int)Math.round(v.w2sY(bly)),
                               (int)Math.round(v.w2sY(bly - Config.FIN_LEN*ty - Config.FIN_DROP*ny)),
                               baseMidY };
        int[] rfx = new int[]{ (int)Math.round(v.w2sX(brx)),
                               (int)Math.round(v.w2sX(brx + Config.FIN_LEN*tx - Config.FIN_DROP*nx)),
                               baseMidX };
        int[] rfy = new int[]{ (int)Math.round(v.w2sY(bry)),
                               (int)Math.round(v.w2sY(bry + Config.FIN_LEN*ty - Config.FIN_DROP*ny)),
                               baseMidY };
        g2.setColor(new Color(200,220,255));
        g2.fillPolygon(lfx, lfy, 3); g2.fillPolygon(rfx, rfy, 3);
        g2.setColor(new Color(0,0,0,120));
        g2.drawPolygon(lfx, lfy, 3); g2.drawPolygon(rfx, rfy, 3);

        if (s.throttle > 0.02) {
            double flameLen = 5.0 * Config.BODY_H * s.throttle;
            double tipx = s.rx - Math.sin(s.ang) * flameLen;
            double tipy = s.ry - Math.cos(s.ang) * flameLen;
            int[] fxp = new int[]{ (int)Math.round(v.w2sX(blx)), (int)Math.round(v.w2sX(brx)), (int)Math.round(v.w2sX(tipx)) };
            int[] fyp = new int[]{ (int)Math.round(v.w2sY(bly)), (int)Math.round(v.w2sY(bry)), (int)Math.round(v.w2sY(tipy)) };
            g2.setColor(new Color(255,170,60,220)); g2.fillPolygon(fxp, fyp, 3);
        }
    }
}
