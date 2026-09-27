package net.runelite.client.plugins.sidepanelpriority;

import java.util.ArrayList;
import java.util.List;

import net.runelite.client.config.ConfigManager;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PriorityStorage {

    // 1. Security Lock: Prevents accidental instantiation (The Anvil Chain)
    private PriorityStorage() {}

    // 2. Database Write Operation
    public static void saveOrder(ConfigManager configManager, List<PluginRow> allRows) {
        List<String> pluginNames = new ArrayList<>();

        for (PluginRow row : allRows) {
            pluginNames.add(row.getPluginName() + ":" + row.isHidden());
        }

        String finalString = String.join(",", pluginNames);
        configManager.setConfiguration("sidepanelpriority", "savedOrder", finalString);
        log.debug("SUCCESSFULLY WRITTEN TO DATABASE: {}", finalString);
    }

    // 3. Database Read Operation
    public static void loadOrder(ConfigManager configManager, List<PluginRow> allRows) {
        String savedString = configManager.getConfiguration("sidepanelpriority", "savedOrder");
        if (savedString == null || savedString.isEmpty()) return;

        String[] savedItems = savedString.split(",");
        List<String> savedNames = new ArrayList<>();

        for (String item : savedItems) {
            String[] parts = item.split(":");
            if (parts.length == 2) {
                savedNames.add(parts[0].trim());
                for (PluginRow row : allRows) {
                    if (row.getPluginName().trim().equals(parts[0].trim())) {
                        row.setHidden(Boolean.parseBoolean(parts[1].trim()));
                    }
                }
            }
        }

        allRows.sort((row1, row2) -> {
            int index1 = savedNames.indexOf(row1.getPluginName().trim());
            int index2 = savedNames.indexOf(row2.getPluginName().trim());
            return Integer.compare(index1 == -1 ? 999 : index1, index2 == -1 ? 999 : index2);
        });
    }
}