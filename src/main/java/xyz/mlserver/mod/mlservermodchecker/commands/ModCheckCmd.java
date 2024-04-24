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
import xyz.mlserver.mod.mlservermodchecker.MLServerModChecker;
import xyz.mlserver.mod.mlservermodchecker.MLServerModCheckerWhitelistMods;

import java.util.List;

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
            Style style;
            for (ModContainer mod : list) {
                comp = new TextComponentString("- " + mod.getName() + "(" + mod.getModId() + ") - バージョン: " + mod.getVersion());
//                style = new Style().setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, mod.getUpdateUrl()) {
//                    @Override
//                    public Action getAction() {
//                        //custom behavior
//                        return Action.OPEN_URL;
//                    }
//                });
//                comp.setStyle(style);
                player.sendMessage(comp);
            }
            comp = new TextComponentString("");
            player.sendMessage(comp);
            String message = "ホワイトリストに登録されていないModがあります。クリックでレポートを送信可能です。(強制するものではありませんがMLServerでは禁止されています。)";
            comp = new TextComponentString(message);
            style = new Style().setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/modcheck sendrequest") {
                @Override
                public Action getAction() {
                    //custom behavior
                    return Action.RUN_COMMAND;
                }
            });
            comp.setStyle(style);
            // 3s後にメッセージを表示
            player.sendMessage(comp);
        } else if (args[0].equalsIgnoreCase("sendrequest")) {

        }
    }
}
