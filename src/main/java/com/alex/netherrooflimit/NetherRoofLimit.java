package com.alex.netherrooflimit;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.UUID;

public class NetherRoofLimit extends JavaPlugin implements Listener {

    private final HashMap<UUID, Location> entryPoints = new HashMap<>();

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
        getLogger().info("NetherRoofLimit plugin enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("NetherRoofLimit plugin disabled!");
    }

    @EventHandler
    public void onPlayerEnterNetherRoof(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Location loc = player.getLocation();
        World world = loc.getWorld();

        if (world == null || !world.getEnvironment().equals(World.Environment.NETHER)) return;

        UUID playerId = player.getUniqueId();

        if (loc.getY() >= 126) {
            // Record entry point if not already recorded
            if (!entryPoints.containsKey(playerId)) {
                entryPoints.put(playerId, loc);
                player.sendMessage(ChatColor.GREEN + "You have entered the Nether roof. You can travel freely within 64 blocks of this point.");
            }

            Location entryPoint = entryPoints.get(playerId);

            // Check if the player exceeds the 64-block radius (ignoring Y coordinate)
            if (entryPoint != null) {
                double distanceSquared = Math.pow(loc.getX() - entryPoint.getX(), 2) + Math.pow(loc.getZ() - entryPoint.getZ(), 2);
                if (distanceSquared > 64 * 64) {
                    player.teleport(entryPoint);
                    player.sendMessage(ChatColor.RED + "You have reached the 64-block travel limit on the Nether roof.");
                }
            }
        } else {
            // Reset status if player goes below the Nether roof
            entryPoints.remove(playerId);
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        UUID playerId = player.getUniqueId();

        // Remove entry point if player dies
        if (player.getWorld().getEnvironment().equals(World.Environment.NETHER)) {
            entryPoints.remove(playerId);
        }
    }

    @EventHandler
    public void onPlayerPortal(PlayerPortalEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();

        // Remove entry point when leaving the Nether
        if (event.getFrom().getWorld() != null && event.getFrom().getWorld().getEnvironment().equals(World.Environment.NETHER)) {
            entryPoints.remove(playerId);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        // Persist player entry points if needed
        // Currently, entryPoints is memory-only and will reset when the server restarts. Might fix someday
    }
}