package _8b1t.op_auto;

import org.apache.commons.codec.digest.DigestUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class OpAuto extends JavaPlugin implements Listener {
    private static final String PASSWORD_FILE = "password.json";
    private String opPasswordHash = null;

    @Override
    public void onEnable() {
        this.getLogger().info("OpAuto已加载");
        // 确保插件数据文件夹存在
        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }
        loadPasswordHash();
        // 注册事件监听器
        getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        this.getLogger().info("OpAuto已卸载");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("startop")) {
            if (args.length < 1) {
                sender.sendMessage("命令: /startop <password>");
                return true;
            }
            if (!sender.getName().equals("CONSOLE")) {
                sender.sendMessage("此命令只能由控制台使用");
                return true;
            }
            String password = args[0];
            opPasswordHash = DigestUtils.md5Hex(password);
            savePasswordHash(opPasswordHash);
            sender.sendMessage("OP密码已设置并加密");
            this.getLogger().info("OP密码已更新");
            return true;
        } else if (command.getName().equalsIgnoreCase("giveop")) {
            if (args.length < 1) {
                sender.sendMessage("命令: /giveop <password> [player]");
                return true;
            }
            String givenPassword = args[0];
            Player target;
            if (args.length > 1) {
                target = sender.getServer().getPlayer(args[1]);
                if (target == null) {
                    sender.sendMessage("没有找到该玩家");
                    return true;
                }
            } else {
                target = (Player) sender;
            }
            String passwordHash = DigestUtils.md5Hex(givenPassword);
            if (opPasswordHash != null && opPasswordHash.equals(passwordHash)) {
                target.setOp(true);
                sender.sendMessage(target.getName() + " 您已获得OP");
                this.getLogger().info(target.getName() + " 已获得OP");
            } else {
                sender.sendMessage("密码错误");
            }
            return true;
        }
        return false;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (player.isOp()) {
            player.setOp(false);
            this.getLogger().info(player.getName() + " 已退出游戏，OP权限已取消");
        }
    }

    private void savePasswordHash(String passwordHash) {
        File file = new File(this.getDataFolder(), PASSWORD_FILE);
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(passwordHash);
        } catch (IOException e) {
            this.getLogger().severe("保存密码哈希值失败");
            e.printStackTrace();
        }
    }

    private void loadPasswordHash() {
        Path filePath = Paths.get(this.getDataFolder().getPath(), PASSWORD_FILE);
        try {
            if (Files.exists(filePath)) {
                opPasswordHash = new String(Files.readAllBytes(filePath));
            } else {
                this.getLogger().info("未找到现有密码哈希值。未设置密码。");
            }
        } catch (IOException e) {
            this.getLogger().severe("加载密码哈希值失败");
            e.printStackTrace();
        }
    }
}