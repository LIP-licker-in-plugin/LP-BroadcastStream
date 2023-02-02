package com.darksoldier1404.dbs.functions;

import com.darksoldier1404.dbs.BroadcastStream;
import com.darksoldier1404.dppc.DPPCore;
import com.darksoldier1404.dppc.api.twitch.TwitchAPI;
import com.darksoldier1404.dppc.utils.ColorUtils;
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import com.github.twitch4j.common.events.domain.EventChannel;
import com.github.twitch4j.helix.domain.Stream;
import com.github.twitch4j.helix.domain.StreamList;
import com.github.twitch4j.helix.domain.User;
import com.github.twitch4j.helix.domain.UserList;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;

@SuppressWarnings("all")
public class DBSFunction {
    private static final BroadcastStream plugin = BroadcastStream.getInstance();
    public static final Map<UUID, String> registeredStreamer = new HashMap<>();
    public static final Map<UUID, String> joinedChannel = new HashMap<>();

    public static final Map<String, List<String>> blackList = new HashMap<>();

    public static void init() {
        if (!DPPCore.getInstance().config.getBoolean("Settings.use-twitch-api")) {
            System.out.println("TwitchAPI를 사용이 비활성화 되어있습니다.");
            return;
        }
        plugin.data.getConfig().getConfigurationSection("Settings.registeredStreamer").getKeys(false).forEach(k -> {
            registeredStreamer.put(UUID.fromString(plugin.data.getConfig().getString("Settings.registeredStreamer." + k + ".UUID")), k);
            TwitchAPI.enableStreamTracking(k);
            TwitchAPI.getTwitchClient().getChat().joinChannel(k);
            blackList.put(k, plugin.data.getConfig().getStringList("Settings.blackList." + k));
        });
    }

    public static void joinTwitchTextChannel(String username, Player p) {
        if (!registeredStreamer.containsValue(username)) {
            p.sendMessage(plugin.data.getPrefix() + username + "스트리머님은 등록되지 않았습니다.");
            return;
        }
        if (joinedChannel.containsKey(p.getUniqueId())) {
            String s = joinedChannel.get(p.getUniqueId());
            if (s.equals(username)) {
                p.sendMessage(plugin.data.getPrefix() + "이미 해당 채널에 입장하셨습니다.");
            } else {
                if (!isLive(username)) {
                    p.sendMessage(plugin.data.getPrefix() + "방송이 오프라인 입니다.");
                    return;
                }
                joinedChannel.remove(p.getUniqueId());
                joinedChannel.put(p.getUniqueId(), username);
                p.sendMessage(plugin.data.getPrefix() + username + "채널에 입장하였습니다.");
            }
        } else {
            joinedChannel.put(p.getUniqueId(), username);
            p.sendMessage(plugin.data.getPrefix() + username + "채널에 입장하였습니다.");
        }
    }

    public static void leaveTwitchTextChannel(Player p) {
        if (joinedChannel.containsKey(p.getUniqueId())) {
            joinedChannel.remove(p.getUniqueId());
            p.sendMessage(plugin.data.getPrefix() + "트위치 채널에서 퇴장하셨습니다.");
        } else {
            p.sendMessage(plugin.data.getPrefix() + "트위치 채널에 입장하지 않았습니다.");
        }
    }

    public static void sendOnAirMessage(Player p) {
        String s = registeredStreamer.get(p.getUniqueId());
        if (s == null) {
            p.sendMessage(plugin.data.getPrefix() + "트위치 채널을 등록하지 않으셨습니다.");
            return;
        }
        if (!isLive(s)) {
            p.sendMessage(plugin.data.getPrefix() + s + " 스트리머님은 방송중이 아닙니다.");
            return;
        }
        sendOnAirMessage(getStream(s), s);
    }

    public static void sendOnAirMessage(Stream s, String id) {
        TextComponent message = new TextComponent(ColorUtils.applyColor(initPlaceholder(plugin.data.getConfig().getString("Settings.liveAnnouncementMessage"), s, id)));
        message.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://www.twitch.tv/" + id));
        Bukkit.getOnlinePlayers().forEach(p -> {
            p.sendMessage(message.getText());
            if (plugin.data.getConfig().getBoolean("Settings.use-liveAnnouncementTitleMessage")) {
                String title = ColorUtils.applyColor(initPlaceholder(plugin.data.getConfig().getString("Settings.liveAnnouncementTitleMessage.title"), s, id));
                String subTitle = ColorUtils.applyColor(initPlaceholder(plugin.data.getConfig().getString("Settings.liveAnnouncementTitleMessage.subTitle"), s, id));
                int fadeIn, stay, fadeOut;
                try {
                    fadeIn = plugin.data.getConfig().getInt("Settings.liveAnnouncementTitleMessage.fadeIn");
                    stay = plugin.data.getConfig().getInt("Settings.liveAnnouncementTitleMessage.stay");
                    fadeOut = plugin.data.getConfig().getInt("Settings.liveAnnouncementTitleMessage.fadeOut");
                } catch (Exception e1) {
                    fadeIn = stay = fadeOut = 10;
                    System.out.println("[DBS] Error while reading config file. Default values are used.");
                }
                p.sendTitle(title, subTitle, fadeIn, stay, fadeOut);
            }
        });
    }

    public static void sendOffAirMessage(EventChannel ch, String id) {
        joinedChannel.forEach((k, v) -> {
            if (v.equals(id)) {
                joinedChannel.remove(k);
            }
        });
    }

    public static String initPlaceholder(String text, Stream s, String id) {
        return text.replace("<streamer>", s.getUserName())
                .replace("<title>", s.getTitle())
                .replace("<viewers>", String.valueOf(s.getViewerCount()))
                .replace("<game>", s.getGameName())
                .replace("<uptime>", s.getUptime().toString())
                .replace("<url>", "https://www.twitch.tv/" + id);
    }

    public static void sendMessageToTwitch(Player p, String message) {
        String streamer = joinedChannel.get(p.getUniqueId());
        if (streamer == null) {
            return;
        }
        if (blackList.get(streamer).contains(p.getName())) {
            return;
        }
        TwitchAPI.getTwitchClient().getChat().sendMessage(streamer, plugin.data.getConfig().getString("Settings.messageToTwitch")
                .replace("<message>", message)
                .replace("<nickname>", p.getName())
                .replace("<streamer>", streamer)
        );
    }

    public static void sendMessageToMinecraft(ChannelMessageEvent e) {
        if (blackList.get(e.getChannel().getName()).contains(e.getUser().getName())) {
            return;
        }
        Bukkit.getOnlinePlayers().forEach(p -> {
            if (joinedChannel.containsKey(p.getUniqueId())) {
                if (joinedChannel.get(p.getUniqueId()).equals(e.getChannel().getName())) {
                    p.sendMessage(ColorUtils.applyColor(plugin.data.getConfig().getString("Settings.messageToMinecraft")
                            .replace("<message>", e.getMessage())
                            .replace("<nickname>", e.getUser().getName())
                            .replace("<streamer>", e.getChannel().getName())
                    ));
                }
            }
        });
    }

    public static boolean isLive(String streamer) {
        try {
            UserList users = TwitchAPI.getTwitchClient().getHelix().getUsers(null, null, Arrays.asList(streamer)).execute();
            if (users.getUsers().size() == 1) {
                User user = users.getUsers().get(0);
                StreamList sl = TwitchAPI.getTwitchClient().getHelix().getStreams(null, null, null, null, null, null, Arrays.asList(user.getId()), null).execute();
                return sl.getStreams().get(0) != null;
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }

    public static Stream getStream(String streamer) {
        try {
            UserList users = TwitchAPI.getTwitchClient().getHelix().getUsers(null, null, Arrays.asList(streamer)).execute();
            if (users.getUsers().size() == 1) {
                User user = users.getUsers().get(0);
                StreamList sl = TwitchAPI.getTwitchClient().getHelix().getStreams(null, null, null, null, null, null, Arrays.asList(user.getId()), null).execute();
                return sl.getStreams().get(0);
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }

    public static User getUser(String streamer) {
        try {
            UserList users = TwitchAPI.getTwitchClient().getHelix().getUsers(null, null, Arrays.asList(streamer)).execute();
            if (users.getUsers().size() == 1) {
                return users.getUsers().get(0);
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }

    public static void registerChannel(Player p, String s) {
        if (registeredStreamer.containsKey(p.getUniqueId())) {
            p.sendMessage(plugin.data.getPrefix() + "이미 등록된 채널입니다.");
            return;
        }
        plugin.data.getConfig().set("Settings.registeredStreamer." + s + ".UUID", p.getUniqueId().toString());
        registeredStreamer.put(p.getUniqueId(), s);
        plugin.data.save();
        p.sendMessage(plugin.data.getPrefix() + "채널이 등록되었습니다.");
    }

    public static void unregisterChannel(Player p) {
        if (!registeredStreamer.containsKey(p.getUniqueId())) {
            p.sendMessage(plugin.data.getPrefix() + "등록된 채널이 없습니다.");
            return;
        }
        String s = registeredStreamer.get(p.getUniqueId());
        plugin.data.getConfig().set("Settings.registeredStreamer." + s, null);
        registeredStreamer.remove(p.getUniqueId());
        plugin.data.save();
        p.sendMessage(plugin.data.getPrefix() + "채널이 등록이 해제 되었습니다.");
    }

    public static void addBlackList(Player p, String username) {
        String ch = registeredStreamer.get(p.getUniqueId());
        if (blackList.get(ch).contains(username)) {
            p.sendMessage(plugin.data.getPrefix() + "이미 블랙리스트에 있습니다.");
            return;
        }
        plugin.data.getConfig().set("Settings.blackList." + ch + "." + username, "true");
        blackList.get(ch).add(username);
        plugin.data.save();
        p.sendMessage(plugin.data.getPrefix() + "블랙리스트에 추가되었습니다.");
    }

    public static void removeBlackList(Player p, String username) {
        String ch = registeredStreamer.get(p.getUniqueId());
        if (!blackList.get(ch).contains(username)) {
            p.sendMessage(plugin.data.getPrefix() + "블랙리스트에 없습니다.");
            return;
        }
        plugin.data.getConfig().set("Settings.blackList." + ch + "." + username, null);
        blackList.get(ch).remove(username);
        plugin.data.save();
        p.sendMessage(plugin.data.getPrefix() + "블랙리스트에서 삭제되었습니다.");
    }
}
