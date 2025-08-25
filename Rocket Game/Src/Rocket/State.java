// File: src/rocket/State.java
package rocket;
import java.awt.Color; import java.util.*; import static java.lang.Math.*;
public class State {
  public static final class Body {
    public String name; public double cx,cy,radius,mu,terrainAmp,terrainL,atmThick; public Color atmInner,atmOuter;
    public double vcx=0,vcy=0; // center velocity (m/s) for ground-frame collisions
    public Body(String n,double x,double y,double R,double M,double ta,double tl,double at,Color ai,Color ao){
      name=n; cx=x; cy=y; radius=R; mu=M; terrainAmp=ta; terrainL=Math.max(1.0,tl); atmThick=Math.max(0.0,at); atmInner=ai; atmOuter=ao;}
    public double k1(){ return (2*PI)/terrainL; }
  }
  public final List<Body> bodies=new ArrayList<>();
  // Rocket
  public double rx=0,ry=0,vx=0,vy=0,ang=0; public boolean leftHeld=false,rightHeld=false,upHeld=false,downHeld=false,paused=false,followRocket=false;
  public double throttle=0.0,timeScale=1.0; public int predHorizonSec=18000;
  // Orbits
  public double simTimeSec=0.0;

  public State(){ seedFromConfig(); reset(); }

  private void seedFromConfig(){
    bodies.clear();
    for(int i=0;i<Config.BODY_COUNT;i++){
      bodies.add(new Body(
        Config.BODY_NAME[i], Config.BODY_CX[i], Config.BODY_CY[i], Config.BODY_RADIUS[i], Config.BODY_MU[i],
        Config.BODY_TERRAIN_AMP[i], Config.BODY_TERRAIN_L[i],
        Config.BODY_ATM_THICK[i], new Color(Config.BODY_ATM_INNER_ARGB[i],true), new Color(Config.BODY_ATM_OUTER_ARGB[i],true)
      ));
    }
    updateOrbits();
  }

  public void reset(){
    simTimeSec=0.0;
    updateOrbits(); // ensures vcx/vcy are set for all bodies at t=0
    int earthIdx=indexOf("Earth");
    Body e=bodies.get(earthIdx);

    // Place rocket on Earth's surface directly "above" the center at t=0
    rx=e.cx; ry=e.cy + e.radius;

    // ✔ FIX: Inherit Earth's center velocity only (don't add A*ω again)
    vx=e.vcx; vy=e.vcy;

    ang=0; throttle=0; timeScale=1; paused=false; followRocket=true;
    predHorizonSec=18000;
  }

  public void advanceSimTime(double dtReal){ simTimeSec+=dtReal*timeScale; updateOrbits(); }

  public void updateOrbits(){
    // Two-pass feel: child velocities include parent velocities
    for(int i=0;i<Config.BODY_COUNT;i++){
      int mode=Config.ORBIT_MODE[i], c=Config.ORBIT_CENTER_IDX[i];
      Body b=bodies.get(i);
      if(mode==0 || c<0){
        b.cx=Config.BODY_CX[i]; b.cy=Config.BODY_CY[i]; b.vcx=0; b.vcy=0;
      }else{
        Body p=bodies.get(c);
        double A=Config.ORBIT_A_M[i], B=Config.ORBIT_B_M[i];
        double w=Config.ORBIT_OMEGA_RAD_PER_S[i];
        double th=Config.ORBIT_PHASE_RAD[i] + w*simTimeSec;
        b.cx = p.cx + A*cos(th);
        b.cy = p.cy + B*sin(th);
        double vx_rel = -A*w*sin(th);
        double vy_rel =  B*w*cos(th);
        b.vcx = p.vcx + vx_rel;
        b.vcy = p.vcy + vy_rel;
      }
    }
  }

  public void scaleTime(double mult){ timeScale=clamp(timeScale*mult,0.25,500.0); }
  public void setThrottle(double t){ throttle=clamp(t,0.0,1.0); }
  public void nudgePredHorizon(int s){ predHorizonSec=(int)clamp(predHorizonSec+s,10.0,(double)Config.PRED_HORIZON_MAX); }

  public Body nearestBody(double x,double y){
    Body best=null; double bestD2=Double.POSITIVE_INFINITY;
    for(Body b:bodies){ double dx=x-b.cx,dy=y-b.cy,d2=dx*dx+dy*dy; if(d2<bestD2){bestD2=d2; best=b;} }
    return best;
  }

  public static double clamp(double v,double lo,double hi){ return Math.max(lo,Math.min(hi,v)); }
  private int indexOf(String name){ for(int i=0;i<bodies.size();i++) if(bodies.get(i).name.equalsIgnoreCase(name)) return i; return 0; }
}
