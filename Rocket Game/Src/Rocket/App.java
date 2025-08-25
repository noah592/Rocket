// File: src/rocket/App.java
package rocket;

import javax.swing.*;
import java.awt.*;

public class App {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            State state = new State();
            SimPanel panel = new SimPanel(state);

            JFrame f = new JFrame("Rocket — Modular");
            f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            f.setContentPane(panel);
            f.pack();
            f.setSize(new Dimension(Config.W, Config.H));
            f.setLocationRelativeTo(null);
            f.setVisible(true);

            // Input (keys, mouse wheel, drag pan)
            Input input = new Input(panel, state);
            panel.addKeyListener(input);
            panel.addMouseWheelListener(input);
            panel.addMouseListener(input);
            panel.addMouseMotionListener(input);
            panel.requestFocusInWindow();
        });
    }
}
