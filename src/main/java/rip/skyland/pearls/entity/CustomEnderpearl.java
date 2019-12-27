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
    private boolean passedThroughFence = false;

    public CustomEnderpearl(Player player) {
        super(((CraftPlayer) player).getHandle().world, ((CraftPlayer) player).getHandle());
    }

    protected void a(MovingObjectPosition movingObjectPosition) {
        Block block = this.world.getType(movingObjectPosition.b, movingObjectPosition.c, movingObjectPosition.d);

        // check if it's a tripwire
        if ((block == Blocks.TRIPWIRE && Locale.PEARL_THROUGH_TRIPWIRE.getAsBoolean())) {
            return;
        }

        if((block == Blocks.FENCE_GATE && BlockFenceGate.b(this.world.getData(movingObjectPosition.b, movingObjectPosition.c, movingObjectPosition.d)) && Locale.PEARL_THROUGH_FENCE.getAsBoolean())) {
            this.passedThroughFence = true;
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
            Location toCheck = getCheckableLocation(location);


            if (Locale.TALIBAN_PEARLING.getAsBoolean() && toCheck.add(player.getEyeLocation().getDirection().multiply(1.5)).getBlock().getType().isSolid() && this.passedThroughSlabOrStair) {

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
            toCheck = getCheckableLocation(location);

            if(location.getBlock().getType().isSolid() && !toCheck.add(0, 1, 0).getBlock().getType().isSolid()) {
                location.setY(location.getY()+1);
            }

            toCheck = getCheckableLocation(location);
            for(int i = 1; i < 2; i++) {
                if(toCheck.add(i, i, i).getBlock().getType().isSolid() && !passableBlock(block, movingObjectPosition) && !passedThroughFence && !passedThroughSlabOrStair) {
                    this.dead = true;
                    return;
                }
            }

            if(location.getBlock().getType().isSolid()) {
                this.dead = true;
                return;
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
        for(int i = 0; i < 3; i++) {
            if(!location1.add(location1.getDirection().multiply(i)).getBlock().getType().isSolid())
                return location1.add(location1.getDirection().multiply(1));
        }

        return null;
    }

    private Location getCheckableLocation(Location originalLocation) {
        Location location = new Location(originalLocation.getWorld(), originalLocation.getBlockX(), originalLocation.getBlockY(), originalLocation.getBlockZ());
        location.setDirection(originalLocation.getDirection());

        return location;
    }

    private boolean passableBlock(Block block, MovingObjectPosition movingObjectPosition) {
        return  (block.equals(Blocks.TRIPWIRE) && Locale.PEARL_THROUGH_TRIPWIRE.getAsBoolean()) &&
                (block.equals(Blocks.FENCE_GATE) && BlockFenceGate.b(this.world.getData(movingObjectPosition.b, movingObjectPosition.c, movingObjectPosition.d)) && Locale.PEARL_THROUGH_FENCE.getAsBoolean());
    }

}
