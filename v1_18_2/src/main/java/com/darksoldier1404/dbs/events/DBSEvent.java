package com.darksoldier1404.dbs.events;

import com.darksoldier1404.dbs.BroadcastStream;
import com.darksoldier1404.dbs.functions.DBSFunction;
import com.darksoldier1404.dppc.api.twitch.TwitchLiveEvent;
import com.darksoldier1404.dppc.api.twitch.TwitchMessageEvent;
import com.darksoldier1404.dppc.api.twitch.TwitchOfflineEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class DBSEvent implements Listener {
    private final BroadcastStream plugin = BroadcastStream.getInstance();

    @EventHandler
    public void onLive(TwitchLiveEvent e) {
        DBSFunction.sendOnAirMessage(e.getStream(), e.getChannel().getName());
    }

    @EventHandler
    public void onLiveMessage(TwitchMessageEvent e) {
        DBSFunction.sendMessageToMinecraft(e.getEvent());
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        DBSFunction.sendMessageToTwitch(e.getPlayer(), e.getMessage());
    }

    @EventHandler
    public void onOffline(TwitchOfflineEvent e) {
        DBSFunction.sendOffAirMessage(e.getChannel(), e.getChannel().getName());
    }
}
