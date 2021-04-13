package gold.tabs.blockhunt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import org.apache.commons.lang.WordUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

public final class Blockhunt extends JavaPlugin {

  private static List<Material> blockChoices;
  private boolean playing = false;
  private int roundNumber;
  private List<Player> players;
  private final BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
  private Map<Player, Material> playerBlockMap;

  public Blockhunt() {
    blockChoices = Arrays.asList(Material.values());
    blockChoices = blockChoices.stream().filter(Material::isSolid).collect(Collectors.toList());
  }

  @Override
  public void onEnable() {}

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
    roundNumber = 1;
    players = new ArrayList<>(Bukkit.getOnlinePlayers());
    playRound();
  }

  private void playRound() {
    playerBlockMap = generateBlocks();
    playerBlockMap.forEach(
        (player, block) -> {
          player.sendTitle(
              WordUtils.capitalizeFully(block.toString().replace("_", " ").toLowerCase()),
              "You have " + roundLength(roundNumber) + " seconds to stand on your block!",
              10,
              100,
              10);
        });
    awaitEndOfRound();
  }

  private static int roundLength(int roundNumber) {
    return 330 - (roundNumber * 30);
  }

  private void awaitEndOfRound() {
    scheduler.scheduleSyncDelayedTask(
        this,
        new Runnable() {
          public void run() {
            endRound();
          }
        },
        roundLength(roundNumber) * 20L);
  }

  private void endRound() {
    playerBlockMap.forEach(
        (player, block) -> {
          if (player.getLocation().getBlock().getRelative(BlockFace.DOWN).getType().equals(block)) {
            player.sendMessage("Round successful!, You won " + roundNumber + " levels");
            player.giveExpLevels(roundNumber);
          } else {
            player.sendMessage("Round unsuccessful, Try harder next time :/");
          }
        });

    roundNumber++;

    if (roundNumber < 10) {
      playRound();
    } else {
      playing = false;
    }
  }

  private Map<Player, Material> generateBlocks() {
    Random random = new Random();
    playerBlockMap = new HashMap<>();
    for (Player player : players) {
      int index = random.nextInt(blockChoices.size());
      playerBlockMap.put(player, blockChoices.get(index));
    }
    return playerBlockMap;
  }
}
