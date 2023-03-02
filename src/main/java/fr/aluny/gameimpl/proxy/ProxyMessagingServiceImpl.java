package fr.aluny.gameimpl.proxy;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import fr.aluny.gameapi.proxy.ProxyMessagingService;
import java.util.Arrays;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;

public class ProxyMessagingServiceImpl implements ProxyMessagingService, PluginMessageListener {

    private final JavaPlugin plugin;

    private String[] onlinePlayers = new String[0];

    public ProxyMessagingServiceImpl(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void connect(Player player, String server) {
        sendData(player, "Connect", server);
    }

    @Override
    public void kickFromProxy(Player sender, String targetName, String reason) {
        sendData(sender, "KickPlayer", targetName, reason);
    }

    @Override
    public void sendMessage(Player sender, String targetName, BaseComponent message) {
        sendData(sender, "MessageRaw", targetName, ComponentSerializer.toString(message));
    }

    @Override
    public void sendMessage(Player sender, String targetName, String message) {
        sendData(sender, "Message", targetName, message);
    }

    @Override
    public String[] getOnlinePlayerNames(Player sender) {
        sendData(sender, "PlayerList", "ALL");
        return this.onlinePlayers;
    }

    @Override
    public void initialize() {
        this.plugin.getServer().getMessenger().registerOutgoingPluginChannel(this.plugin, "BungeeCord");
        this.plugin.getServer().getMessenger().registerIncomingPluginChannel(this.plugin, "BungeeCord", this);
    }

    private void sendData(Player player, String... utfData) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        Arrays.stream(utfData).forEach(out::writeUTF);

        player.sendPluginMessage(this.plugin, "BungeeCord", out.toByteArray());
    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        if (!channel.equals("BungeeCord")) {
            return;
        }
        ByteArrayDataInput in = ByteStreams.newDataInput(message);

        if (in.readUTF().equals("PlayerList")) {
            this.onlinePlayers = in.readUTF().split(", ");
        }
    }
}
