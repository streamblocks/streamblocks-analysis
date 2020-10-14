package ch.epfl.vlsc.analysis.core.visualization.util;

import javax.swing.*;
import java.awt.*;

public class ColorCodeLegend extends JPanel {

    private static final long serialVersionUID = 1L;

    public ColorCodeLegend() {
    }

    public void add(Color color, String text) {
        JLabel box = new JLabel("   ");
        box.setOpaque(true);
        box.setBackground(color);
        add(box);
        add(new JLabel(text));
        add(Box.createHorizontalStrut(40));
    }
}