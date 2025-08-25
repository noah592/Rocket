// File: src/rocket/Render.java
package rocket;

import java.awt.*;

public final class Render {
    private Render(){}

    public static void drawAll(Graphics2D g2, SimPanel v, State s, Config c) {
        // Background
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(Color.BLACK);
        g2.fillRect(0, 0, v.getWidth(), v.getHeight());

        // Stars
        DrawStars.draw(g2, v, c);

        // NEW: Orbits (behind bodies)
        DrawOrbits.draw(g2, v, s);

        // Bodies (surface + halos / horizons)
        DrawBodies.renderBodies(g2, v, s, c);

        // Predictive trajectory
        DrawPrediction.draw(g2, v, s, c);

        // Rocket
        DrawRocket.draw(g2, v, s, c);

        // HUD
        DrawHud.draw(g2, v, s, c);
    }
}
