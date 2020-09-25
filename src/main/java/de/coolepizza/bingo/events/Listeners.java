package de.coolepizza.bingo.events;

import com.google.gson.internal.$Gson$Preconditions;
import de.coolepizza.bingo.Bingo;
import de.coolepizza.bingo.manager.BingoManager;
import de.coolepizza.bingo.manager.BingoSettings;
import de.coolepizza.bingo.team.Team;
import de.coolepizza.bingo.team.TeamManager;
import de.coolepizza.bingo.utils.ItemBuilder;
import de.coolepizza.bingo.utils.ScoreboardUtils;
import de.coolepizza.bingo.utils.TablistManager;
import org.bukkit.*;
import org.bukkit.craftbukkit.libs.org.apache.commons.lang3.builder.Diff;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

public class Listeners implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent e){
        ScoreboardUtils.setCurrentScoreboard(e.getPlayer() , "§lBINGO");
        e.getPlayer().sendMessage("§aDieser Server nutzt " + Bingo.getInstance().getDescription().getName() + " v" + Bingo.getInstance().getDescription().getVersion()  + " by CoolePizza!");
        if (Bingo.getBingoManager().bingoState == BingoManager.BingoState.SETTINGS && e.getPlayer().isOp()){
            e.getPlayer().getInventory().clear();
            e.getPlayer().getInventory().addItem(new ItemBuilder(Material.NETHER_STAR).setDisplayname("§9Spiel Einstellungen").build());
        }else if (Bingo.getBingoManager().bingoState == BingoManager.BingoState.TEAM_JOIN ){
            e.getPlayer().getInventory().clear();
            e.getPlayer().getInventory().addItem(new ItemBuilder(Material.WHITE_BED).setDisplayname("§9Teamauswahl").build());
        }
        World w =  Bukkit.getWorld("world");
        int spawnx = (int) w.getSpawnLocation().getX();
        int spawnz = (int) w.getSpawnLocation().getZ();

        int y = w.getHighestBlockYAt(spawnx , spawnz);
        e.getPlayer().teleport(new Location(w ,spawnx+5 , y+2 , spawnz+5));

        TablistManager.sendPackets();
        Bingo.getBingoManager().getTeamManager().initPlayer(e.getPlayer());
    }
    @EventHandler
    public void onQuit(PlayerQuitEvent e){
        if (Bingo.getBingoManager().getTeamManager().getTeamFromPlayer(e.getPlayer()) != Team.SPECTATOR){
            Bingo.getBingoManager().getTeamManager().setTeam(e.getPlayer() , Team.SPECTATOR);
        }
    }

    @EventHandler
    public void onDamage(EntityDamageEvent e){
        if (Bingo.getTimer().isPaused()){
            e.setCancelled(true);
        }
    }
    @EventHandler
    public void onDamage(InventoryClickEvent e){
        if (Bingo.getTimer().isPaused()){
            e.setCancelled(true);
        }
    }
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e){
        if (Bingo.getTimer().isPaused()){
            e.setCancelled(true);
        }else {
            e.setCancelled(false);
        }
    }
    @EventHandler
    public void onBlockBreak(BlockBreakEvent e){
        if (Bingo.getTimer().isPaused()){
            e.setCancelled(true);
        }else {
            e.setCancelled(false);
        }
    }
    @EventHandler
    public void onInteract(PlayerInteractEvent e){
        if (e.getItem() == null){
            return;
        }
        if (e.getItem().getItemMeta().getDisplayName().equalsIgnoreCase("§9Spiel Einstellungen") && Bingo.getBingoManager().bingoState == BingoManager.BingoState.SETTINGS){
            Bingo.getBingoManager().getBingosettings().openSettingsInventory(e.getPlayer());
        }else if (e.getItem().getItemMeta().getDisplayName().equalsIgnoreCase("§9Teamauswahl") && Bingo.getBingoManager().bingoState == BingoManager.BingoState.TEAM_JOIN){
            Bingo.getBingoManager().getTeamManager().openTeamGUI(e.getPlayer());
        }
    }
    @EventHandler
    public void onBlockBreak(PlayerDropItemEvent e) {
        if (Bingo.getTimer().isPaused()) {
            e.setCancelled(true);
        }
    }
    @EventHandler
    public void onFoodChange(FoodLevelChangeEvent e){
        e.setCancelled(true);
    }
    @EventHandler
    public void onBlockBreak(InventoryClickEvent e) {
        if (e.getView().getTitle() == "§9Bingo §7>> §9Einstellungen"){
            e.setCancelled(true);
            ItemStack itemStack = e.getCurrentItem();

            Player player = (Player) e.getWhoClicked();

            if (itemStack != null){
                String local = itemStack.getItemMeta().getLocalizedName();
                if (local.equalsIgnoreCase("max")){
                    player.playSound(player.getLocation() , Sound.BLOCK_ANVIL_USE , 1 ,1);
                }

                if (itemStack.getType() == Material.LIME_DYE){
                    Bingo.getBingoManager().startTeamState();
                    return;
                }

                if (local.equalsIgnoreCase("maxplayers+")){
                    Bingo.getBingoManager().getBingosettings().addMaxPlayers(player);
                }else if (local.equalsIgnoreCase("maxplayers-")){
                    Bingo.getBingoManager().getBingosettings().removeMaxPlayer(player);
                }else if (local.equalsIgnoreCase("items-")){
                    Bingo.getBingoManager().getBingosettings().removeItems(player);
                }else if (local.equalsIgnoreCase("items+")){
                    Bingo.getBingoManager().getBingosettings().addItems(player);
                }else if (local.startsWith("dif_")){
                    BingoSettings.BingoDifficulty difficulty = null;
                    String dif = local.replace("dif_" , "");
                    if (dif.equalsIgnoreCase("NORMAL")){
                        difficulty = BingoSettings.BingoDifficulty.NORMAl;
                    }else if (dif.equalsIgnoreCase("HARD")){
                        difficulty = BingoSettings.BingoDifficulty.HARD;
                    }else if (dif.equalsIgnoreCase("EASY")){
                        difficulty = BingoSettings.BingoDifficulty.EASY;
                    }
                    Bingo.getBingoManager().getBingosettings().setDifficulty(player , difficulty);
                }
            }
        }else if (e.getView().getTitle().equalsIgnoreCase("§9Teamauswahl")){
            if (e.getCurrentItem() != null){
                Team team = Team.valueOf(e.getCurrentItem().getItemMeta().getLocalizedName());
                Bingo.getBingoManager().getTeamManager().setTeam((Player) e.getWhoClicked(), team);
                e.getWhoClicked().sendMessage("§aDu bist jetz in Team " + team.getTeamid() +  " !");
            }
        }
    }

}
