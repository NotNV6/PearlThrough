package rip.skyland.pearls.entity;

import lombok.Getter;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R3.event.CraftEventFactory;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import rip.skyland.pearls.Locale;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.IntStream;

// TODO: clean code
public class CustomEnderpearl extends EntityEnderPearl {

    private boolean passedThroughSlabOrStair = false;
    private boolean passedThroughFence = false;

    @Getter
    private final ArrayList<Block> fenceTypes = new ArrayList<>(Arrays.asList(
            Blocks.FENCE, Blocks.ACACIA_FENCE, Blocks.BIRCH_FENCE,
            Blocks.DARK_OAK_FENCE, Blocks.JUNGLE_FENCE, Blocks.NETHER_BRICK_FENCE,
            Blocks.SPRUCE_FENCE, Blocks.FENCE
    ));

    @Getter
    private final ArrayList<Block> fenceGateTypes = new ArrayList<>(Arrays.asList(
            Blocks.FENCE_GATE, Blocks.ACACIA_FENCE_GATE, Blocks.BIRCH_FENCE_GATE,
            Blocks.DARK_OAK_FENCE_GATE, Blocks.JUNGLE_FENCE_GATE, Blocks.SPRUCE_FENCE_GATE
    ));

    public CustomEnderpearl(final Player player) {
        super(((CraftPlayer) player).getHandle().world, ((CraftPlayer) player).getHandle());
    }

    protected void a(final MovingObjectPosition movingObjectPosition) {
        final Block block;

        //fix crash by ticking entity
        try {
            block = this.world.getType(movingObjectPosition.a()).getBlock();
        } catch (final Exception ex) { return; }

        // check if it's a tripwire
        if (block == Blocks.TRIPWIRE && Locale.PEARL_THROUGH_TRIPWIRE.getAsBoolean()) return;

        if (getFenceGateTypes().contains(block) && BlockFenceGate.a(block, block) && Locale.PEARL_THROUGH_OPEN_FENCE.getAsBoolean()) {
            this.passedThroughFence = true;
            return;
        }

        if (getFenceTypes().contains(block) && Locale.PEARL_THROUGH_FENCE.getAsBoolean()) {
            this.passedThroughFence = true;
            return;
        }

        // taliban pearls
        // block.getMaterial().equals(Material.STEP) isn't the same type (always false?)
        if ((block.getMaterial().equals(Material.STEP) && Locale.PEARL_THROUGH_SLAB.getAsBoolean()) || (block.getName().toLowerCase().contains("stairs") && Locale.PEARL_THROUGH_STAIR.getAsBoolean())) {
            this.passedThroughSlabOrStair = true;
            return;
        }

        // anti glitch thing
        if (Locale.ANTI_GLITCH.getAsBoolean() && this.getBukkitEntity().getLocation().getBlock().getType().isSolid()) {
            this.dead = true;
            return;
        }

        if (this.world.isClientSide) return;

        // since when can other entities shoot enderpearls? (mojang logic)
        if (this.getShooter() == null || !(this.getShooter() instanceof EntityPlayer)) {
            this.dead = true;
            return;
        }

        final EntityPlayer entityShooter = (EntityPlayer) this.getShooter();

        if (!(entityShooter.playerConnection.isDisconnected()) && entityShooter.world == this.world) {

            final CraftPlayer player = entityShooter.getBukkitEntity();

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

            if (location.getBlock().getType().isSolid() && !toCheck.add(0, 1, 0).getBlock().getType().isSolid()) {
                location.setY(location.getY()+1);
            }

            toCheck = getCheckableLocation(location);
            for (int i = 1; i < 2; i++) {
                if(toCheck.add(i, i, i).getBlock().getType().isSolid() && !passableBlock(block, movingObjectPosition) && !passedThroughFence && !passedThroughSlabOrStair) {
                    this.dead = true;
                    return;
                }
            }

            if (location.getBlock().getType().isSolid()) {
                this.dead = true;
                return;
            }

            PlayerTeleportEvent event = new PlayerTeleportEvent(player, player.getLocation(), location, PlayerTeleportEvent.TeleportCause.ENDER_PEARL);
            Bukkit.getPluginManager().callEvent(event);

            if (!(event.isCancelled()) && !(entityShooter.playerConnection.isDisconnected())) {
                /*if (this.getShooter().am()) {
                    this.getShooter().mount(null);
                }*/

                entityShooter.playerConnection.teleport(event.getTo());
                IntStream.range(0, 32).forEach(i ->
                        this.world.addParticle(EnumParticle.PORTAL, this.locX, this.locY + this.random.nextDouble() * 2.0D, this.locZ, this.random.nextGaussian(), 0.0D, this.random.nextGaussian())
                );

                this.getShooter().fallDistance = 0.0F;

                if (Locale.PEARL_DAMAGE.getAsBoolean()) {
                    CraftEventFactory.entityDamage = this;
                    this.getShooter().damageEntity(DamageSource.FALL, 5.0F);
                    CraftEventFactory.entityDamage = null;
                }
            }
        }
        this.dead = true;
    }

    private Location getClosestSafeLocation(final Location location) {
        for (int i = 0; i < 3; i++) {
            if (!(location.add(location.getDirection().multiply(i)).getBlock().getType().isSolid())) return location.add(location.getDirection().multiply(1));
        }
        return null;
    }

    private Location getCheckableLocation(final Location originalLocation) {
        Location location = new Location(originalLocation.getWorld(), originalLocation.getBlockX(), originalLocation.getBlockY(), originalLocation.getBlockZ());
        location.setDirection(originalLocation.getDirection());
        return location;
    }

    private boolean passableBlock(final Block block, final MovingObjectPosition movingObjectPosition) {
        return (block.equals(Blocks.TRIPWIRE) && Locale.PEARL_THROUGH_TRIPWIRE.getAsBoolean()) &&
                (getFenceGateTypes().contains(block) && BlockFenceGate.a(block, block) && Locale.PEARL_THROUGH_OPEN_FENCE.getAsBoolean());
    }

}
