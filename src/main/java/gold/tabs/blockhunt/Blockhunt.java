package gold.tabs.blockhunt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class Blockhunt extends JavaPlugin {

  // private static final List<Material> blockChoices = Arrays.asList(Material.values());
  private static final List<Material> blockChoices =
      new ArrayList<>(Arrays.asList(Material.GRASS_BLOCK, Material.STONE, Material.DIRT));

  private boolean playing = false;

  private BukkitScheduler scheduler = Bukkit.getServer().getScheduler();

  @Override
  public void onEnable() {
    System.out.println(blockChoices);
  }

  @Override
  public void onDisable() {
    // Plugin shutdown logic
  }

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    if (command.getName().equalsIgnoreCase("blockhunt") && !playing) {
      playGame();
      return true;
    }
    return false;
  }

  private void playGame() {
    playing = true;
    List<Player> players = new ArrayList<>(Bukkit.getOnlinePlayers());
    System.out.println(players);
    playRound(players, 1);
  }

  private void playRound(List<Player> players, int roundNumber) {
    Map<Player, Material> playerBlockMap = generateBlocks(players);
    playerBlockMap.forEach(
        (player, block) -> {
          player.sendTitle(
              block.name(),
              "You have " + roundLength(roundNumber) + " seconds to stand on your block!",
              10,
              100,
              10);
        });
    endTimer(playerBlockMap, roundNumber);
  }

  private int roundLength(int roundNumber) {
    if (roundNumber <= 5) {
      return 360 - (roundNumber * 60);
    } else {
      return 30;
    }
  }

  private void endTimer(Map<Player, Material> playerBlockMap, int roundNumber) {
    scheduler.scheduleSyncDelayedTask(
            this,
            new Runnable() {
              public void run() {
                endRound(playerBlockMap, roundNumber);
              }
            },
            roundLength(roundNumber) * 20L);
  }

  private void endRound(Map<Player, Material> playerBlockMap, int roundNumber) {
    playerBlockMap.forEach(
        (player, block) -> {
          if (player.getLocation().getBlock().getRelative(BlockFace.DOWN).getType().equals(block)) {
            player.sendMessage(
                "Round successful!, You won " + roundNumber + " levels");
            player.giveExpLevels(roundNumber);
          } else {
            player.sendMessage("Round unsuccessful, Try harder next time :/");
          }
        });
  }

  private Map<Player, Material> generateBlocks(List<Player> players) {
    Random random = new Random();
    Map<Player, Material> playerBlockMap = new HashMap<>();
    for (Player player : players) {
      int index = random.nextInt(blockChoices.size());
      playerBlockMap.put(player, blockChoices.get(index));
    }
    return playerBlockMap;
  }
}
