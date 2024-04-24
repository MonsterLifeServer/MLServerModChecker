package xyz.mlserver.mod.mlservermodchecker.listeners;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;

public class ForgeServerLoginListener {

    @SubscribeEvent
    public void onPlayerJoin(FMLNetworkEvent.ServerConnectionFromClientEvent  event) {
        String serverAddress = event.getManager().getRemoteAddress().toString();
        // サーバーアドレスが mc.mlserver.xyz の場合のみ、サーバーアクセスの処理を続行
        System.out.println(serverAddress);
        if (serverAddress.contains("sIHyHTkwgShYvLW.jp.normal.nohit.cc")) {
            String message = "サーバーにアクセスしました";
            TextComponentString comp = new TextComponentString(message);
            Style style = new Style().setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/lobby") {
                @Override
                public Action getAction() {
                    //custom behavior
                    return Action.RUN_COMMAND;
                }
            });
            comp.setStyle(style);
            // 3s後にメッセージを表示
            EntityPlayer player = Minecraft.getMinecraft().player;
            player.sendMessage(comp);
        }
    }

}
