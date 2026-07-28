package kez.politempire.kezStreamer;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class StreamManager {
    private final KezStreamer plugin;
    private BukkitTask adTask;
    private BukkitTask offlineCheckTask;
    private BukkitTask offlineTimerTask;
    private String activeStreamer;
    private String activeLink;
    private final Map<String, Long> cooldowns = new HashMap<>();

    public StreamManager(KezStreamer plugin) {
        this.plugin = plugin;
    }

    public boolean isOnCooldown(String playerName) {
        if (!cooldowns.containsKey(playerName)) {
            return false;
        }
        long cooldownSeconds = plugin.getConfig().getLong("settings.stream-cooldown-seconds", 60);
        long timeLeft = (cooldowns.get(playerName) + (cooldownSeconds * 1000)) - System.currentTimeMillis();
        return timeLeft > 0;
    }

    public long getCooldownTimeLeft(String playerName) {
        long cooldownSeconds = plugin.getConfig().getLong("settings.stream-cooldown-seconds", 60);
        long timeLeft = (cooldowns.get(playerName) + (cooldownSeconds * 1000)) - System.currentTimeMillis();
        return timeLeft / 1000;
    }

    public String getActiveStreamer() {
        return activeStreamer;
    }

    public String getActiveLink() {
        return activeLink;
    }

    public void startStreamAnnounce(String playerName, String link) {
        activeStreamer = playerName;
        activeLink = link;
        cooldowns.put(playerName, System.currentTimeMillis());

        String startMsg = plugin.getConfig().getString("messages.stream-start",
                "&6&lИгрок &e{player} &6&lначал стрим! &7(Группа: {group})");
        startMsg = ChatColor.translateAlternateColorCodes('&', startMsg);
        startMsg = startMsg.replace("{player}", playerName);
        startMsg = startMsg.replace("{group}", getPlayerGroup(playerName));
        Bukkit.broadcastMessage(startMsg);

        startAdLoop();
        startOfflineCheck(playerName);
    }

    public void stopStream(String playerName) {
        if (activeStreamer != null && activeStreamer.equals(playerName)) {
            String stopMsg = plugin.getConfig().getString("messages.stream-stopped-manual",
                    "&6&lСтрим игрока &e{player} &6&lзавершен");
            stopMsg = ChatColor.translateAlternateColorCodes('&', stopMsg);
            stopMsg = stopMsg.replace("{player}", playerName);
            stopMsg = stopMsg.replace("{group}", getPlayerGroup(playerName));
            Bukkit.broadcastMessage(stopMsg);

            activeStreamer = null;
            activeLink = null;
            stopAllTasks();
        }
    }

    private void sendClickableMessage(String message, String link) {
        message = message.replace("{group}", getPlayerGroup(activeStreamer));
        String[] parts = message.split("\\[link\\]");

        if (parts.length == 1 && !message.contains("[link]")) {
            Bukkit.broadcastMessage(message);
            return;
        }

        String linkText = plugin.getConfig().getString("messages.link-text", "&6&l[СМОТРЕТЬ]");
        linkText = ChatColor.translateAlternateColorCodes('&', linkText);

        for (Player player : Bukkit.getOnlinePlayers()) {
            TextComponent finalMessage = Component.text("");

            for (int i = 0; i < parts.length; i++) {
                Component textPart = Component.text(ChatColor.translateAlternateColorCodes('&', parts[i]));
                finalMessage = finalMessage.append(textPart);

                if (i < parts.length - 1 || message.endsWith("[link]")) {
                    Component linkComponent = Component.text(linkText)
                            .clickEvent(ClickEvent.openUrl(link))
                            .hoverEvent(HoverEvent.showText(Component.text("§eНажмите, чтобы открыть ссылку")));
                    finalMessage = finalMessage.append(linkComponent);
                }
            }

            player.sendMessage(finalMessage);
        }
    }

    private void startAdLoop() {
        stopAdTask();
        int intervalMinutes = plugin.getConfig().getInt("settings.ad-interval-minutes", 10);
        long intervalTicks = intervalMinutes * 60 * 20L;

        adTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (activeStreamer != null && activeLink != null) {
                String adMsg = plugin.getConfig().getString("messages.stream-ad",
                        "&eСейчас стримит {player} [link]");
                adMsg = ChatColor.translateAlternateColorCodes('&', adMsg);
                adMsg = adMsg.replace("{player}", activeStreamer);
                sendClickableMessage(adMsg, activeLink);
            }
        }, intervalTicks, intervalTicks);
    }

    private void startOfflineCheck(String playerName) {
        stopOfflineCheckTask();
        offlineCheckTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            Player player = Bukkit.getPlayer(playerName);
            if (player == null || !player.isOnline()) {
                stopOfflineCheckTask();
                startOfflineTimer(playerName);
            }
        }, 20L, 20L);
    }

    private void startOfflineTimer(String playerName) {
        stopOfflineTimerTask();
        int offlineMinutes = plugin.getConfig().getInt("settings.offline-timer-minutes", 5);
        long offlineTicks = offlineMinutes * 60 * 20L;

        offlineTimerTask = Bukkit.getScheduler().runTaskLater(plugin, () -> {
            Player player = Bukkit.getPlayer(playerName);
            if (player == null || !player.isOnline()) {
                String stopMsg = plugin.getConfig().getString("messages.stream-stopped",
                        "&6&lСтрим игрока &e{player} &6&lавтоматически завершен из-за отсутствия");
                stopMsg = ChatColor.translateAlternateColorCodes('&', stopMsg);
                stopMsg = stopMsg.replace("{player}", playerName);
                stopMsg = stopMsg.replace("{group}", getPlayerGroup(playerName));
                Bukkit.broadcastMessage(stopMsg);

                activeStreamer = null;
                activeLink = null;
                stopAllTasks();
            } else {
                startOfflineCheck(playerName);
            }
        }, offlineTicks);
    }

    public void stopAdTask() {
        if (adTask != null) {
            adTask.cancel();
            adTask = null;
        }
    }

    public void stopOfflineCheckTask() {
        if (offlineCheckTask != null) {
            offlineCheckTask.cancel();
            offlineCheckTask = null;
        }
    }

    public void stopOfflineTimerTask() {
        if (offlineTimerTask != null) {
            offlineTimerTask.cancel();
            offlineTimerTask = null;
        }
    }

    public void stopAllTasks() {
        stopAdTask();
        stopOfflineCheckTask();
        stopOfflineTimerTask();
    }

    public String getLinkForPlayer(String playerName) {
        File listFile = new File(plugin.getDataFolder(), "list.yml");
        YamlConfiguration listConfig = YamlConfiguration.loadConfiguration(listFile);
        return listConfig.getString("streamers." + playerName);
    }

    public void saveLinkForPlayer(String playerName, String link) {
        File listFile = new File(plugin.getDataFolder(), "list.yml");
        YamlConfiguration listConfig = YamlConfiguration.loadConfiguration(listFile);
        listConfig.set("streamers." + playerName, link);
        try {
            listConfig.save(listFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getPlayerGroup(String playerName) {
        if (playerName == null) {
            return "MEDIA";
        }
        Player targetPlayer = Bukkit.getPlayer(playerName);
        if (targetPlayer == null) {
            return "MEDIA";
        }
        try {
            net.luckperms.api.LuckPerms luckPerms = net.luckperms.api.LuckPermsProvider.get();
            net.luckperms.api.model.user.User user = luckPerms.getUserManager().getUser(targetPlayer.getUniqueId());
            if (user == null) {
                return "MEDIA";
            }
            String groupName = user.getPrimaryGroup();
            return groupName.toUpperCase();
        } catch (Exception e) {
            return "MEDIA";
        }
    }
}