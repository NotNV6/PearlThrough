package rip.skyland.pearls.entity;

import net.minecraft.server.v1_7_R4.*;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftPlayer;
import org.bukkit.entity.Player;
import rip.skyland.pearls.Locale;

public class CustomEnderpearl extends EntityEnderPearl {

    public CustomEnderpearl(Player player) {
        super(((CraftPlayer) player).getHandle().world, ((CraftPlayer) player).getHandle());
    }

    protected void a(MovingObjectPosition movingObjectPosition) {
        Block block = this.world.getType(movingObjectPosition.b, movingObjectPosition.c, movingObjectPosition.d);

        boolean shouldContinue = true;

        // check if it's a passable block
        if ((block == Blocks.TRIPWIRE && Locale.PEARL_THROUGH_TRIPWIRE.getAsBoolean()) ||
                (block == Blocks.FENCE_GATE && BlockFenceGate.b(this.world.getData(movingObjectPosition.b, movingObjectPosition.c, movingObjectPosition.d)) && Locale.PEARL_THROUGH_FENCE.getAsBoolean())
                //|| (block == Blocks.STEP && Locale.PEARL_THROUGH_SLAB.getAsBoolean())
                //|| (block.getName().toLowerCase().contains("stairs") && Locale.PEARL_THROUGH_STAIR.getAsBoolean())
        ) {
            shouldContinue = false;
        }

        Location location = this.getBukkitEntity().getLocation();

        // anti glitch thing
        if (location.getBlock().getType().isSolid()) {
            shouldContinue = false;
        }

        if (shouldContinue)
            super.a(movingObjectPosition);
    }
}
