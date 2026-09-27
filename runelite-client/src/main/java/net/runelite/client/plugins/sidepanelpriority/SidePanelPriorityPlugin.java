package net.runelite.client.plugins.sidepanelpriority;

import java.lang.reflect.Field;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import javax.inject.Inject;
import javax.swing.SwingUtilities;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;

import net.runelite.client.config.ConfigManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.util.ImageUtil;

@PluginDescriptor(
        name = "Plugin Order Priority",
        description = "Allows users to sort and prioritize side panel icons.",
        tags = {"ui", "panel", "sort", "priority", "plugin", "order", "icon"}
)
@Slf4j
@SuppressWarnings("unused") // NEW: Tells the IDE that RuneLite handles this behind the scenes
public class SidePanelPriorityPlugin extends Plugin {

    @Inject
    private ClientToolbar clientToolbar;
    @Inject
    private ConfigManager configManager;
    @Inject
    private SidePanelPriorityConfig config;

    private SidePanelPriorityPanel panel;
    private NavigationButton navButton;

    @Override
    protected void startUp() throws Exception {
        panel = new SidePanelPriorityPanel(clientToolbar, configManager);
        navButton = NavigationButton.builder()
                .tooltip("Plugin Order Priority")
                .icon(ImageUtil.loadImageResource(getClass(), "icon.png"))
                .panel(panel)
                .build();
        clientToolbar.addNavigation(navButton);

        log.debug("Commencing targeted sidebar UI extraction.");
        extractSidebarEntries(); // Method Extraction: Delegating the heavy lifting!
    }

    private void extractSidebarEntries() {
        try {
            Field clientUiField = clientToolbar.getClass().getDeclaredField("clientUI");
            clientUiField.setAccessible(true);
            Object clientUiObject = clientUiField.get(clientToolbar);

            Field entriesField = clientUiObject.getClass().getDeclaredField("sidebarEntries");
            entriesField.setAccessible(true);
            Object entriesObj = entriesField.get(clientUiObject);

            if (entriesObj instanceof Set) {
                // The spell successfully grabbed the data! Next, we pass it to the timer.
                scheduleSidebarLoad((Set<?>) entriesObj);
            }
        } catch (Exception e) {
            log.error("Failed to extract sidebar UI.", e);
        }
    }

    private void scheduleSidebarLoad(Set<?> entriesSet) {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                log.debug("3-second initialization delay complete. Processing sidebar.");
                SwingUtilities.invokeLater(() -> {
                    for (Object item : entriesSet) {
                        if (item instanceof NavigationButton) {
                            panel.addPluginToPanel((NavigationButton) item);
                        }
                    }
                    panel.loadAndApplySavedOrder();
                });
            }
        }, 3000);
    }

    @Override
    protected void shutDown() throws Exception {
        clientToolbar.removeNavigation(navButton);
    }

    @Provides
    SidePanelPriorityConfig provideConfig(net.runelite.client.config.ConfigManager configManager) {
        return configManager.getConfig(SidePanelPriorityConfig.class);
    }

}