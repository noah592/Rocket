// File: src/rocket/Config.java
package rocket;

public class Config {
  // Window/loop
  public static final int W=1200,H=800,FPS=60;

  // Rocket/physics
  public static final double BODY_H=50.0,BODY_W=5.0,CONE_H=10.0,FIN_LEN=6.0,FIN_DROP=6.0,MAX_THRUST_G=4.0,G0=9.80665;
  public static final double ANG_SPEED_RAD=1.361357; // ~78 deg/s
  public static final double THROTTLE_RATE=0.8;

  // Prediction
  public static final double BASE_PRED_DT=0.5;
  public static final int PRED_POINTS_MAX=2000,PRED_HORIZON_MAX=18000;

  // Camera/zoom
  public static final double MIN_PX_PER_M=1e-11, MAX_PX_PER_M=800.0;

  // Body rendering / arcs
  public static final double FILL_MAX_RADIUS_PX=8000.0,ARC_SPAN_MAX_RAD=1.3;
  public static final int ARC_SAMPLES=128,ARC_SEGMENT_PX=220,ARC_MAX_SEGMENTS=60,ATM_BANDS=40;

  // Starfield
  public static final int STAR_CELL_PX=28; public static final double STAR_DENSITY=0.80; public static final long STAR_SEED=42L;

  // Colors (ARGB)
  public static final int COL_EARTH_BLUE_ARGB=0xFF286EC8, COL_EARTH_EDGE_ARGB=0xFF78AAE6, COL_STAR_ARGB=0xFFF0F0FF, COL_PRED_ARGB=0x46B4C8FF;
  public static final int COL_ATM_INNER_ARGB=0x965AA0E6, COL_ATM_OUTER_ARGB=0x005AA0E6, COL_SUN_GLOW_INNER_ARGB=0xA0FFAA28, COL_SUN_GLOW_OUTER_ARGB=0x00FFAA28;
  public static final int COL_NONE_ARGB=0x00000000;

  // HUD basics
  public static final String HUD_FONT_FAMILY="Consolas", LABEL_FONT_FAMILY="Consolas";
  public static final int HUD_FONT_SIZE=16, LABEL_FONT_SIZE=14;
  public static final int HUD_MARGIN=10;
  public static final String CONTROLS_LEGEND="F cam mode  |  -/= time×  |  click planet to follow  |  wheel zoom, drag pan";

  // -------- Bodies (Sun-centric). Order: Sun, Mercury, Venus, Earth, Moon, Mars, Jupiter, Saturn, Uranus, Neptune
  public static final int BODY_COUNT=10;
  public static final String[] BODY_NAME={"Sun","Mercury","Venus","Earth","Moon","Mars","Jupiter","Saturn","Uranus","Neptune"};
  public static final double[] BODY_CX={0,0,0,0,0,0,0,0,0,0};
  public static final double[] BODY_CY={0,0,0,0,0,0,0,0,0,0};
  public static final double[] BODY_RADIUS={
    696_340_000.0,2_439_700.0,6_051_800.0,6_371_000.0,1_737_400.0,3_389_500.0,69_911_000.0,58_232_000.0,25_362_000.0,24_622_000.0
  };
  public static final double[] BODY_MU={
    1.32712440018e20,2.2032e13,3.24859e14,3.986004418e14,4.9048695e12,4.282837e13,1.26686534e17,3.7931187e16,5.793939e15,6.836529e15
  };
  public static final double[] BODY_TERRAIN_AMP={0.0,800,500,1000,800,1200,0,0,0,0};
  public static final double[] BODY_TERRAIN_L  ={5000,5000,5000,5000,5000,8000,5000,5000,5000,5000};
  public static final double[] BODY_ATM_THICK={13_115_806.0,0,0,120_000.0,0,0,0,0,0,0};
  public static final int[] BODY_ATM_INNER_ARGB={
    COL_SUN_GLOW_INNER_ARGB, COL_NONE_ARGB, COL_NONE_ARGB, COL_ATM_INNER_ARGB, COL_NONE_ARGB, COL_NONE_ARGB, COL_NONE_ARGB, COL_NONE_ARGB, COL_NONE_ARGB, COL_NONE_ARGB
  };
  public static final int[] BODY_ATM_OUTER_ARGB={
    COL_SUN_GLOW_OUTER_ARGB, COL_NONE_ARGB, COL_NONE_ARGB, COL_ATM_OUTER_ARGB, COL_NONE_ARGB, COL_NONE_ARGB, COL_NONE_ARGB, COL_NONE_ARGB, COL_NONE_ARGB, COL_NONE_ARGB
  };
  public static final int[] BODY_COLOR_ARGB={
    0xFFFFAA28,0xFFB5B5B5,0xFFEED28E,0xFF69B36B,0xFFBFBFBF,0xFFB04A2E,0xFFD7A26D,0xFFE6C27A,0xFF78D3E1,0xFF2F5DDE
  };

  // ---------- ORBITS (on-rails) ----------
  public static final int[] ORBIT_MODE={0,1,1,1,1,1,1,1,1,1};
  public static final int[] ORBIT_CENTER_IDX={-1,0,0,0,3,0,0,0,0,0};
  public static final double[] ORBIT_A_M={
    0,57_909_227_000.0,108_209_475_000.0,149_597_870_700.0,384_400_000.0,227_939_200_000.0,778_299_000_000.0,1_433_449_370_000.0,2_872_466_000_000.0,4_495_060_000_000.0
  };
  public static final double[] ORBIT_B_M=ORBIT_A_M.clone();
  public static final double[] ORBIT_PERIOD_S={
    0.0,7_600_530.24,19_414_166.4,31_558_149.7635456,2_360_591.5104,59_355_072.0,374_335_689.6,929_596_608.0,2_651_218_560.0,5_200_329_600.0
  };
  public static final double[] ORBIT_PHASE_RAD={0,0,0,0,0,0,0,0,0,0};
  public static final double[] ORBIT_OMEGA_RAD_PER_S={
    0.0,8.266772328741615e-07,3.2363920127827824e-07,1.9909865927683785e-07,2.6616995272150692e-06,
    1.0585759726109988e-07,1.6784895166938380e-08,6.7590450020011110e-09,2.3699235521267574e-09,1.2082282836802472e-09
  };

  // Arrow LOD
  public static final double ARROW_MIN_HEIGHT_PX=15.0, ARROW_SIZE_PX=15.0;

  // --- New: HUD buttons + time limits ---
  public static final int HUD_BTN_SIZE=22, HUD_BTN_GAP=8;
  public static final double TIME_SCALE_MIN=0.25, TIME_SCALE_MAX=500.0;
  public static final double TIME_SCALE_STEP_DOWN=0.5, TIME_SCALE_STEP_UP=2.0;

  // --- New: label culling & tiny-body rule ---
  public static final double LABEL_HIDE_BELOW_RPX=10.0;   // don't label if body radius on screen < 10 px
  public static final double TINY_BODY_THRESH_PX=3.5;     // show 2px white dot if below this
}
