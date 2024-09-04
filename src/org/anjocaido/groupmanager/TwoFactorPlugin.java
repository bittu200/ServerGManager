package org.anjocaido.groupmanager;

import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.anjocaido.groupmanager.GroupManager;
import java.util.HashMap;
import java.util.UUID;

public class TwoFactorPlugin extends JavaPlugin implements Listener {

    private HashMap<UUID, String> playerSecrets = new HashMap<>();
    private HashMap<UUID, Boolean> authenticatedPlayers = new HashMap<>();
    private GoogleAuthenticator gAuth = new GoogleAuthenticator();

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);

        if (getConfig().contains("players")) {
            for (String key : getConfig().getConfigurationSection("players").getKeys(false)) {
                UUID uuid = UUID.fromString(key);
                playerSecrets.put(uuid, getConfig().getString("players." + key + ".secret"));
                authenticatedPlayers.put(uuid, getConfig().getBoolean("players." + key + ".authenticated", false));
            }
        }

        getLogger().info("TwoFactorPlugin has been enabled!");
    }

    @Override
    public void onDisable() {
        for (UUID uuid : playerSecrets.keySet()) {
            getConfig().set("players." + uuid.toString() + ".secret", playerSecrets.get(uuid));
            getConfig().set("players." + uuid.toString() + ".authenticated", authenticatedPlayers.getOrDefault(uuid, false));
        }
        saveConfig();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();

        if (player.hasPermission("twofactor.verify")) {
            if (!playerSecrets.containsKey(playerUUID)) {
                // First time join with permission
                GoogleAuthenticatorKey key = gAuth.createCredentials();
                playerSecrets.put(playerUUID, key.getKey());
                player.kickPlayer(ChatColor.RED + "You must set up 2FA with the following key: " + ChatColor.YELLOW + key.getKey());
            } else {
                if (authenticatedPlayers.getOrDefault(playerUUID, false)) {
                    player.sendMessage(ChatColor.GREEN + "You have already authenticated this session.");
                } else {
                    // Ask the player to verify 2FA
                    player.sendMessage(ChatColor.GREEN + "Please verify your 2FA code using /2fa <code>.");
                }
            }
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (label.equalsIgnoreCase("2fa") && sender instanceof Player) {
            Player player = (Player) sender;
            UUID playerUUID = player.getUniqueId();

            if (!player.hasPermission("twofactor.verify")) {
                player.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
                return true;
            }

            if (args.length != 1) {
                player.sendMessage(ChatColor.RED + "Usage: /2fa <code>");
                return true;
            }

            String codeStr = args[0];
            int code;

            try {
                code = Integer.parseInt(codeStr);
            } catch (NumberFormatException e) {
                player.sendMessage(ChatColor.RED + "Invalid code format. Please enter a 6-digit numeric code.");
                return true;
            }

            String secret = playerSecrets.get(playerUUID);

            if (secret == null) {
                player.sendMessage(ChatColor.RED + "You do not have a 2FA secret set up. Please rejoin to receive a new key.");
                return true;
            }

            if (gAuth.authorize(secret, code)) {
                authenticatedPlayers.put(playerUUID, true);
                player.sendMessage(ChatColor.GREEN + "2FA verification successful! You are now authenticated.");
            } else {
                player.sendMessage(ChatColor.RED + "Invalid 2FA code. Please try again.");
            }

            return true;
        }

        return false;
    }
                                  }
