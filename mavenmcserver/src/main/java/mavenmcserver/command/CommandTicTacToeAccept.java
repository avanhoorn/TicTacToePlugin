package mavenmcserver.command;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import mavenmcserver.Plugin;
import mavenmcserver.game.Game;
import net.md_5.bungee.api.ChatColor;

public class CommandTicTacToeAccept implements CommandExecutor, TabCompleter {
	
	public static String COMMAND_NAME = "tictactoeaccept";
	
	public CommandTicTacToeAccept(Plugin plugin) {
		plugin.getCommand(CommandTicTacToeAccept.COMMAND_NAME).setExecutor(this);
		plugin.getCommand(CommandTicTacToeAccept.COMMAND_NAME).setTabCompleter(this);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		
		if(!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.RED + "this command may only be executed by players" + ChatColor.RESET);
			return true;
		}
		
		if(args.length != 1) return false;
		
		String uuidString = args[0];
		UUID uuid = UUID.fromString(uuidString);
		
		if(uuid == null) {
			sender.sendMessage(ChatColor.RED + "Please enter a valid UUID ('" + uuidString + "' is not valid)!");
		}
		
		Game targetGame = Game.queuedGames.get(uuid);
		
		if(targetGame == null) {
			sender.sendMessage(ChatColor.RED + "No such game ('" + uuidString + "')!" + ChatColor.RESET);
			return true;
		}
		
		targetGame.start();
		
		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
		
		if(args.length > 1) return new ArrayList<String>();
		
		ArrayList<String> completions = new ArrayList<String>();
		for(UUID gameUUID: Game.queuedGames.keySet()) {
			completions.add(gameUUID.toString());
		}
		
		ArrayList<String> filteredCompletions = new ArrayList<String>();
		StringUtil.copyPartialMatches(args[args.length - 1], completions, filteredCompletions);
		
		return filteredCompletions;
	}
	
	
}
