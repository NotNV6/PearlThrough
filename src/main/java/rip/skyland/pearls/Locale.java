package rip.skyland.pearls;

import org.bukkit.configuration.file.FileConfiguration;

public enum Locale {

    PEARL_DAMAGE("general.pearl_damage", false),
    ANTI_GLITCH("general.anti_glitch", false),
    PEARL_THROUGH_OPEN_FENCE("general.pearl_through_open_fence", true),
    PEARL_THROUGH_TRIPWIRE("general.pearl_through_tripwire", true),
    PEARL_THROUGH_SLAB("taliban.pearl_through_slab", true),
    TALIBAN_PEARLING("taliban.taliban_pearling", true),
    PEARL_THROUGH_STAIR("taliban.pearl_through_stair", true),
    PEARL_THROUGH_FENCE("general.pearl_through_fence", true);

    private Object value;
    private String path;

    Locale(String path, Object value) {
        FileConfiguration config = PearlPlugin.getInstance().getConfig();
        this.path = path;

        if (config.contains(path)) {
            this.value = config.get(path);
            return;
        }
        this.value = value;
    }

    public void setValue(Object value) {
        this.value = value;
        PearlPlugin.getInstance().getConfig().set(path, value);
        PearlPlugin.getInstance().saveConfig();
    }

    public boolean getAsBoolean() {
        return (boolean) value;
    }

}