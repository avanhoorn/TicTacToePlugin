package mavenmcserver.game;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;

import mavenmcserver.Plugin;
import mavenmcserver.game.GameState.FieldState;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.hover.content.Text;

public class Game {
	
		/// Contains all  queued games that still have to be accepted / rejected
		public static HashMap<UUID, Game> queuedGames = new HashMap<UUID, Game>();
		
		/// Contains all games that are currently running in connection to their players (every game is in this map twice!)
		public static HashMap<Player, Game> runningGames = new HashMap<Player, Game>();

		public UUID uuid = UUID.randomUUID();
		public GameConfig config;
		public GameListener listener;
		public Location location;
		public GameState state;
		boolean opponentPlayersTurn = true;
		public CubicBlockArea gameArea; // the area to protect
		public Plugin plugin;
		
		private HashMap<Location, BlockData> beforeGameBlock = new HashMap<Location, BlockData>();
		
		public Game(GameConfig config, Plugin plugin) {
			Game.queuedGames.put(this.uuid, this);
			
			this.config = config;
			this.location = this.generateGameLocation();
			
			this.plugin = plugin;
			
			this.listener = new GameListener(this);
			this.state = new GameState(this.config.size);
			
			Location startBlock = new Location(this.location.getWorld(), this.location.getBlockX() - 2, this.location.getBlockY() - 1, this.location.getBlockZ() - 2);
			Location endBlock = new Location(this.location.getWorld(), this.location.getBlockX() + this.config.size.x * 2, this.location.getBlockY() + this.config.size.y * 2, this.location.getBlockZ() + this.config.size.z * 2);
			this.gameArea = new CubicBlockArea(startBlock, endBlock);
			
			
			this.inviteOpponent();
		}
		
		private Location generateGameLocation() {
			// double type to get rid of casting in the switch statement!
			double gameWidthInBlocks = (double)this.config.size.x * 2 - 1;
			double gameDepthInBlocks = (double)this.config.size.z * 2 - 1;
			
			double offsetX = 0, offsetZ = 0;
			
			switch(this.config.mainPlayer.getFacing()) {
			case NORTH: // towards negative Z
				offsetX = -Math.floor(gameWidthInBlocks / 2);
				offsetZ = -gameDepthInBlocks - 2;
				break;
			case EAST: // towards positive X
				offsetX = 2;
				offsetZ = -Math.floor(gameDepthInBlocks / 2);
				break;
			case SOUTH: // towards positive Z
				offsetX = -Math.floor(gameWidthInBlocks / 2);
				offsetZ = 2;
				break;
			case WEST: // towards negative X
				offsetX = -gameWidthInBlocks - 2;
				offsetZ = -Math.floor(gameDepthInBlocks / 2);
				break;
			default:
					break;
			}
			
			
			Location playerLocation = this.config.mainPlayer.getLocation();
			return new Location(playerLocation.getWorld(), playerLocation.getBlockX() + offsetX, playerLocation.getBlockY(), playerLocation.getBlockZ() + offsetZ);
		}
		
		private void inviteOpponent() {
			this.config.opponentPlayer.sendMessage("Hello " + ChatColor.AQUA + ChatColor.BOLD + this.config.opponentPlayer.getName() + ChatColor.RESET + "! " + ChatColor.AQUA + ChatColor.BOLD + this.config.mainPlayer.getName() + ChatColor.RESET + " would like to play a game of tic-tac-toe with you!");
			BaseComponent[] invitationComponent = new ComponentBuilder("Click ")
					.append("here").color(ChatColor.GREEN).bold(true).event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tictactoeaccept " + this.uuid.toString())).event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Click to accept")))
					.append(" to accept the game!").reset().create();
			this.config.opponentPlayer.spigot().sendMessage(invitationComponent);
		}
		
		public void start() {
			this.listener.activate();
			
			// Store old blocks
			this.beforeGameBlock.clear();
			this.gameArea.forEach((block) -> this.beforeGameBlock.put(block.getLocation(), block.getBlockData()));
			
			// Fill area with air (except for bottom layer)
			this.gameArea.forEach((block) -> {
				if(block.getLocation().getBlockY() != this.gameArea.startBlock.getBlockY()) {
					block.setType(Material.AIR);
				}
			});
			
			// Base plate
			for(int x = 0; x < this.config.size.x * 2 - 1; x++) {
				for(int z = 0; z < this.config.size.z * 2 - 1; z++) {
					this.location.getWorld().getBlockAt(this.location.getBlockX() + x, this.location.getBlockY(), this.location.getBlockZ() + z).setType(Material.BLACK_CONCRETE);
				}
			}
			
			// Fields
			for(int x = 0; x < this.config.size.x; x++) {
				for(int y = 0; y < this.config.size.y; y++) {
					for(int z = 0; z < this.config.size.z; z++) {
						this.location.getWorld().getBlockAt(this.location.getBlockX() + x * 2, this.location.getBlockY() + 1 + y * 2, this.location.getBlockZ() + z * 2).setType(Material.WHITE_CONCRETE);
					}
				}
			}
			
			this.config.mainPlayer.sendMessage(ChatColor.AQUA + "" + this.config.opponentPlayer.getName() + ChatColor.RESET + " has accepted your game!");
			
			this.registerStarted();
		}
		
		private void registerStarted() {
			Game.queuedGames.remove(this.uuid);
			Game.runningGames.put(this.config.mainPlayer, this);
			Game.runningGames.put(this.config.opponentPlayer, this);
			
			// Tells players who have requested a game with either mainPlayer or
			// opponentPlayer that they are not available anymore
			for (Entry<UUID, Game> queuedGameEntry : Game.queuedGames.entrySet()) {
				Game queuedGame = queuedGameEntry.getValue();
				if (queuedGame.config.opponentPlayer == this.config.opponentPlayer) {
					queuedGame.config.mainPlayer.sendMessage(ChatColor.AQUA + "" + this.config.opponentPlayer.getName() + ChatColor.RESET + " has just accepted another game.");
				} else if (queuedGame.config.opponentPlayer == this.config.mainPlayer) {
					queuedGame.config.mainPlayer.sendMessage(ChatColor.AQUA + "" + this.config.mainPlayer.getName() + ChatColor.RESET + " has just started their own game of tic-tac-toe.");
				}
			}

			// Remove redundant games:
			Game.queuedGames.entrySet().removeIf(e -> (e.getValue().config.opponentPlayer == this.config.opponentPlayer || e.getValue().config.opponentPlayer == this.config.mainPlayer));
		}
		
		public enum GameEndCause {
			MAIN_WIN,
			OPPONENT_WIN,
			TIE,
			CANCEL
		}
		
		public void end(GameEndCause cause) {
			this.listener.deactivate();
			
			
			this.gameArea.forEach((block) -> block.setBlockData(this.beforeGameBlock.get(block.getLocation())));
			
			
			switch(cause) {
			case CANCEL:
				String message = "Your current game of tic-tac-toe was " + ChatColor.YELLOW + ChatColor.BOLD + "cancelled" + ChatColor.RESET + "!";
				this.config.mainPlayer.sendMessage(message);
				this.config.opponentPlayer.sendMessage(message);
				break;
			case MAIN_WIN:
				break;
			case OPPONENT_WIN:
				break;
			case TIE:
				break;
			}
			
			
			this.registerEnded();
		}
		
		private void registerEnded() {
			Game.runningGames.remove(this.config.mainPlayer);
			Game.runningGames.remove(this.config.opponentPlayer);
		}
		
		/**
		 * The current player in turn marks the field at *position*.
		 * @param position
		 */
		public void placeAt(FieldPoint position) {
			
			if(this.state.getStateAt(position) != FieldState.NEUTRAL) return;
			
			this.state.setStateAt(position, this.opponentPlayersTurn ? FieldState.OPPONENT : FieldState.MAIN);
			
			
			Location inWorldLocation = this.state.fieldPointToBlockLocation(this.location, position);
			
			this.location.getWorld().getBlockAt(inWorldLocation).setType(this.opponentPlayersTurn ? Material.LIGHT_BLUE_CONCRETE : Material.RED_CONCRETE);
			
			
			this.opponentPlayersTurn = !this.opponentPlayersTurn;
		}
		
		
		public Player getPlayerInTurn() {
			return this.opponentPlayersTurn ? this.config.opponentPlayer : this.config.mainPlayer;
		}
	
}
