package xyz.mlserver.mod.mlservermodchecker.commands;

import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraftforge.fml.common.ModContainer;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import xyz.mlserver.mod.mlservermodchecker.MLServerModChecker;
import xyz.mlserver.mod.mlservermodchecker.MLServerModCheckerWhitelistMods;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ModCheckCmd extends CommandBase {

    @Override
    public String getName() {
        return "modcheck";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/modcheck";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) {
        EntityPlayer player = Minecraft.getMinecraft().player;
        if (args.length == 0) {
            MLServerModCheckerWhitelistMods.fetchModList(MLServerModChecker.loader);
            String message = "メッセージクリックでModチェックを行います。このチェックはMonsterLifeServerへレポートされません。";
            TextComponentString comp = new TextComponentString(message);
            Style style = new Style().setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/modcheck confirm") {
                @Override
                public Action getAction() {
                    //custom behavior
                    return Action.RUN_COMMAND;
                }
            });
            comp.setStyle(style);
            // 3s後にメッセージを表示
            player.sendMessage(comp);
        } else if (args[0].equalsIgnoreCase("confirm")) {
            List<ModContainer> list = MLServerModCheckerWhitelistMods.getNoWhitelistMods();
            if (list.isEmpty()) {
                String message = "ホワイトリストに登録されていないModはありません。";
                TextComponentString comp = new TextComponentString(message);
                player.sendMessage(comp);
                return;
            }
            TextComponentString comp;
            comp = new TextComponentString("ホワイトリストに登録されていないMod:");
            player.sendMessage(comp);
            StringBuilder json_text = new StringBuilder("{" +
                    "\"data\": [");
            boolean is_first = true;
            for (ModContainer mod : list) {
                comp = new TextComponentString("- " + mod.getName() + "(" + mod.getModId() + ") - バージョン: " + mod.getVersion());
                player.sendMessage(comp);
                if (!is_first) {
                    json_text.append(",");
                }
                json_text
                        .append("{")
                        .append("\"mod_id\": \"").append(mod.getModId()).append("\",")
                        .append("\"mod_name\": \"").append(mod.getName()).append("\",")
                        .append("\"mod_version\": \"").append(mod.getVersion()).append("\"")
                        .append("}");
                is_first = false;
            }
            json_text.append("]" + "}");

            MLServerModCheckerWhitelistMods.exportNoWhitelistMods(json_text.toString());

            comp = new TextComponentString("");
            player.sendMessage(comp);
            String message = "ホワイトリストに登録されていないModがあります。./config/mlservermodchecker/no_whitelist_mods.jsonに保存されました。";
            comp = new TextComponentString(message);
            // 3s後にメッセージを表示
            player.sendMessage(comp);
        }
    }
}
