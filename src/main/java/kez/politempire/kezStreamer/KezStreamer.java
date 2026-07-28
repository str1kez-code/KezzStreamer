package kez.politempire.kezStreamer;

import org.bukkit.plugin.java.JavaPlugin;

public final class KezStreamer extends JavaPlugin {
    private static KezStreamer instance;
    private StreamManager streamManager;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        createListConfig();
        streamManager = new StreamManager(this);

        CommandExecutor commandExecutor = new CommandExecutor(this, streamManager);
        getCommand("startstream").setExecutor(commandExecutor);
        getCommand("setstreamlink").setExecutor(commandExecutor);
        getCommand("stopstream").setExecutor(commandExecutor);
        getCommand("media").setExecutor(commandExecutor);

        getLogger().info("KezStreamer успешно загружен!");
    }

    @Override
    public void onDisable() {
        if (streamManager != null) {
            streamManager.stopAllTasks();
        }
        getLogger().info("KezStreamer выгружен!");
    }

    private void createListConfig() {
        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }
        java.io.File listFile = new java.io.File(getDataFolder(), "list.yml");
        if (!listFile.exists()) {
            try {
                listFile.createNewFile();
                org.bukkit.configuration.file.YamlConfiguration listConfig =
                        org.bukkit.configuration.file.YamlConfiguration.loadConfiguration(listFile);
                listConfig.set("streamers", new java.util.HashMap<String, String>());
                listConfig.save(listFile);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static KezStreamer getInstance() {
        return instance;
    }

    public StreamManager getStreamManager() {
        return streamManager;
    }
}