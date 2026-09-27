package net.runelite.client.plugins.sidepanelpriority; // Ensure this matches your other files!

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("sidepanelpriority")
public interface SidePanelPriorityConfig extends Config {

    @ConfigItem(
            keyName = "savedOrder",
            name = "Saved Panel Order",
            description = "The hidden string that stores your custom sidebar order",
            hidden = true // We hide this so the user doesn't accidentally edit the raw text!
    )
    default String savedOrder() {
        return ""; // By default, the warehouse is empty
    }
}