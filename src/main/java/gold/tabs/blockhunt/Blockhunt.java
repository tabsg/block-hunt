package gold.tabs.blockhunt;

import static gold.tabs.blockhunt.OverworldBlocks.*;

import org.apache.commons.lang.WordUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public final class Blockhunt extends JavaPlugin {

  private static List<Material> blockChoices;
  private boolean playing = false;
  private int roundNumber;
  private List<Player> players;
  private final BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
  private final Map<Player, Material> playerBlockMap = new HashMap<>();
  private final Map<Player, Integer> scores = new HashMap<>();
  private int countdownID;
  private final List<Material> overworldBlocks = OverworldBlocks.blockList;
  private int rounds;


  public Blockhunt() {
    blockChoices = Arrays.asList(Material.values());
    blockChoices = blockChoices.stream().filter(Material::isSolid).collect(Collectors.toList());
  }

  @Override
  public void onEnable() {}

  @Override
  public void onDisable() {}

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    if (command.getName().equalsIgnoreCase("blockhunt") && !playing && args.length == 1) {
      try {
        rounds = Integer.parseInt(args[0]);
      } catch (NumberFormatException e) {
        return false;
      }
      playGame();
      return true;
    }
    return false;
  }

  private void playGame() {
    playing = true;
    roundNumber = 1;
    players = new ArrayList<>(Bukkit.getOnlinePlayers());
    scores.clear();
    players.forEach(player -> scores.put(player, 0));
    playRound();
  }

  private void playRound() {
    generateBlocks();
    playerBlockMap.forEach(
        (player, block) -> {
          String blockName =
              WordUtils.capitalizeFully(block.toString().replace("_", " ").toLowerCase());
          player.sendTitle(
              blockName,
              "You have " + roundLength(roundNumber) + " seconds to stand on your block!",
              10,
              100,
              10);
          player.sendMessage("Search for " + blockName + "!");
        });
    awaitEndOfRound();
  }

  private void awaitEndOfRound() {
    scheduler.scheduleSyncDelayedTask(
        this, this::startCountdown, (roundLength(roundNumber) - 6) * 20L);
  }

  private void startCountdown() {
    countdownID =
        scheduler.scheduleSyncRepeatingTask(
            this,
            new Runnable() {
              int timeRemaining = 5;

              @Override
              public void run() {
                for (Player player : players) {
                  player.sendTitle(String.valueOf(timeRemaining), "", 5, 10, 5);
                }
                timeRemaining--;
                if (timeRemaining == 0) {
                  endRound();
                }
              }
            },
            0L,
            20L);
  }

  private void endRound() {
    scheduler.cancelTask(countdownID);
    checkBlocksBeneathPlayers();
    showLeaderboard();
    roundNumber++;

    if (roundNumber < rounds) {
      playRound();
    } else {
      gameOver();
    }
  }

  private void checkBlocksBeneathPlayers() {
    playerBlockMap.forEach(
        (player, block) -> {
          if (player.getLocation().getBlock().getRelative(BlockFace.DOWN).getType().equals(block)) {
            player.sendMessage("Round successful!, You won " + roundNumber + " levels");
            player.giveExpLevels(roundNumber);
            scores.put(player, scores.get(player) + roundNumber);
          } else {
            player.sendMessage("Round unsuccessful, Try harder next time :/");
          }
        });
  }

  private static int roundLength(int roundNumber) {
    return 5;
    // return 330 - (roundNumber * 30);
  }

  private void generateBlocks() {
    Random random = new Random();
    playerBlockMap.clear();
    for (Player player : players) {
      int index = random.nextInt(blockChoices.size());
      playerBlockMap.put(player, blockChoices.get(index));
    }
  }

  private List<Entry<Player, Integer>> getLeaderboard() {
    List<Entry<Player, Integer>> scoreList = new ArrayList<>(scores.entrySet());
    scoreList.sort(Entry.comparingByValue());
    return scoreList.stream().limit(5).collect(Collectors.toList());
  }

  private void showLeaderboard() {
    List<Entry<Player, Integer>> leaderboard = getLeaderboard();


    for (Player player : players) {
      player.sendMessage("\nLeaderboard:");
      for (int i = 0; i < leaderboard.size(); i++) {
        Entry<Player, Integer> entry = leaderboard.get(i);
        player.sendMessage("  " + i + ": " + entry.getKey().getDisplayName() + " [" + entry.getValue() + " points]");
      }
      player.sendMessage("");
    }
  }

  private void gameOver() {
    Player winner = getLeaderboard().get(0).getKey();

    for (Player player : players) {
      player.sendTitle("Game Over", winner.getDisplayName() + " wins!", 10, 100, 10);
    }

    playing = false;
  }

}
