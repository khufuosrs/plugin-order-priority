package net.runelite.client.plugins.sidepanelpriority;

import java.awt.*;

import javax.swing.*;
import javax.swing.plaf.basic.BasicButtonUI;

import lombok.Getter;
import lombok.Setter;

import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.NavigationButton;

public class PluginRow extends JPanel {

    // 1. Core Data
    @Getter
    private final String pluginName;

    @Getter @Setter
    private NavigationButton navButton;

    // 2. State Variables
    private boolean isSelected = false;
    @Getter
    private boolean isHidden;
    private Color normalBackgroundColor;

    // 3. UI Components & Icons
    private final JPanel contentPanel = new JPanel(new BorderLayout());
    private final JLabel numberLabel;
    private final JLabel nameLabel;
    private final JButton visibilityButton;
    private final ImageIcon visibleIcon;
    private final ImageIcon invisibleIcon;


    public PluginRow(SidePanelPriorityPanel parentPanel,int priorityNumber, NavigationButton navButton) {
        this.navButton = navButton;
        this.pluginName = navButton.getTooltip();
        setLayout(new BorderLayout());
        setOpaque(false);
        setPreferredSize(new Dimension(0, 40));

        /// 1. Fixed Number Slot (Custom Paint & 28px Buffer)
        numberLabel = new JLabel(priorityNumber + ". ") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2d.setFont(getFont());

                // Dynamically calculate the vertical center!
                FontMetrics metrics = g2d.getFontMetrics();
                int y = ((getHeight() - metrics.getHeight()) / 2) + metrics.getAscent();

                // Draw the black drop shadow offset by 1 pixel
                g2d.setColor(Color.BLACK);
                g2d.drawString(getText(), 1, y + 1);

                // Draw the main orange text centered over it
                g2d.setColor(ColorScheme.BRAND_ORANGE);
                g2d.drawString(getText(), 0, y);
                g2d.dispose();
            }
        };
        numberLabel.setFont(FontManager.getRunescapeFont().deriveFont(Font.BOLD));
        numberLabel.setPreferredSize(new Dimension(28, 30)); // Restoring your preferred 28-pixel buffer!

        /// 2. The Menu Box
        contentPanel.setOpaque(true); // NEW: Let the floor color show through!

        // Zebra Striping: Alternate background color based on priority number!
        normalBackgroundColor = (priorityNumber % 2 == 0)
                ? ColorScheme.DARKER_GRAY_COLOR
                : ColorScheme.DARK_GRAY_COLOR;

        // Re-initialize the menuBox before painting it!
        JPanel menuBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        menuBox.setOpaque(false);
        menuBox.setBackground(normalBackgroundColor);
        menuBox.setBorder(new javax.swing.border.EmptyBorder(8, 2, 7, 2));

        // Extract, Resize, and Sharpen the Plugin Icon uniformly to 20x20 pixels
        JLabel iconLabel = new JLabel();
        if (navButton.getIcon() != null) {
            java.awt.Image rawImage = navButton.getIcon();
            java.awt.Image smoothImage = rawImage.getScaledInstance(25, 25, java.awt.Image.SCALE_SMOOTH);
            iconLabel.setIcon(new ImageIcon(smoothImage));
        }

        nameLabel = new JLabel(pluginName);
        nameLabel.setFont(FontManager.getRunescapeFont());
        nameLabel.setForeground(Color.WHITE);

        menuBox.add(iconLabel);
        menuBox.add(nameLabel);

        // 3. The Visibility Toggle Button
        // 1. Borrow the raw images from the Loot Tracker folder
        java.awt.image.BufferedImage visibleImg = net.runelite.client.util.ImageUtil.loadImageResource(getClass(), "visible_icon.png");
        java.awt.image.BufferedImage invisibleImg = net.runelite.client.util.ImageUtil.loadImageResource(getClass(), "invisible_icon.png");

        // 2. Convert the raw images into UI Icons
        visibleIcon = new ImageIcon(visibleImg);
        invisibleIcon = new ImageIcon(invisibleImg);

        // 3. The Visibility Toggle Button
        visibilityButton = new JButton();
        visibilityButton.setIcon(this.isHidden ? invisibleIcon : visibleIcon);
        visibilityButton.setUI(new BasicButtonUI());
        visibilityButton.setPreferredSize(new Dimension(25, 15));
        visibilityButton.setBorderPainted(false);
        visibilityButton.setContentAreaFilled(false);

        // The Tripwire: Swap the icon instead of text!
        visibilityButton.addActionListener(e -> {
            boolean newState = !isHidden;
            setHidden(newState);
        });

        // 1. Create an invisible wrapper case using GridBagLayout
        JPanel buttonWrapper = new JPanel(new java.awt.GridBagLayout());
        buttonWrapper.setOpaque(false); // Make the case invisible so the zebra stripe shows through
        buttonWrapper.setBorder(new javax.swing.border.EmptyBorder(0, 0, 0, 2)); // Add a little padding on the right

        // 2. Put the button in the dead-center of the wrapper
        buttonWrapper.add(visibilityButton);

        visibilityButton.setVisible(false);

        // 5. Final Assembly
        add(numberLabel, BorderLayout.WEST); // Attach to the transparent master floor!
        contentPanel.add(menuBox, BorderLayout.CENTER);
        contentPanel.add(buttonWrapper, BorderLayout.EAST);
        add(contentPanel, BorderLayout.CENTER);

        addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                // 1. Tell the master panel to wipe all highlights!
                parentPanel.deselectAllRows();

                // 2. Highlight this specific row
                select();

                // 3. Register this row as the active target for the Up/Down buttons
                parentPanel.setActiveRow(PluginRow.this);
            }
            // ... keep your mouseEntered/Exited methods exactly as they are
        });

    }

    public void select() {
        isSelected = true;
        contentPanel.setBackground(ColorScheme.DARKER_GRAY_HOVER_COLOR);
        visibilityButton.setVisible(true);
    }

    public void unselect() {
        isSelected = false;
        contentPanel.setBackground(normalBackgroundColor); // Make sure this targets the master row!
        visibilityButton.setVisible(false);
    }

    public void setPriorityNumber(int newPriority) {
        numberLabel.setText(newPriority + ". ");

        // Recalculate zebra stripe live based on current position!
        normalBackgroundColor = (newPriority % 2 == 0)
                ? ColorScheme.DARKER_GRAY_COLOR
                : new java.awt.Color(35,35,35);

        // If the row isn't currently selected, instantly apply the new stripe color
        if (!isSelected) {
            contentPanel.setBackground(normalBackgroundColor);
        }
    }

    public void setHidden(boolean hidden) {
        this.isHidden = hidden;

        if (hidden) {
            nameLabel.setText("<html><strike>" + pluginName + "</strike></html>");
            nameLabel.setForeground(ColorScheme.MEDIUM_GRAY_COLOR);
            if (visibilityButton != null) visibilityButton.setIcon(invisibleIcon);
        } else {
            nameLabel.setText(pluginName);
            nameLabel.setForeground(Color.WHITE);
            if (visibilityButton != null) visibilityButton.setIcon(visibleIcon);
        }
    }

}