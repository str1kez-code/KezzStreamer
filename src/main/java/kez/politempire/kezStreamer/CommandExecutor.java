package kez.politempire.kezStreamer;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandExecutor implements org.bukkit.command.CommandExecutor {
    private final KezStreamer plugin;
    private final StreamManager streamManager;

    public CommandExecutor(KezStreamer plugin, StreamManager streamManager) {
        this.plugin = plugin;
        this.streamManager = streamManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Эта команда только для игроков!");
            return true;
        }

        switch (command.getName().toLowerCase()) {
            case "startstream":
                return handleStartStream(player);
            case "stopstream":
                return handleStopStream(player);
            case "setstreamlink":
                return handleSetStreamLink(player, args);
            case "media":
                return handleMedia(player);
            default:
                return false;
        }
    }

    private boolean handleStartStream(Player player) {
        if (!player.hasPermission("kezstreamer.stream")) {
            player.sendMessage(ChatColor.RED + "У вас нет прав на эту команду.");
            return true;
        }

        if (streamManager.isOnCooldown(player.getName())) {
            long timeLeft = streamManager.getCooldownTimeLeft(player.getName());
            String cooldownMsg = plugin.getConfig().getString("messages.cooldown", "&cПодождите еще {time} секунд!");
            cooldownMsg = ChatColor.translateAlternateColorCodes('&', cooldownMsg);
            cooldownMsg = cooldownMsg.replace("{time}", String.valueOf(timeLeft));
            player.sendMessage(cooldownMsg);
            return true;
        }

        String link = streamManager.getLinkForPlayer(player.getName());
        if (link == null) {
            player.sendMessage(ChatColor.RED + "Вы не можете начать стрим без установленной ссылки. Используйте /setstreamlink <ссылка>");
            return true;
        }

        streamManager.startStreamAnnounce(player.getName(), link);
        player.sendMessage(ChatColor.GREEN + "Стрим запущен!");
        return true;
    }

    private boolean handleStopStream(Player player) {
        if (!player.hasPermission("kezstreamer.stream")) {
            player.sendMessage(ChatColor.RED + "У вас нет прав на эту команду.");
            return true;
        }

        if (streamManager.getActiveStreamer() == null) {
            player.sendMessage(ChatColor.RED + "Сейчас нет активных стримов.");
            return true;
        }

        if (!streamManager.getActiveStreamer().equals(player.getName())) {
            player.sendMessage(ChatColor.RED + "Вы можете остановить только свой стрим.");
            return true;
        }

        streamManager.stopStream(player.getName());
        player.sendMessage(ChatColor.GREEN + "Стрим остановлен.");
        return true;
    }

    private boolean handleSetStreamLink(Player player, String[] args) {
        if (!player.hasPermission("kezstreamer.stream")) {
            player.sendMessage(ChatColor.RED + "У вас нет прав на эту команду.");
            return true;
        }

        if (args.length == 0) {
            player.sendMessage(ChatColor.RED + "Укажите ссылку: /setstreamlink <ссылка>");
            return true;
        }

        String link = args[0];
        streamManager.saveLinkForPlayer(player.getName(), link);
        player.sendMessage(ChatColor.GREEN + "Ссылка на стрим установлена: " + link);
        return true;
    }

    private boolean handleMedia(Player player) {
        if (!player.hasPermission("kezstreamer.media")) {
            player.sendMessage(ChatColor.RED + "У вас нет прав на эту команду.");
            return true;
        }

        String header = plugin.getConfig().getString("messages.media-header",
                "&6&l▬▬▬▬▬▬▬▬▬▬▬▬ &5&lACTIVE STREAMS &6&l▬▬▬▬▬▬▬▬▬▬▬▬");
        header = ChatColor.translateAlternateColorCodes('&', header);
        player.sendMessage(header);

        String activeStreamer = streamManager.getActiveStreamer();
        if (activeStreamer == null) {
            String noStreams = plugin.getConfig().getString("messages.media-no-streams", "&cНет активных стримов.");
            noStreams = ChatColor.translateAlternateColorCodes('&', noStreams);
            player.sendMessage(noStreams);
        } else {
            String streamFormat = plugin.getConfig().getString("messages.media-stream-format", "&e{player} &7- ");
            streamFormat = ChatColor.translateAlternateColorCodes('&', streamFormat);
            streamFormat = streamFormat.replace("{player}", activeStreamer);

            String clickText = plugin.getConfig().getString("messages.media-click-text", "&6&l[СМОТРЕТЬ]");
            clickText = ChatColor.translateAlternateColorCodes('&', clickText);

            String link = streamManager.getActiveLink();

            TextComponent message = Component.text(streamFormat);
            TextComponent linkComponent = Component.text(clickText)
                    .clickEvent(ClickEvent.openUrl(link))
                    .hoverEvent(HoverEvent.showText(Component.text("§eНажмите, чтобы открыть стрим")));

            message = message.append(linkComponent);
            player.sendMessage(message);
        }

        String footer = plugin.getConfig().getString("messages.media-footer",
                "&6&l▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
        footer = ChatColor.translateAlternateColorCodes('&', footer);
        player.sendMessage(footer);

        return true;
    }
}