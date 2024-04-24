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
            Style style;
            for (ModContainer mod : list) {
                comp = new TextComponentString("- " + mod.getName() + "(" + mod.getModId() + ") - バージョン: " + mod.getVersion());
                player.sendMessage(comp);
            }
            comp = new TextComponentString("");
            player.sendMessage(comp);
            String message = "ホワイトリストに登録されていないModがあります。クリックで許可申請を送信可能です。(強制するものではありませんがMLServerでは禁止されています。)";
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
            List<ModContainer> list = MLServerModCheckerWhitelistMods.getNoWhitelistMods();
            if (list.isEmpty()) {
                String message = "ホワイトリストに登録されていないModはありません。";
                TextComponentString comp = new TextComponentString(message);
                player.sendMessage(comp);
                return;
            }
            String message = "{" +
                    "    \"data\": [";
            for (ModContainer mod : list) {
                message +=
                        "        {" +
                        "            \"modId\": \"" + mod.getModId() + "\"," +
                        "            \"modName\": \"" + mod.getName() + "\"," +
                        "            \"modVersion\": \"" + mod.getVersion() + "\"" +
                        "        },";
            }
            message += "    ]" +
                    "}";
            try {
                String result = callWebAPI(message);
                System.out.println(result);
                TextComponentString comp = new TextComponentString("許可申請を送信しました。");
                player.sendMessage(comp);
            } catch (IOException e) {
                e.printStackTrace();
                TextComponentString comp = new TextComponentString("許可申請の送信に失敗しました。");
                player.sendMessage(comp);
            }

        }
    }

    private static final String WEB_API_ENDPOINT = "https://wiki.mlserver.xyz/api/whitelist_mods/request.php";

    public String callWebAPI(String postJson) throws IOException {

        final Map<String, String> httpHeaders = new LinkedHashMap<String, String>();
        final String resultStr = doPost(WEB_API_ENDPOINT, "UTF-8", httpHeaders, postJson);

        return resultStr;
    }

    public String doPost(String url, String encoding, Map<String, String> headers, String jsonString) throws IOException {
        final okhttp3.MediaType mediaTypeJson = okhttp3.MediaType.parse("application/json; charset=" + encoding);

        final RequestBody requestBody = RequestBody.create(mediaTypeJson, jsonString);

        final Request request = new Request.Builder()
                .url(url)
                .headers(Headers.of(headers))
                .post(requestBody)
                .build();

        final OkHttpClient client = new OkHttpClient.Builder()
                .build();
        final Response response = client.newCall(request).execute();
        final String resultStr = response.body().string();
        return resultStr;
    }
}
