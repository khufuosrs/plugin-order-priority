package net.runelite.client.plugins.sidepanelpriority;

import java.awt.*;

import javax.swing.*;

import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;

public class UIBuilder {

    // 1. Security Lock: Prevents accidental instantiation (The Anvil Chain)
    private UIBuilder() {}

    // 2. Custom UI Painters
    public static void applyShadowTextUI(JButton button) {
        button.setUI(new javax.swing.plaf.basic.BasicButtonUI() {
            @Override
            protected void paintText(Graphics g, JComponent c, Rectangle textRect, String text) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                int x = textRect.x;
                int y = textRect.y + g2d.getFontMetrics().getAscent();

                g2d.setColor(Color.BLACK);
                g2d.drawString(text, x + 1, y + 1);

                g2d.setColor(c.getForeground());
                g2d.drawString(text, x, y);
                g2d.dispose();
            }
        });
    }

    public static JButton createControlButton(String text, String tooltip) {
        JButton button = new JButton(text);
        button.setToolTipText(tooltip);
        button.setFont(FontManager.getRunescapeFont());
        button.setBackground(ColorScheme.DARK_GRAY_COLOR);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(new javax.swing.border.EmptyBorder(5, 5, 5, 5));

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(ColorScheme.DARKER_GRAY_HOVER_COLOR);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(ColorScheme.DARK_GRAY_COLOR);
            }
        });
        return button;
    }
}