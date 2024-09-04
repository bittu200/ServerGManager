/*
 *  GroupManager - A plug-in for Spigot/Bukkit based Minecraft servers.
 *  Copyright (C) 2020  ElgarL
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.anjocaido.groupmanager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.anjocaido.groupmanager.commands.ManCheckW;
import org.anjocaido.groupmanager.commands.ManClear;
import org.anjocaido.groupmanager.commands.ManDemote;
import org.anjocaido.groupmanager.commands.ManGAdd;
import org.anjocaido.groupmanager.commands.ManGAddI;
import org.anjocaido.groupmanager.commands.ManGAddP;
import org.anjocaido.groupmanager.commands.ManGAddV;
import org.anjocaido.groupmanager.commands.ManGCheckP;
import org.anjocaido.groupmanager.commands.ManGCheckV;
import org.anjocaido.groupmanager.commands.ManGClearP;
import org.anjocaido.groupmanager.commands.ManGDel;
import org.anjocaido.groupmanager.commands.ManGDelI;
import org.anjocaido.groupmanager.commands.ManGDelP;
import org.anjocaido.groupmanager.commands.ManGDelV;
import org.anjocaido.groupmanager.commands.ManGList;
import org.anjocaido.groupmanager.commands.ManGListP;
import org.anjocaido.groupmanager.commands.ManGListV;
import org.anjocaido.groupmanager.commands.ManImport;
import org.anjocaido.groupmanager.commands.ManLoad;
import org.anjocaido.groupmanager.commands.ManPromote;
import org.anjocaido.groupmanager.commands.ManSave;
import org.anjocaido.groupmanager.commands.ManSelect;
import org.anjocaido.groupmanager.commands.ManToggleSave;
import org.anjocaido.groupmanager.commands.ManToggleValidate;
import org.anjocaido.groupmanager.commands.ManUAdd;
import org.anjocaido.groupmanager.commands.ManUAddP;
import org.anjocaido.groupmanager.commands.ManUAddSub;
import org.anjocaido.groupmanager.commands.ManUAddTemp;
import org.anjocaido.groupmanager.commands.ManUAddV;
import org.anjocaido.groupmanager.commands.ManUCheckP;
import org.anjocaido.groupmanager.commands.ManUCheckV;
import org.anjocaido.groupmanager.commands.ManUClearP;
import org.anjocaido.groupmanager.commands.ManUDel;
import org.anjocaido.groupmanager.commands.ManUDelAllTemp;
import org.anjocaido.groupmanager.commands.ManUDelP;
import org.anjocaido.groupmanager.commands.ManUDelSub;
import org.anjocaido.groupmanager.commands.ManUDelTemp;
import org.anjocaido.groupmanager.commands.ManUDelV;
import org.anjocaido.groupmanager.commands.ManUListP;
import org.anjocaido.groupmanager.commands.ManUListTemp;
import org.anjocaido.groupmanager.commands.ManUListV;
import org.anjocaido.groupmanager.commands.ManWhois;
import org.anjocaido.groupmanager.commands.ManWorld;
import org.anjocaido.groupmanager.data.User;
import org.anjocaido.groupmanager.dataholder.OverloadedWorldHolder;
import org.anjocaido.groupmanager.dataholder.worlds.MirrorsMap;
import org.anjocaido.groupmanager.dataholder.worlds.WorldsHolder;
import org.anjocaido.groupmanager.dependencies.DependencyManager;
import org.anjocaido.groupmanager.events.GMWorldListener;
import org.anjocaido.groupmanager.events.GroupManagerEventHandler;
import org.anjocaido.groupmanager.localization.Messages;
import org.anjocaido.groupmanager.metrics.Metrics;
import org.anjocaido.groupmanager.permissions.BukkitPermissions;
import org.anjocaido.groupmanager.placeholder.GMPlaceholderExpansion;
import org.anjocaido.groupmanager.tasks.BukkitPermsUpdateTask;
import org.anjocaido.groupmanager.tasks.UpdateTask;
import org.anjocaido.groupmanager.utils.BukkitWrapper;
import org.anjocaido.groupmanager.utils.OfflinePlayerCache;
import org.anjocaido.groupmanager.utils.Tasks;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;



//new
package com.example.myplugin;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
/**
 *
 * @author gabrielcouto, ElgarL
 */
public class GroupManager extends JavaPlugin {
//new

    private Connection connection;
    private FileConfiguration messagesConfig;
    private FileConfiguration banPlayersConfig;
    private File banPlayersFile;

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
        this.getCommand("gm1").setExecutor(this);
        this.getCommand("gm2").setExecutor(this);
        this.getCommand("gm3").setExecutor(this);
        this.getCommand("fly").setExecutor(this);
        this.getCommand("tp").setExecutor(this);
        this.getCommand("staff").setExecutor(this);
        this.getCommand("banmanager").setExecutor(this);

        createMessagesFile();
        createBanPlayersFile();

        if (getConfig().getString("ban-storage-method").equalsIgnoreCase("mysql")) {
            connectDatabase();
        }
    }

    @Override
    public void onDisable() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void connectDatabase() {
        try {
            FileConfiguration config = getConfig();
            String host = config.getString("mysql.host");
            String port = config.getString("mysql.port");
            String database = config.getString("mysql.database");
            String user = config.getString("mysql.user");
            String password = config.getString("mysql.password");

            connection = DriverManager.getConnection(
                    "jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=false",
                    user,
                    password
            );

            Statement statement = connection.createStatement();
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS bans (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "player VARCHAR(36), " +
                    "reason TEXT, " +
                    "staff VARCHAR(36), " +
                    "date DATETIME)");

        } catch (SQLException e) {
            getLogger().severe(messagesConfig.getString("messages.mysql-connection-error"));
            e.printStackTrace();
        }
    }

    private void createMessagesFile() {
        File messagesFile = new File(getDataFolder(), "messages.yml");
        if (!messagesFile.exists()) {
            saveResource("messages.yml", false);
        }
        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
    }

    private void createBanPlayersFile() {
        banPlayersFile = new File(getDataFolder(), "banplayers.yml");
        if (!banPlayersFile.exists()) {
            try {
                banPlayersFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        banPlayersConfig = YamlConfiguration.loadConfiguration(banPlayersFile);
    }

    private void saveBannedPlayerToFile(String player, String reason, String staff, Timestamp date) {
        String path = "banned-players." + player;
        banPlayersConfig.set(path + ".reason", reason);
        banPlayersConfig.set(path + ".staff", staff);
        banPlayersConfig.set(path + ".date", date.toString());
        try {
            banPlayersConfig.save(banPlayersFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean isPlayerBannedInFile(String player) {
        return banPlayersConfig.contains("banned-players." + player);
    }

    private String getBanReasonFromFile(String player) {
        return banPlayersConfig.getString("banned-players." + player + ".reason");
    }

    private String getBanStaffFromFile(String player) {
        return banPlayersConfig.getString("banned-players." + player + ".staff");
    }

    private String getBanDateFromFile(String player) {
        return banPlayersConfig.getString("banned-players." + player + ".date");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String gameModeMessage = messagesConfig.getString("messages.gamemode-set");
        String playerNotFoundMessage = messagesConfig.getString("messages.player-not-found");
        String flyToggleMessage = messagesConfig.getString("messages.fly-toggle");
        String teleportSuccessMessage = messagesConfig.getString("messages.teleport-success");
        String invalidCoordinatesMessage = messagesConfig.getString("messages.invalid-coordinates");
        String staffModeToggleMessage = messagesConfig.getString("messages.staff-mode-toggle");

        if (command.getName().equalsIgnoreCase("gm1")) {
            return handleGameModeCommand(sender, GameMode.CREATIVE, args, gameModeMessage, playerNotFoundMessage);
        }
        if (command.getName().equalsIgnoreCase("gm2")) {
            return handleGameModeCommand(sender, GameMode.ADVENTURE, args, gameModeMessage, playerNotFoundMessage);
        }
        if (command.getName().equalsIgnoreCase("gm3")) {
            return handleGameModeCommand(sender, GameMode.SPECTATOR, args, gameModeMessage, playerNotFoundMessage);
        }
        if (command.getName().equalsIgnoreCase("fly")) {
            return handleFlyCommand(sender, args, flyToggleMessage, playerNotFoundMessage);
        }
        if (command.getName().equalsIgnoreCase("tp")) {
            return handleTeleportCommand(sender, args, teleportSuccessMessage, invalidCoordinatesMessage, playerNotFoundMessage);
        }
        if (command.getName().equalsIgnoreCase("staff")) {
            return handleStaffCommand(sender, staffModeToggleMessage);
        }
        if (command.getName().equalsIgnoreCase("banmanager")) {
            return handleBanManagerCommand(sender);
        }
        return false;
    }

    private boolean handleGameModeCommand(CommandSender sender, GameMode gameMode, String[] args, String successMessage, String playerNotFoundMessage) {
        if (args.length == 0 && sender instanceof Player) {
            Player player = (Player) sender;
            player.setGameMode(gameMode);
            player.sendMessage(ChatColor.GREEN + successMessage.replace("%mode%", gameMode.toString()));
        } else if (args.length == 1) {
            Player target = Bukkit.getPlayer(args[0]);
            if (target != null) {
                target.setGameMode(gameMode);
                target.sendMessage(ChatColor.GREEN + successMessage.replace("%mode%", gameMode.toString()));
            } else {
                sender.sendMessage(ChatColor.RED + playerNotFoundMessage);
            }
        }
        return true;
    }

    private boolean handleFlyCommand(CommandSender sender, String[] args, String successMessage, String playerNotFoundMessage) {
        if (args.length == 0 && sender instanceof Player) {
            Player player = (Player) sender;
            player.setAllowFlight(!player.getAllowFlight());
            player.sendMessage(ChatColor.GREEN + successMessage);
        } else if (args.length == 1) {
            Player target = Bukkit.getPlayer(args[0]);
            if (target != null) {
                target.setAllowFlight(!target.getAllowFlight());
                target.sendMessage(ChatColor.GREEN + successMessage);
            } else {
                sender.sendMessage(ChatColor.RED + playerNotFoundMessage);
            }
        }
        return true;
    }

    private boolean handleTeleportCommand(CommandSender sender, String[] args, String successMessage, String invalidCoordinatesMessage, String playerNotFoundMessage) {
        if (args.length == 1) {
            Player target = Bukkit.getPlayer(args[0]);
            if (target != null && sender instanceof Player) {
                Player player = (Player) sender;
                player.teleport(target.getLocation());
                player.sendMessage(ChatColor.GREEN + successMessage);
            } else {
                sender.sendMessage(ChatColor.RED + playerNotFoundMessage);
            }
        } else if (args.length == 3) {
            try {
                double x = Double.parseDouble(args[0]);
                double y = Double.parseDouble(args[1]);
                double z = Double.parseDouble(args[2]);
                Location loc = new Location(Bukkit.getWorlds().get(0), x, y, z);
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    player.teleport(loc);
                    player.sendMessage(ChatColor.GREEN + successMessage);
                }
            } catch (NumberFormatException e) {
                sender.sendMessage(ChatColor.RED + invalidCoordinatesMessage);
            }
        }
        return true;
    }

    private boolean handleStaffCommand(CommandSender sender, String staffModeToggleMessage) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            player.sendMessage(ChatColor.GREEN + staffModeToggleMessage);
    }
        return true;
    }

    private boolean handleBanManagerCommand(CommandSender sender) {
        if (!(sender instanceof Player)) return false;
        Player player = (Player) sender;
        Inventory banManagerGUI = Bukkit.createInventory(null, 54, messagesConfig.getString("messages.ban-manager-title"));

        if (getConfig().getString("ban-storage-method").equalsIgnoreCase("mysql")) {
            try {
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery("SELECT * FROM bans");
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

                while (resultSet.next()) {
                    ItemStack banItem = new ItemStack(Material.PAPER);
                    ItemMeta meta = banItem.getItemMeta();
                    meta.setDisplayName(ChatColor.RED + resultSet.getString("player"));
                    List<String> lore = new ArrayList<>();
                    lore.add(ChatColor.GRAY + messagesConfig.getString("messages.ban-manager-item-lore")
                            .replace("%reason%", resultSet.getString("reason"))
                            .replace("%staff%", resultSet.getString("staff"))
                            .replace("%date%", dateFormat.format(resultSet.getTimestamp("date"))));
                    meta.setLore(lore);
                    banItem.setItemMeta(meta);
                    banManagerGUI.addItem(banItem);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            Set<String> bannedPlayers = banPlayersConfig.getConfigurationSection("banned-players").getKeys(false);
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            for (String bannedPlayer : bannedPlayers) {
                ItemStack banItem = new ItemStack(Material.PAPER);
                ItemMeta meta = banItem.getItemMeta();
                meta.setDisplayName(ChatColor.RED + bannedPlayer);
                List<String> lore = new ArrayList<>();
                lore.add(ChatColor.GRAY + messagesConfig.getString("messages.ban-manager-item-lore")
                        .replace("%reason%", getBanReasonFromFile(bannedPlayer))
                        .replace("%staff%", getBanStaffFromFile(bannedPlayer))
                        .replace("%date%", dateFormat.format(Timestamp.valueOf(getBanDateFromFile(bannedPlayer)))));
                meta.setLore(lore);
                banItem.setItemMeta(meta);
                banManagerGUI.addItem(banItem);
            }
        }

        player.openInventory(banManagerGUI);
        return true;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        String storageMethod = getConfig().getString("ban-storage-method");

        if (storageMethod.equalsIgnoreCase("mysql")) {
            try {
                PreparedStatement statement = connection.prepareStatement("SELECT * FROM bans WHERE player=?");
                statement.setString(1, player.getName());
                ResultSet resultSet = statement.executeQuery();

                if (resultSet.next()) {
                    String reason = resultSet.getString("reason");
                    String staff = resultSet.getString("staff");
                    Timestamp date = resultSet.getTimestamp("date");
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

                    String banMessage = ChatColor.RED + messagesConfig.getString("messages.banned") + "\n" +
                                        ChatColor.GRAY + messagesConfig.getString("messages.ban-reason").replace("%reason%", reason) + "\n" +
                                        ChatColor.GRAY + messagesConfig.getString("messages.ban-staff").replace("%staff%", staff) + "\n" +
                                        ChatColor.GRAY + messagesConfig.getString("messages.ban-date").replace("%date%", dateFormat.format(date));

                    player.kickPlayer(banMessage);
                }
            } catch (SQLException e) {
                e.printStackTrace();
                player.kickPlayer(ChatColor.RED + messagesConfig.getString("messages.ban-check-error"));
            }
        } else if (storageMethod.equalsIgnoreCase("yml")) {
            if (isPlayerBannedInFile(player.getName())) {
                String reason = getBanReasonFromFile(player.getName());
                String staff = getBanStaffFromFile(player.getName());
                String date = getBanDateFromFile(player.getName());

                String banMessage = ChatColor.RED + messagesConfig.getString("messages.banned") + "\n" +
                                    ChatColor.GRAY + messagesConfig.getString("messages.ban-reason").replace("%reason%", reason) + "\n" +
                                    ChatColor.GRAY + messagesConfig.getString("messages.ban-staff").replace("%staff%", staff) + "\n" +
                                    ChatColor.GRAY + messagesConfig.getString("messages.ban-date").replace("%date%", date);

                player.kickPlayer(banMessage);
            }
        }
    }
										  }

	//old
	private File backupFolder;
	private ScheduledThreadPoolExecutor scheduler;
	private static Map<String, ArrayList<User>> overloadedUsers = new HashMap<>();
	private static Map<String, String> selectedWorlds = new HashMap<>();

	private static WorldsHolder worldsHolder;

	private static boolean isLoaded = false;
	private static GMConfiguration config;
	private final ReentrantLock saveLock = new ReentrantLock();

	private String lastError = ""; //$NON-NLS-1$

	private static GlobalGroups globalGroups;

	private static GroupManagerEventHandler GMEventHandler;
	@Deprecated // This field will be changing to private in the future. Please use the static getBukkitPermissions() method
	public static BukkitPermissions BukkitPermissions;
	private GMWorldListener WorldEvents;
	public static final Logger logger = Logger.getLogger("GroupManager");

	@Override
	public void onLoad() {

		// Check dependencies
		getLogger().info("Dependencies: " + (DependencyManager.checkDependencies(this) ? "OK" : "Warning unknown state!"));
	}

	@Override
	public void onDisable() {

		onDisable(false);
	}

	@Override
	public void onEnable() {

		// Instance our cache to populate it.
		getLogger().info("OfflinePlayers cached ( " + OfflinePlayerCache.getInstance().size() + " ).");

		/*
		 * Initialize the event handler
		 */
		setGMEventHandler(new GroupManagerEventHandler(this));
		onEnable(false);
	}

	public void onDisable(boolean restarting) {

		setLoaded(false);

		if (!restarting) {
			// Unregister this service if we are shutting down.
			this.getServer().getServicesManager().unregister(this);
			if (WorldEvents != null)
				WorldEvents = null;
			BukkitPermissions = null;
		}

		disableScheduler(); // Shutdown before we save, so it doesn't interfere.
		if (worldsHolder != null) {
			try {
				worldsHolder.saveChanges(false);
			} catch (IllegalStateException ex) {
				GroupManager.logger.log(Level.WARNING, ex.getMessage());
			}
		}

		// Remove all attachments before clearing
		if (BukkitPermissions != null) {
			BukkitPermissions.removeAllAttachments();
		}

		// log that we are disabled.
		PluginDescriptionFile pdfFile = this.getDescription();
		GroupManager.logger.log(Level.INFO, String.format(Messages.getString("GroupManager.DISABLED"), pdfFile.getVersion())); //$NON-NLS-1$
	}

	public void onEnable(boolean restarting) {

		try {
			PluginDescriptionFile pdfFile = this.getDescription();

			/*
			 * reset local variables.
			 */
			overloadedUsers = new HashMap<>();
			selectedWorlds = new HashMap<>();
			lastError = ""; //$NON-NLS-1$

			/*
			 * Load our config.yml
			 */
			prepareBackupFolder();
			config = new GMConfiguration(this);
			config.load();

			/*
			 * Configure the worlds holder.
			 */
			if (!restarting)
				worldsHolder = new MirrorsMap(this);

			/*
			 * Load the global groups before we load our worlds
			 */
			globalGroups = new GlobalGroups(this);
			globalGroups.load();
			setLoaded(false); // Reset as GG flags us loaded too early.

			/*
			 * Load our world data.
			 */
			worldsHolder.resetWorldsHolder();

			/*
			 *  Initialize the world listener and Bukkit permissions
			 *  to handle events and initialize our command handlers
			 */
			if (!restarting) {
				WorldEvents = new GMWorldListener(this);
				BukkitPermissions = new BukkitPermissions(this);

				checkPlugins();
				initCommands();

			} else {
				BukkitPermissions.reset();
			}

			/*
			 * Start the scheduler for data saving.
			 */
			enableScheduler();

			/*
			 * Schedule a Bukkit Permissions update for 1 tick later.
			 * All plugins will be loaded by then
			 */
			if (getServer().getScheduler().scheduleSyncDelayedTask(this, new BukkitPermsUpdateTask(), 1) == -1)
				GroupManager.logger.log(Level.SEVERE, Messages.getString("GroupManager.ERROR_SCHEDULING_SUPERPERMS")); //$NON-NLS-1$

			/*
			 * Register as a service and Metrics.
			 */
			if (!restarting) {
				this.getServer().getServicesManager().register(GroupManager.class, this, this, ServicePriority.Lowest);

				/*
				 * Register Metrics
				 */
				try {
					Metrics metrics = new Metrics(this, 7982);

					metrics.addCustomChart(new Metrics.SimplePie("language", () -> GroupManager.getGMConfig().getLanguage()));
					metrics.addCustomChart(new Metrics.SimplePie("database", () -> GroupManager.getGMConfig().getDatabaseType()));
				} catch (Exception e) {
					GroupManager.logger.log(Level.WARNING, "Failed to setup Metrics"); //$NON-NLS-1$
				}
			}

			/*
			 * Flag that we are now loaded and should start processing events.
			 */
			setLoaded(true);
			GroupManager.logger.log(Level.INFO, String.format("DataSource - %s", GroupManager.getGMConfig().getDatabaseType())); //$NON-NLS-1$
			GroupManager.logger.log(Level.INFO, String.format(Messages.getString("GroupManager.ENABLED"), pdfFile.getVersion())); //$NON-NLS-1$

			/*
			 * Version check.
			 */
			this.getServer().getScheduler().runTaskLaterAsynchronously(this, new UpdateTask(pdfFile.getVersion()), 1L);

		} catch (Exception ex) {

			/*
			 * Store the error and write to the log.
			 */
			saveErrorLog(ex);

			/*
			 * Throw an error so Bukkit knows about it.
			 */
			throw new IllegalArgumentException(ex.getMessage(), ex);
		}
	}

	/**
	 * Register all our commands.
	 */
	private void initCommands() {

		getCommand("mancheckw").setExecutor(new ManCheckW()); //$NON-NLS-1$
		getCommand("manclear").setExecutor(new ManClear()); //$NON-NLS-1$
		getCommand("mandemote").setExecutor(new ManDemote()); //$NON-NLS-1$
		getCommand("mangadd").setExecutor(new ManGAdd()); //$NON-NLS-1$
		getCommand("mangaddi").setExecutor(new ManGAddI()); //$NON-NLS-1$
		getCommand("mangaddp").setExecutor(new ManGAddP()); //$NON-NLS-1$
		getCommand("mangaddv").setExecutor(new ManGAddV()); //$NON-NLS-1$
		getCommand("mangcheckp").setExecutor(new ManGCheckP()); //$NON-NLS-1$
		getCommand("mangcheckv").setExecutor(new ManGCheckV()); //$NON-NLS-1$
		getCommand("mangclearp").setExecutor(new ManGClearP()); //$NON-NLS-1$
		getCommand("mangdel").setExecutor(new ManGDel()); //$NON-NLS-1$
		getCommand("mangdeli").setExecutor(new ManGDelI()); //$NON-NLS-1$
		getCommand("mangdelp").setExecutor(new ManGDelP()); //$NON-NLS-1$
		getCommand("mangdelv").setExecutor(new ManGDelV()); //$NON-NLS-1$
		getCommand("manglist").setExecutor(new ManGList()); //$NON-NLS-1$
		getCommand("manglistp").setExecutor(new ManGListP()); //$NON-NLS-1$
		getCommand("manglistv").setExecutor(new ManGListV()); //$NON-NLS-1$
		getCommand("manimport").setExecutor(new ManImport()); //$NON-NLS-1$
		getCommand("manload").setExecutor(new ManLoad()); //$NON-NLS-1$
		getCommand("manpromote").setExecutor(new ManPromote()); //$NON-NLS-1$
		getCommand("mansave").setExecutor(new ManSave()); //$NON-NLS-1$
		getCommand("manselect").setExecutor(new ManSelect()); //$NON-NLS-1$
		getCommand("mantogglesave").setExecutor(new ManToggleSave()); //$NON-NLS-1$
		getCommand("mantogglevalidate").setExecutor(new ManToggleValidate()); //$NON-NLS-1$
		getCommand("manuadd").setExecutor(new ManUAdd()); //$NON-NLS-1$
		getCommand("manuaddp").setExecutor(new ManUAddP()); //$NON-NLS-1$
		getCommand("manuaddsub").setExecutor(new ManUAddSub()); //$NON-NLS-1$
		getCommand("manuaddv").setExecutor(new ManUAddV()); //$NON-NLS-1$
		getCommand("manucheckp").setExecutor(new ManUCheckP()); //$NON-NLS-1$
		getCommand("manucheckv").setExecutor(new ManUCheckV()); //$NON-NLS-1$
		getCommand("manuclearp").setExecutor(new ManUClearP()); //$NON-NLS-1$
		getCommand("manudel").setExecutor(new ManUDel()); //$NON-NLS-1$
		getCommand("manudelsub").setExecutor(new ManUDelSub()); //$NON-NLS-1$
		getCommand("manudelp").setExecutor(new ManUDelP()); //$NON-NLS-1$
		getCommand("manudelv").setExecutor(new ManUDelV()); //$NON-NLS-1$
		getCommand("manulistp").setExecutor(new ManUListP()); //$NON-NLS-1$
		getCommand("manulistv").setExecutor(new ManUListV()); //$NON-NLS-1$
		getCommand("manwhois").setExecutor(new ManWhois()); //$NON-NLS-1$
		getCommand("manworld").setExecutor(new ManWorld()); //$NON-NLS-1$
		getCommand("manuaddtemp").setExecutor(new ManUAddTemp()); //$NON-NLS-1$
		getCommand("manudeltemp").setExecutor(new ManUDelTemp()); //$NON-NLS-1$
		getCommand("manudelalltemp").setExecutor(new ManUDelAllTemp()); //$NON-NLS-1$
		getCommand("manulisttemp").setExecutor(new ManUListTemp()); //$NON-NLS-1$

	}

	/**
	 * Initialize any plugins we may hook in to.
	 */
	private void checkPlugins() {

		List<String> addons = new ArrayList<>();
		Plugin addon = getServer().getPluginManager().getPlugin("PlaceholderAPI");
		if (addon != null) {
			new GMPlaceholderExpansion(this).register();
			addons.add(String.format("%s v%s", "PlaceholderAPI", addon.getDescription().getVersion()));
		}

		if (!addons.isEmpty())
			GroupManager.logger.log(Level.INFO, "Add-ons: " + String.join(", ", addons));
	}

	/**
	 * Write an error.log
	 * 
	 * @param ex	the Exception to save to the log.
	 */
	private void saveErrorLog(Exception ex) {

		if (!getDataFolder().exists()) {
			getDataFolder().mkdirs();
		}

		lastError = ex.getMessage();

		GroupManager.logger.log(Level.SEVERE, "===================================================="); //$NON-NLS-1$
		GroupManager.logger.log(Level.SEVERE, String.format("= ERROR REPORT START - %s =", this.getDescription().getVersion())); //$NON-NLS-1$
		GroupManager.logger.log(Level.SEVERE, "===================================================="); //$NON-NLS-1$
		GroupManager.logger.log(Level.SEVERE, "=== PLEASE COPY AND PASTE THE ERROR.LOG FROM THE ==="); //$NON-NLS-1$
		GroupManager.logger.log(Level.SEVERE, "= GROUPMANAGER FOLDER TO A GROUPMANAGER  DEVELOPER ="); //$NON-NLS-1$
		GroupManager.logger.log(Level.SEVERE, "===================================================="); //$NON-NLS-1$
		GroupManager.logger.log(Level.SEVERE, lastError);
		GroupManager.logger.log(Level.SEVERE, "===================================================="); //$NON-NLS-1$
		GroupManager.logger.log(Level.SEVERE, "= ERROR REPORT ENDED ="); //$NON-NLS-1$
		GroupManager.logger.log(Level.SEVERE, "===================================================="); //$NON-NLS-1$

		// Append this error to the error log.
		try {
			String error = "=============================== GM ERROR LOG ===============================\n"; //$NON-NLS-1$
			error += String.format("= ERROR REPORT START - %s =\n\n", this.getDescription().getVersion()); //$NON-NLS-1$

			error += Tasks.getStackTraceAsString(ex);
			error += "\n============================================================================\n"; //$NON-NLS-1$

			Tasks.appendStringToFile(error, (getDataFolder() + System.getProperty("file.separator") + "ERROR.LOG")); //$NON-NLS-1$ //$NON-NLS-2$
		} catch (IOException e) {
			// Failed to write file.
			e.printStackTrace();
		}

	}

	private void prepareBackupFolder() {

		backupFolder = new File(this.getDataFolder(), "backup"); //$NON-NLS-1$
		if (!backupFolder.exists()) {
			getBackupFolder().mkdirs();
		}
	}

	public void enableScheduler() {

		if (worldsHolder != null) {
			disableScheduler();
			/*
			 * Thread to handle saving data.
			 */
			Runnable committer = () -> {

				if (isLoaded())
					getWorldsHolder().refreshData(null);
			};
			/*
			 * Thread for purging expired permissions.
			 */
			Runnable cleanup = () -> {

				if (isLoaded()) {

					try {

						/*
						 * If we removed any permissions and saving is not disabled
						 * we update our data files so we are not updating perms
						 * every 60 seconds.
						 */
						getWorldsHolder().purgeExpiredPerms();

					} catch (Exception ex) {
						GroupManager.logger.log(Level.SEVERE, "Failed to purge expired permissions:\n" + Tasks.getStackTraceAsString(ex));
					}
				}
			};

			/*
			 * Thread to purge old users.
			 */
			Runnable maintenance = () -> {

				if (isLoaded()) {

					GroupManager.logger.log(Level.INFO, "Running maintenance purge of old users.. ");
					try {
						int count = 0;

						for (OverloadedWorldHolder world : getWorldsHolder().allWorldsDataList()) {

							if (!getWorldsHolder().hasUsersMirror(world.getName())) {

								for (Iterator<User> iterator = world.getUserList().iterator(); iterator.hasNext();) {
									User user = iterator.next();
									long lastPlayed = user.getVariables().getVarDouble("lastplayed").longValue();

									/*
									 * Users with a lastplayed variable set to 0 are protected from deletion.
									 */
									if (lastPlayed == 0) continue;
									
									String id = user.getUUID();
									long serverLastPlayed = (id.length() > 16) ? BukkitWrapper.getInstance().getLastOnline(UUID.fromString(id)) : BukkitWrapper.getInstance().getLastOnline(id);

									if (Tasks.isExpired(TimeUnit.MILLISECONDS.toSeconds(serverLastPlayed) + getGMConfig().userExpires())) {

										world.removeUser(user.getUUID());
										world.setUsersChanged(true);
										count++;
									}
									Thread.sleep(50);
								}
							}
						}

						if (count > 0)
							GroupManager.logger.log(Level.INFO, String.format("Removed %s expired users.", count)); //$NON-NLS-1$
					} catch (Exception ex) {
						GroupManager.logger.log(Level.WARNING, "Failed to purge old users: " + ex.getMessage());
					}
				}
			};

			scheduler = new ScheduledThreadPoolExecutor(3);
			long minutes = getGMConfig().getSaveInterval();

			if (minutes > 0) {
				scheduler.scheduleAtFixedRate(committer, minutes, minutes, TimeUnit.MINUTES);

				if (getGMConfig().isTimedEnabled())
					scheduler.scheduleAtFixedRate(cleanup, 0, 1, TimeUnit.MINUTES);

				if (getGMConfig().isPurgeEnabled())
					scheduler.schedule(maintenance, 30, TimeUnit.SECONDS);

				GroupManager.logger.log(Level.INFO, (String.format(Messages.getString("GroupManager.SCHEDULED_DATA_SAVING_SET"), minutes))); //$NON-NLS-1$
			} else
				GroupManager.logger.log(Level.WARNING, Messages.getString("GroupManager.SCHEDULED_DATA_SAVING_DISABLED")); //$NON-NLS-1$

			GroupManager.logger.log(Level.INFO, String.format(Messages.getString("GroupManager.BACKUPS_RETAINED_MSG"), getGMConfig().getBackupDuration())); //$NON-NLS-1$
		}
	}

	public void disableScheduler() {

		if (scheduler != null) {
			try {
				scheduler.setContinueExistingPeriodicTasksAfterShutdownPolicy(false);
				scheduler.setExecuteExistingDelayedTasksAfterShutdownPolicy(false);
				scheduler.shutdown();
			} catch (Exception ignored) {
			}
			scheduler = null;
			GroupManager.logger.log(Level.WARNING, Messages.getString("GroupManager.SCHEDULED_DATA_SAVING_DISABLED")); //$NON-NLS-1$
		}
	}

	/**
	 * Get the World data containers.
	 * 
	 * @return the WorldsHolder.
	 */
	public WorldsHolder getWorldsHolder() {

		return worldsHolder;
	}

	/**
	 * Get all Users in an overloaded state.
	 * 
	 * @return the overloadedUsers
	 */
	public static Map<String, ArrayList<User>> getOverloadedUsers() {

		return overloadedUsers;
	}

	/**
	 * Get all command senders who have a world selected for commands.
	 * 
	 * @return the selectedWorlds.
	 */
	public static Map<String, String> getSelectedWorlds() {

		return selectedWorlds;
	}

	/**
	 * Send a notification that a user's groups have been changed using permission nodes:
	 * 
	 * groupmanager.notify.self; groupmanager.notify.other
	 * 
	 * @param name
	 * @param msg
	 */
	public static void notify(String name, String msg) {

		Player player = Bukkit.getServer().getPlayerExact(name);

		for (Player test : Bukkit.getServer().getOnlinePlayers()) {
			if (!test.equals(player)) {
				if (test.hasPermission("groupmanager.notify.other")) { //$NON-NLS-1$
					/*
					 * Notify others (with the permission node
					 * 'groupmanager.notify.other') of a group change.
					 * 
					 * %s was moved to the group %s in world %s.
					 */
					test.sendMessage(ChatColor.YELLOW + name + Messages.getString("PLAYER_WAS") + msg); //$NON-NLS-1$
				}
			} else if ((player != null) && ((player.hasPermission("groupmanager.notify.self")) || (player.hasPermission("groupmanager.notify.other")))) { //$NON-NLS-1$ //$NON-NLS-2$
				/*
				 * Notify the player (if they have the permission node
				 * 'groupmanager.notify.self') of their group change.
				 * 
				 * You were moved to the group %s in world %s.
				 */
				player.sendMessage(ChatColor.YELLOW + Messages.getString("YOU_WERE") + msg); //$NON-NLS-1$
			}
		}
	}

	/**
	 * @return the config
	 */
	public static GMConfiguration getGMConfig() {

		return config;
	}

	/**
	 * @return the backupFolder
	 */
	public File getBackupFolder() {

		return backupFolder;
	}

	public static GlobalGroups getGlobalGroups() {

		return globalGroups;
	}

	/**
	 * Is the plugin fully loaded?
	 * 
	 * @return	true if we are loaded or false if reloading/loading.
	 */
	public static boolean isLoaded() {

		return isLoaded;
	}

	public static void setLoaded(boolean isLoaded) {

		GroupManager.isLoaded = isLoaded;
	}

	/**
	 * @return the saveLock
	 */
	public ReentrantLock getSaveLock() {

		return saveLock;
	}

	/**
	 * @return true if the scheduler is running.
	 */
	public boolean isSchedulerRunning() {

		return scheduler != null;
	}

	/**
	 * @return the lastError
	 */
	public String getLastError() {

		return lastError;
	}

	/**
	 * @param lastError the lastError to set
	 */
	public void setLastError(String lastError) {

		this.lastError = lastError;
	}

	/**
	 * @return the bukkitPermissions
	 */
	public static BukkitPermissions getBukkitPermissions() {

		return BukkitPermissions;
	}

	public static GroupManagerEventHandler getGMEventHandler() {

		return GMEventHandler;
	}

	private static void setGMEventHandler(GroupManagerEventHandler gMEventHandler) {

		GMEventHandler = gMEventHandler;
	}

	/**
	 * @deprecated	Use getGMConfig().isToggleValidate()
	 * @return the validateOnlinePlayer
	 */
	@Deprecated // Use getGMConfig().isToggleValidate()
	public boolean isValidateOnlinePlayer() {

		return getGMConfig().isToggleValidate();
	}

	/**
	 * @deprecated	Use getGMConfig().setToggleValidate(value)
	 * @param validateOnlinePlayer the validateOnlinePlayer to set
	 */
	@Deprecated // Use getGMConfig().setToggleValidate(value)
	public void setValidateOnlinePlayer(boolean validateOnlinePlayer) {

		getGMConfig().setToggleValidate(validateOnlinePlayer);
	}
}
