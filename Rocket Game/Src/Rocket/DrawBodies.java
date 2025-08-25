// File: src/rocket/DrawBodies.java
package rocket;

import java.awt.*;
import java.awt.geom.*;
import java.util.ArrayList;
import static java.lang.Math.*;

public final class DrawBodies {
  private DrawBodies(){}
  // Track placed label bounds this frame to avoid overlaps
  private static final ArrayList<Rectangle> LABEL_BOUNDS = new ArrayList<>();

  public static void renderBodies(Graphics2D g2, SimPanel v, State s, Config c){
    LABEL_BOUNDS.clear();
    for(int i=0;i<Config.BODY_COUNT;i++) drawBody(g2,v,s,i,s.bodies.get(i));
  }

  private static void drawBody(Graphics2D g2, SimPanel v, State s, int idx, State.Body b){
    final double cxpx=v.w2sX(b.cx), cypx=v.w2sY(b.cy);
    final double rpx=b.radius*v.pxPerM();
    final boolean isEarth="Earth".equalsIgnoreCase(b.name);

    // Tiny-body rule: draw 2px white dot, no label
    if (rpx < Config.TINY_BODY_THRESH_PX){
      g2.setColor(Color.WHITE);
      g2.fillRect((int)round(cxpx)-1, (int)round(cypx)-1, 2, 2);
      return;
    }

    if(rpx<=Config.FILL_MAX_RADIUS_PX){
      // Full-disc
      if(isEarth){
        drawEarthTexturedDisc(g2,cxpx,cypx,rpx);
        drawEarthAtmosphereFull(g2,cxpx,cypx,b,v);
      }else{
        g2.setColor(new Color(Config.BODY_COLOR_ARGB[idx],true));
        g2.fill(new Ellipse2D.Double(cxpx-rpx,cypx-rpx,rpx*2,rpx*2));
        if(b.atmThick>0.0 && (Config.BODY_ATM_INNER_ARGB[idx]>>>24)>0){
          float innerFrac=(float)(b.radius/(b.radius+b.atmThick));
          float[] dist=new float[]{0f,innerFrac,1f};
          Color[] cols=new Color[]{new Color(0,0,0,0),
            new Color(Config.BODY_ATM_INNER_ARGB[idx],true),
            new Color(Config.BODY_ATM_OUTER_ARGB[idx],true)};
          float haloR=(float)((b.radius+b.atmThick)*v.pxPerM());
          Paint old=g2.getPaint();
          g2.setPaint(new RadialGradientPaint(
            new Point2D.Float((float)cxpx,(float)cypx),haloR,dist,cols,MultipleGradientPaint.CycleMethod.NO_CYCLE));
          g2.fill(new Ellipse2D.Double(cxpx-haloR,cypx-haloR,haloR*2,haloR*2));
          g2.setPaint(old);
        }
      }
    }else{
      // Horizon arc (opaque interior bands)
      final double camX=v.camX(), camY=v.camY();
      double camAng=atan2(camY-b.cy,camX-b.cx); if(camAng<0) camAng+=2*PI;
      double diag=hypot(v.getWidth(),v.getHeight());
      double span=min(Config.ARC_SPAN_MAX_RAD,(diag*1.2)/max(rpx,1e-6));

      final int N=Config.ARC_SAMPLES;
      final double[] arcSX=new double[N], arcSY=new double[N], thetas=new double[N];

      for(int i=0;i<N;i++){
        double t=(i/(double)(N-1)-0.5)*span, th=camAng+t; if(th<0) th+=2*PI; if(th>=2*PI) th-=2*PI;
        double rSurf=isEarth? b.radius : b.radius+Physics.elevationAtAngle(b,th);
        double wx=b.cx+rSurf*cos(th), wy=b.cy+rSurf*sin(th);
        thetas[i]=th; arcSX[i]=v.w2sX(wx); arcSY[i]=v.w2sY(wy);
      }

      // Interior
      double need=hypot(v.getWidth(),v.getHeight())*1.8;
      int bands=min(Config.ARC_MAX_SEGMENTS,max(1,(int)ceil(need/Config.ARC_SEGMENT_PX)));
      Color interior=isEarth? new Color(0xFF69B36B,true):new Color(Config.BODY_COLOR_ARGB[idx],true);
      for(int bnd=0;bnd<bands;bnd++){
        int offO=bnd*Config.ARC_SEGMENT_PX, offI=(bnd+1)*Config.ARC_SEGMENT_PX;
        Polygon poly=new Polygon();
        for(int i=0;i<N;i++){ double th=thetas[i], nx=-cos(th), ny=sin(th);
          poly.addPoint((int)round(arcSX[i]+nx*offO),(int)round(arcSY[i]+ny*offO)); }
        for(int i=N-1;i>=0;i--){ double th=thetas[i], nx=-cos(th), ny=sin(th);
          poly.addPoint((int)round(arcSX[i]+nx*offI),(int)round(arcSY[i]+ny*offI)); }
        g2.setColor(interior); g2.fillPolygon(poly);
      }

      // Atmosphere rings (contiguous)
      if(b.atmThick>0.0){
        final double atmPx=b.atmThick*v.pxPerM();
        final int ab=Math.max(200,Config.ATM_BANDS*6);
        final double bandPx=Math.max(1.25,atmPx/ab);
        final Color base=isEarth? new Color(135,206,235,255)
                                : new Color(Config.BODY_ATM_INNER_ARGB[idx],true);
        for(int bnd=0;bnd<ab;bnd++){
          double offO=bnd*bandPx, offI=(bnd+1)*bandPx;
          Path2D.Double ring=new Path2D.Double();
          double th0=thetas[0], nx0=-cos(th0), ny0=sin(th0);
          ring.moveTo(arcSX[0]-nx0*offO,arcSY[0]-ny0*offO);
          for(int i=1;i<N;i++){ double th=thetas[i], nx=-cos(th), ny=sin(th);
            ring.lineTo(arcSX[i]-nx*offO,arcSY[i]-ny*offO); }
          for(int i=N-1;i>=0;i--){ double th=thetas[i], nx=-cos(th), ny=sin(th);
            ring.lineTo(arcSX[i]-nx*offI,arcSY[i]-ny*offI); }
          ring.closePath();
          double t=bnd/(double)Math.max(1,ab-1);
          int alpha=(int)round(255*(1.0-t));
          g2.setColor(new Color(base.getRed(),base.getGreen(),base.getBlue(),alpha));
          g2.fill(ring);
        }
      }
    }

    // Label (cull if too small or overlaps)
    if (rpx >= Config.LABEL_HIDE_BELOW_RPX){
      String text=b.name;
      Font f=new Font(Config.LABEL_FONT_FAMILY, Font.PLAIN, Config.LABEL_FONT_SIZE);
      g2.setFont(f);
      FontMetrics fm=g2.getFontMetrics();
      int ox=10, oy=-10;
      int sx=(int)round(cxpx)+ox, sy=(int)round(cypx)+oy;
      Rectangle bounds=new Rectangle(sx, sy - fm.getAscent(), fm.stringWidth(text), fm.getAscent());
      boolean overlaps=false;
      for(Rectangle r: LABEL_BOUNDS){ if (r.intersects(bounds)) { overlaps=true; break; } }
      if(!overlaps){
        g2.setColor(new Color(0,0,0,180)); g2.drawString(text, sx+1, sy+1);
        g2.setColor(Color.WHITE);          g2.drawString(text, sx,   sy);
        LABEL_BOUNDS.add(bounds);
      }
    }
  }

  // Earth full-disc: oceans + land (with a top-edge cap touching rim)
  private static void drawEarthTexturedDisc(Graphics2D g2,double cx,double cy,double rpx){
    Color ocean=new Color(0xFF2A74C5,true), land=new Color(0xFF69B36B,true);
    Shape disc=new Ellipse2D.Double(cx-rpx,cy-rpx,rpx*2,rpx*2);
    g2.setColor(ocean); g2.fill(disc);
    double[][] P={
      {-0.35,-0.05,0.95,0.55},{0.20,0.00,0.80,0.50},{-0.05,0.30,0.70,0.40},
      { 0.45,-0.25,0.55,0.35},{-0.55,0.35,0.50,0.30},{ 0.15,0.45,0.40,0.28},
      { 0.02,-0.78,0.72,0.44} // top-edge cap
    };
    g2.setColor(land);
    for(double[] p:P){
      double px=cx+p[0]*rpx, py=cy+p[1]*rpx, w=p[2]*rpx, h=p[3]*rpx;
      g2.fill(new Ellipse2D.Double(px-w*0.5,py-h*0.5,w,h));
    }
    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,0.10f));
    g2.setColor(ocean); g2.fill(disc);
    g2.setComposite(AlphaComposite.SrcOver);
  }

  // Earth full-disc atmosphere as donut: sky-blue → transparent
  private static void drawEarthAtmosphereFull(Graphics2D g2,double cx,double cy,State.Body b,SimPanel v){
    if(b.atmThick<=0) return;
    double r=b.radius, haloM=r+b.atmThick;
    float haloR=(float)(haloM*v.pxPerM()), innerR=(float)(r*v.pxPerM());
    float fSurface=(float)(r/haloM);
    float[] dist=new float[]{fSurface,1f};
    Color[] cols=new Color[]{new Color(135,206,235,255),new Color(135,206,235,0)};
    Path2D.Double donut=new Path2D.Double(Path2D.WIND_EVEN_ODD);
    donut.append(new Ellipse2D.Double(cx-haloR,cy-haloR,haloR*2,haloR*2),false);
    donut.append(new Ellipse2D.Double(cx-innerR,cy-innerR,innerR*2,innerR*2),false);
    Paint old=g2.getPaint();
    g2.setPaint(new RadialGradientPaint(
      new Point2D.Float((float)cx,(float)cy),haloR,dist,cols,MultipleGradientPaint.CycleMethod.NO_CYCLE));
    g2.fill(donut);
    g2.setPaint(old);
  }
}
