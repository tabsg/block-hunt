package gold.tabs.blockhunt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class Blockhunt extends JavaPlugin {

  // private static final List<Material> blockChoices = Arrays.asList(Material.values());
  private static final Set<Material> blockChoices =
      new HashSet<>(Arrays.asList(Material.GRASS_BLOCK, Material.STONE, Material.DIRT));

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
    if (command.getName().equalsIgnoreCase("blockhunt")) {
      play();
      return true;
    }
    return false;
  }

  private void play() {
    // get players
    // generate blocks
    // tell players their blocks
    // start a timer
    // countdown last 5 seconds
    // at end of timer, check blocks
    // allocate points
    List<Player> players = new ArrayList<>(Bukkit.getOnlinePlayers());
    System.out.println(players);
  }
}
