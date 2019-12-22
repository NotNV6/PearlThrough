package rip.skyland.pearls;

import org.bukkit.configuration.file.FileConfiguration;

import java.util.Collections;
import java.util.List;


public enum Locale {

    PEARL_THROUGH_FENCE("general.pearl_through_open_fence", true),
    PEARL_THROUGH_TRIPWIRE("general.pearl_through_tripwire", true),
    PEARL_THROUGH_SLAB("general.pearl_through_slab", true),
    PEARL_THROUGH_STAIR("general.pearl_through_stair", true);

    Object value;

    Locale(String path, Object value) {
        FileConfiguration config = PearlPlugin.getInstance().getConfig();

        if(config.contains(path))
            this.value = config.get(path);
        else
            this.value = value;
    }

    public boolean getAsBoolean() {
        return (boolean) value;
    }

}
