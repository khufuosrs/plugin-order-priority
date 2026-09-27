package net.runelite.client.plugins.sidepanelpriority;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import net.runelite.client.config.ConfigManager;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.ui.PluginPanel;
import org.jetbrains.annotations.NotNull;

import lombok.Getter;
import lombok.Setter;

public class SidePanelPriorityPanel extends PluginPanel {

    private final ClientToolbar clientToolbar;
    private final ConfigManager configManager;

    private final List<PluginRow> allRows = new ArrayList<>();
    @Setter
    @Getter
    private PluginRow activeRow = null;

    private final JPanel listContainer;

    public SidePanelPriorityPanel(ClientToolbar clientToolbar, ConfigManager configManager) {
        super();
        this.clientToolbar = clientToolbar;
        this.configManager = configManager;

        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(10, 10, 10, 5));

        // 1. Top: The Title and Master Controls
        add(buildHeaderPanel(), BorderLayout.NORTH);

        // 2. Center: The Dynamic Plugin List
        listContainer = new JPanel();
        listContainer.setLayout(new BoxLayout(listContainer, BoxLayout.Y_AXIS));
        add(listContainer, BorderLayout.CENTER);

        // 3. Bottom: The Save and Reset Actions
        add(buildActionFooter(), BorderLayout.SOUTH);
    }

    private JPanel buildHeaderPanel() {
        // 1. Stylized Title
        JLabel titleLabel = createTitleLabel();

        // 2. Master Control Bar (Using UIBuilder!)
        JPanel controlBar = new JPanel(new GridLayout(1, 4, 2, 0));
        controlBar.setBorder(new EmptyBorder(0, 28, 0, 28));

        JButton topButton = UIBuilder.createControlButton("△", "Move to Top");
        JButton upButton = UIBuilder.createControlButton("▲", "Move Up");
        JButton downButton = UIBuilder.createControlButton("▼", "Move Down");
        JButton bottomButton = UIBuilder.createControlButton("▽", "Move to Bottom");

        upButton.addActionListener(e -> { if (getActiveRow() != null) moveRowUp(getActiveRow()); });
        downButton.addActionListener(e -> { if (getActiveRow() != null) moveRowDown(getActiveRow()); });
        topButton.addActionListener(e -> { if (getActiveRow() != null) moveRowToTop(getActiveRow()); });
        bottomButton.addActionListener(e -> { if (getActiveRow() != null) moveRowToBottom(getActiveRow()); });

        controlBar.add(topButton);
        controlBar.add(upButton);
        controlBar.add(downButton);
        controlBar.add(bottomButton);

        // 3. Assembly
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBorder(new EmptyBorder(0, 0, 5, 0));
        headerPanel.add(titleLabel, BorderLayout.NORTH);
        headerPanel.add(controlBar, BorderLayout.SOUTH);

        return headerPanel;
    }

    private JPanel buildActionFooter() {
        JPanel buttonBox = new JPanel(new GridLayout(2, 1, 0, 5));
        buttonBox.setBorder(new EmptyBorder(15, 28, 5, 28));

        JButton applyButton = new JButton("Save & Apply Order");
        applyButton.setBackground(new Color(40, 167, 69));
        applyButton.setForeground(Color.WHITE);
        applyButton.setFocusPainted(false);
        UIBuilder.applyShadowTextUI(applyButton);
        applyButton.addActionListener(e -> applyNewOrder(true));

        JButton resetButton = createResetButton(configManager);

        buttonBox.add(applyButton);
        buttonBox.add(resetButton);

        return buttonBox;
    }

    public void applyNewOrder(boolean saveToDatabase) {
        if (saveToDatabase) {
            PriorityStorage.saveOrder(configManager, allRows); // Delegating to the new Storage class!
        }

        System.out.println("--- EXECUTING CLONING REORGANIZATION ---");
        // ... remainder of method stays exactly the same

        // PASS 1: Nuke the board completely
        for (PluginRow row : allRows) {
            clientToolbar.removeNavigation(row.getNavButton());
        }

        // Wait 100ms for the UI to register the empty board
        new java.util.Timer().schedule(new java.util.TimerTask() {
            @Override
            public void run() {
                javax.swing.SwingUtilities.invokeLater(() -> {
                    int newPriorityNumber = 1;

                    // PASS 2: Build fresh clones and add them to the UI
                    for (PluginRow row : allRows) {
                        NavigationButton oldButton = row.getNavButton();

                        NavigationButton clonedButton = NavigationButton.builder()
                                .tooltip(oldButton.getTooltip())
                                .icon(oldButton.getIcon())
                                .panel(oldButton.getPanel())
                                .priority(newPriorityNumber)
                                .build();

                        // Update the Banker's clipboard with the new live ticket!
                        row.setNavButton(clonedButton);

                        // NEW: Only hand the clone to RuneLite if it is NOT hidden!
                        if (!row.isHidden()) {
                            clientToolbar.addNavigation(clonedButton);
                            newPriorityNumber++;
                        }
                    }
                    System.out.println("--- CLONING COMPLETE ---");
                });
            }
        }, 100);
    }
    public void loadAndApplySavedOrder() {
        PriorityStorage.loadOrder(configManager, allRows);
        refreshUI();
        applyNewOrder(false);
    }
    private void refreshUI() {
        listContainer.removeAll();

        int currentPriority = 1;

        for (PluginRow row : allRows) {
            row.setPriorityNumber(currentPriority);
            listContainer.add(row);
            currentPriority++;
        }

        listContainer.revalidate();
        listContainer.repaint();
        printManifest();
    }
    public void printManifest() {
        System.out.println("--- CURRENT PLUGIN ORDER ---");
        for (PluginRow row : allRows) {
            System.out.println(row.getPluginName());
        }
    }

    public void moveRowUp(PluginRow row) {
        int currentIndex = allRows.indexOf(row);

        if (currentIndex > 0) {
            allRows.remove(currentIndex);
            allRows.add(currentIndex - 1, row);
            refreshUI();
        }
    }
    public void moveRowDown(PluginRow row) {
        int currentIndex = allRows.indexOf(row);

        if (currentIndex < allRows.size() - 1) {
            allRows.remove(currentIndex);
            allRows.add(currentIndex + 1, row);
            refreshUI();
        }
        }
    public void moveRowToTop(PluginRow row) {
        int currentIndex = allRows.indexOf(row);
        if (currentIndex > 0) {
            allRows.remove(currentIndex);
            allRows.add(0, row); // Insert at the absolute beginning
            refreshUI();
        }
    }
    public void moveRowToBottom(PluginRow row) {
        int currentIndex = allRows.indexOf(row);
        if (currentIndex != -1 && currentIndex < allRows.size() - 1) {
            allRows.remove(currentIndex);
            allRows.add(allRows.size(), row); // Insert at the absolute end
            refreshUI();
        }
    }

    public void addPluginToPanel(NavigationButton navButton) {
        int nextPriority = allRows.size() + 1;

        PluginRow newRow = new PluginRow(this, nextPriority, navButton);

        allRows.add(newRow);
        refreshUI();
    }
    public void deselectAllRows() {
        for (PluginRow row : allRows) {
            row.unselect();
        }
    }

    private @NotNull JLabel createTitleLabel() {
        JLabel titleLabel = new JLabel("Plugin Order Priority", SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2d.setFont(getFont());
                FontMetrics metrics = g2d.getFontMetrics();
                int x = (getWidth() - metrics.stringWidth(getText())) / 2;

                g2d.setColor(Color.BLACK);
                g2d.drawString(getText(), x + 2, 28);
                g2d.setColor(ColorScheme.BRAND_ORANGE);
                g2d.drawString(getText(), x, 26);
                g2d.dispose();
            }
        };
        titleLabel.setFont(FontManager.getRunescapeFont().deriveFont(Font.BOLD, 26f));
        titleLabel.setPreferredSize(new Dimension(0, 38));
        return titleLabel;
    }
    private @NotNull JButton createResetButton(ConfigManager configManager) {
        JButton resetButton = new JButton("Reset to Default");
        resetButton.setBackground(new java.awt.Color(220, 53, 69)); // Clean UI Red
        resetButton.setForeground(Color.WHITE); // White text
        resetButton.setFocusPainted(false);
        UIBuilder.applyShadowTextUI(resetButton);
        resetButton.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "Reset layout to default? This clears your custom save.",
                    "Confirm Reset",
                    JOptionPane.YES_NO_OPTION
            );

            if (confirm == JOptionPane.YES_OPTION) {
                // 1. Wipe the persistent vault entry
                configManager.unsetConfiguration("sidepanelpriority", "savedOrder");

                // 2. Inform the user to restart
                JOptionPane.showMessageDialog(
                        this,
                        "Layout reset. Please restart RuneLite to restore default ordering.",
                        "Restart Required",
                        JOptionPane.INFORMATION_MESSAGE
                );
            }
        });
        return resetButton;
    }
}
