package me.AhmHkn;

import net.minecraft.server.v1_8_R3.IChatBaseComponent;
import net.minecraft.server.v1_8_R3.PacketPlayOutChat;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;


public class EnderpearlCooldown extends JavaPlugin implements Listener {

    public static EnderpearlCooldown plugin;
    public static HashMap<Player, Integer> liste;
    public static Boolean epblok;

    public void onEnable() {
        saveDefaultConfig();
        plugin = this;
        liste = new HashMap<>();
        epblok = getConfig().getBoolean("EPBlok");
        getServer().getPluginManager().registerEvents(this, this);
    }


    @EventHandler
    public void EnderPearl(PlayerInteractEvent e) {
        if (e.getPlayer().getItemInHand() != null) {
            if (!e.getPlayer().getItemInHand().getType().equals(Material.ENDER_PEARL))
                return;
            if (liste.containsKey(e.getPlayer())) {
                e.setCancelled(true);
                e.setUseItemInHand(Event.Result.DENY);
                return;
            }
            if (epblok && !e.isCancelled()) {
                e.setUseItemInHand(Event.Result.ALLOW);
            }
        }
    }


    @EventHandler
    public void onEnderPearlShoot(ProjectileLaunchEvent e) {
        if (!(e.getEntity() instanceof EnderPearl))
            return;
        if (!((e.getEntity().getShooter()) instanceof Player))
            return;
        Player p = (Player) e.getEntity().getShooter();
        if (liste.containsKey(p)) {
            e.setCancelled(true);
            return;
        }
        if (e.isCancelled())
            return;
        if (!p.hasPermission("enderpearl.cooldown.bypass")) {
            liste.put(p, cd());
            actionBar(p);
        }
    }



    public void actionBar(final Player p) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!liste.containsKey(p))
                    cancel();

                if (liste.get(p) > 0) {
                    String gerisayim = plugin.getConfig().getString("mesaj").replace("%sure%", String.valueOf(liste.get(p))).replace("&", "§");
                    PacketPlayOutChat packet = new PacketPlayOutChat(IChatBaseComponent.ChatSerializer.a("{\"text\":\"" + gerisayim.replace("&", "§") + "\"}"), (byte) 2);
                    ((CraftPlayer) p).getHandle().playerConnection.sendPacket(packet);
                    liste.put(p, liste.get(p) - 1);
                    return;
                }

                if (liste.get(p) == 0) {
                    String hazir = plugin.getConfig().getString("hazirmesaj").replace("&", "§");
                    PacketPlayOutChat packet = new PacketPlayOutChat(IChatBaseComponent.ChatSerializer.a("{\"text\":\"" + hazir.replace("&", "§") + "\"}"), (byte) 2);
                    ((CraftPlayer) p).getHandle().playerConnection.sendPacket(packet);
                    liste.remove(p);
                    cancel();
                }

            }
        }.runTaskTimer(plugin, 0L, 20L);
    }


    public Integer cd(){
        return plugin.getConfig().getInt("cooldown");
    }

}
