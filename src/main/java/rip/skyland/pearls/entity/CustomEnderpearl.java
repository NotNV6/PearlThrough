package rip.skyland.pearls.entity;

import net.minecraft.server.v1_7_R4.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_7_R4.event.CraftEventFactory;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import rip.skyland.pearls.Locale;

import java.util.stream.IntStream;

// TODO: clean code
public class CustomEnderpearl extends EntityEnderPearl {

    private boolean passedThroughSlabOrStair = false;

    public CustomEnderpearl(Player player) {
        super(((CraftPlayer) player).getHandle().world, ((CraftPlayer) player).getHandle());
    }

    protected void a(MovingObjectPosition movingObjectPosition) {
        Block block = this.world.getType(movingObjectPosition.b, movingObjectPosition.c, movingObjectPosition.d);

        // check if it's a passable block
        if ((block == Blocks.TRIPWIRE && Locale.PEARL_THROUGH_TRIPWIRE.getAsBoolean()) ||
                (block == Blocks.FENCE_GATE && BlockFenceGate.b(this.world.getData(movingObjectPosition.b, movingObjectPosition.c, movingObjectPosition.d)) && Locale.PEARL_THROUGH_FENCE.getAsBoolean())
        ) {
            return;
        }


        // taliban pearls
        if ((block == Blocks.STEP && Locale.PEARL_THROUGH_SLAB.getAsBoolean()) || (block.getName().toLowerCase().contains("stairs") && Locale.PEARL_THROUGH_STAIR.getAsBoolean())) {
            this.passedThroughSlabOrStair = true;
            return;
        }

        // anti glitch thing
        if (Locale.ANTI_GLITCH.getAsBoolean() && this.getBukkitEntity().getLocation().getBlock().getType().isSolid()) {
            this.dead = true;
            return;
        }

        IntStream.range(0, 32).forEach(i -> this.world.addParticle("portal", this.locX, this.locY + this.random.nextDouble() * 2.0D, this.locZ, this.random.nextGaussian(), 0.0D, this.random.nextGaussian()));

        if (this.world.isStatic) {
            return;
        }

        // since when can other entities shoot enderpearls? (mojang logic)
        if (this.getShooter() == null || !(this.getShooter() instanceof EntityPlayer)) {
            this.dead = true;
            return;
        }

        if (((EntityPlayer) this.getShooter()).playerConnection.b().isConnected() && this.getShooter().world == this.world) {

            EntityPlayer entityplayer = (EntityPlayer) this.getShooter();
            CraftPlayer player = entityplayer.getBukkitEntity();

            Location location = this.getBukkitEntity().getLocation();
            location.setPitch(player.getLocation().getPitch());
            location.setYaw(player.getLocation().getYaw());

            // crit block for taliban pearling
            if (Locale.TALIBAN_PEARLING.getAsBoolean() && location.add(player.getEyeLocation().getDirection().multiply(1.5)).getBlock().getType().isSolid() && this.passedThroughSlabOrStair) {

                if (location.getBlock().getType().isSolid()) {
                    if (this.getClosestSafeLocation(location) != null) {
                        location = this.getClosestSafeLocation(location);
                    } else {
                        this.dead = true;
                        return;
                    }
                }
            }

            assert location != null;
            if(location.getBlock().getType().isSolid() && !location.add(0, 1, 0).getBlock().getType().isSolid()) {
                location.setY(location.getY()+1);
            }

            PlayerTeleportEvent event = new PlayerTeleportEvent(player, player.getLocation(), location, PlayerTeleportEvent.TeleportCause.ENDER_PEARL);
            Bukkit.getPluginManager().callEvent(event);

            if (!event.isCancelled() && !entityplayer.playerConnection.isDisconnected()) {
                if (this.getShooter().am()) {
                    this.getShooter().mount(null);
                }

                entityplayer.playerConnection.teleport(event.getTo());
                this.getShooter().fallDistance = 0.0F;

                if(Locale.PEARL_DAMAGE.getAsBoolean()) {
                    CraftEventFactory.entityDamage = this;
                    this.getShooter().damageEntity(DamageSource.FALL, 5.0F);
                    CraftEventFactory.entityDamage = null;
                }
            }
        }

        this.dead = true;
    }

    private Location getClosestSafeLocation(Location location1) {
        for (int x = 0; x < 3; x++) {
            for (int z = 0; z < 3; z++) {
                Location location = new Location(location1.getWorld(), location1.getBlockX(), location1.getBlockY(), location1.getBlockZ());
                location.setDirection(location1.getDirection());

                location.setX(location.getBlockX() - 1 + x);
                location.setY(location.getBlockY() + 1);
                location.setZ(location.getBlockZ() - 1 + z);

                if (!location.getBlock().getType().isSolid()) {
                    return location;
                }
            }
        }

        return null;
    }

}
