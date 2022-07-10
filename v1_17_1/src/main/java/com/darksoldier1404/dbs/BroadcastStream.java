package com.darksoldier1404.dbs;

import com.darksoldier1404.dbs.commands.DBSCommand;
import com.darksoldier1404.dbs.events.DBSEvent;
import com.darksoldier1404.dbs.functions.DBSFunction;
import com.darksoldier1404.dppc.utils.DataContainer;
import org.bukkit.plugin.java.JavaPlugin;

public class BroadcastStream extends JavaPlugin {
    private static BroadcastStream plugin;
    public static DataContainer data;
    public static BroadcastStream getInstance() {
        return plugin;
    }

    @Override
    public void onEnable() {
        plugin = this;
        data = new DataContainer(plugin);
        DBSFunction.init();
        plugin.getServer().getPluginManager().registerEvents(new DBSEvent(), plugin);
        getCommand("dbs").setExecutor(new DBSCommand());
    }

    @Override
    public void onDisable() {
        data.save();
    }
}
