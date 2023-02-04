package com.darksoldier1404.dbs.commands;

import com.darksoldier1404.dbs.BroadcastStream;
import com.darksoldier1404.dbs.functions.DBSFunction;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings("all")
public class DBSCommand implements CommandExecutor, TabCompleter {
    private final BroadcastStream plugin = BroadcastStream.getInstance();

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used in-game.");
            return false;
        }
        Player p = (Player) sender;
        if (args.length == 0) {
            if(p.hasPermission("dbs.use")){
                p.sendMessage(plugin.data.getPrefix() + "/dbs join/접속 <channel> - 해당 트위치 채널에 참여합니다.");
                p.sendMessage(plugin.data.getPrefix() + "/dbs leave/퇴장 - 접속중인 트위치 채널에서 떠납니다.");
            }
            // for the streamer's channel
            if(p.hasPermission("dbs.streamer")) {
                p.sendMessage(plugin.data.getPrefix() + "/dbs register/등록 <channel> - 자신의 트위치 채널을 등록합니다.");
                p.sendMessage(plugin.data.getPrefix() + "/dbs unregister/등록해제 <channel> - 자신의 트위치 채널을 등록 해제합니다.");
                p.sendMessage(plugin.data.getPrefix() + "/dbs onair/알림 - 자신의 트위치 생방송 알림을 띄웁니다.");
                p.sendMessage(plugin.data.getPrefix() + "/dbs blacklist/블랙리스트 <username> - 해당 유저를 블랙리스트에 추가합니다.");
                p.sendMessage(plugin.data.getPrefix() + "/dbs unblacklist/블랙리스트해제 <username> - 해당 유저를 블랙리스트에서 제거합니다.");
            }
            return false;
        }
        if(!p.hasPermission("dbs.use")) {
            p.sendMessage(plugin.data.getPrefix() + "권한이 없습니다.");
            return false;
        }
        if (args[0].equalsIgnoreCase("join") || args[0].equals("접속")) {
            if (args.length == 1) {
                p.sendMessage(plugin.data.getPrefix() + "/dbs join <channel> - 트위치 채널에 참여합니다.");
                return false;
            }
            DBSFunction.joinTwitchTextChannel(args[1], p);
            return false;
        }
        if (args[0].equalsIgnoreCase("leave") || args[0].equals("퇴장")) {
            DBSFunction.leaveTwitchTextChannel(p);
            return false;
        }
        if(!p.hasPermission("dbs.streamer")) {
            p.sendMessage(plugin.data.getPrefix() + "권한이 없습니다.");
            return false;
        }
        if (args[0].equalsIgnoreCase("onair") || args[0].equals("알림")) {
            DBSFunction.sendOnAirMessage(p);
            return false;
        }
        if (args[0].equalsIgnoreCase("register") || args[0].equals("등록")) {
            if (args.length == 1) {
                p.sendMessage(plugin.data.getPrefix() + "/dbs register <channel> - 자신의 트위치 채널을 등록합니다.");
                return false;
            }
            DBSFunction.registerChannel(p, args[1]);
            return false;
        }
        if (args[0].equalsIgnoreCase("unregister") || args[0].equals("등록해제")) {
            if (args.length == 1) {
                p.sendMessage(plugin.data.getPrefix() + "/dbs unregister <channel> - 자신의 트위치 채널을 등록 해제합니다.");
                return false;
            }
            DBSFunction.unregisterChannel(p);
            return false;
        }
        if (args[0].equalsIgnoreCase("blacklist") || args[0].equals("블랙리스트")) {
            if (args.length == 1) {
                p.sendMessage(plugin.data.getPrefix() + "/dbs blacklist <username> - 해당 트위치 채널을 블랙리스트에 추가합니다.");
                return false;
            }
            DBSFunction.addBlackList(p, args[1]);
            return false;
        }
        if (args[0].equalsIgnoreCase("unblacklist") || args[0].equals("블랙리스트해제")) {
            if (args.length == 1) {
                p.sendMessage(plugin.data.getPrefix() + "/dbs unblacklist <username> - 해당 트위치 채널을 블랙리스트에서 제거합니다.");
                return false;
            }
            DBSFunction.removeBlackList(p, args[1]);
            return false;
        }
        return false;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            return null;
        }
        Player p = (Player) sender;
        if (args.length == 1) {
            if (sender.hasPermission("dbs.streamer")) {
                return Arrays.asList("접속", "퇴장", "등록", "등록해제", "알림", "블랙리스트", "블랙리스트해제");
            }
            return Arrays.asList("접속", "퇴장");
        }
        if (args[0].equalsIgnoreCase("join") || args[0].equals("접속")) {
            if (args.length == 2) {
                return DBSFunction.registeredStreamer.values().stream().collect(Collectors.toList());
            }
        }
        if (args[0].equalsIgnoreCase("unblacklist") || args[0].equals("블랙리스트해제")) {
            if (args.length == 2) {
                if (DBSFunction.blackList.get(p.getUniqueId()) != null) {
                    return DBSFunction.blackList.get(p.getUniqueId());
                }
            }
        }
        return null;
    }
}
