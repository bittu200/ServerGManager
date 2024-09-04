package org.anjocaido.groupmanager;

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

public class Main extends JavaPlugin implements Listener {

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
