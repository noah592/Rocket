// File: src/rocket/DrawStars.java
package rocket;
import java.awt.*; import java.util.Random;
public final class DrawStars {
    private DrawStars(){}
    private static int cw=-1,ch=-1; private static int[] xs=new int[0], ys=new int[0], ss=new int[0];
    public static void draw(Graphics2D g2, SimPanel v, Config c){
        int w=Math.max(1,v.getWidth()), h=Math.max(1,v.getHeight());
        if(w!=cw||h!=ch) regen(w,h);
        g2.setColor(new Color(Config.COL_STAR_ARGB,true));
        for(int i=0;i<xs.length;i++) g2.fillRect(xs[i],ys[i],ss[i],ss[i]);
    }
    private static void regen(int w,int h){
        Random rng=new Random(Config.STAR_SEED);
        java.util.ArrayList<Integer> X=new java.util.ArrayList<>(), Y=new java.util.ArrayList<>(), S=new java.util.ArrayList<>();
        int cols=Math.max(1,(int)Math.ceil(w/(double)Config.STAR_CELL_PX));
        int rows=Math.max(1,(int)Math.ceil(h/(double)Config.STAR_CELL_PX));
        for(int gy=0;gy<rows;gy++) for(int gx=0;gx<cols;gx++){
            if(rng.nextDouble()>Config.STAR_DENSITY) continue;
            int cellX=gx*Config.STAR_CELL_PX, cellY=gy*Config.STAR_CELL_PX;
            int jx=(int)Math.round((rng.nextDouble()-0.5)*(Config.STAR_CELL_PX*0.8));
            int jy=(int)Math.round((rng.nextDouble()-0.5)*(Config.STAR_CELL_PX*0.8));
            int sx=cellX+Config.STAR_CELL_PX/2+jx, sy=cellY+Config.STAR_CELL_PX/2+jy;
            if(sx<0||sx>=w||sy<0||sy>=h) continue;
            int size=(rng.nextDouble()<0.88)?1:2; X.add(sx); Y.add(sy); S.add(size);
        }
        xs=X.stream().mapToInt(Integer::intValue).toArray();
        ys=Y.stream().mapToInt(Integer::intValue).toArray();
        ss=S.stream().mapToInt(Integer::intValue).toArray();
        cw=w; ch=h;
    }
}
