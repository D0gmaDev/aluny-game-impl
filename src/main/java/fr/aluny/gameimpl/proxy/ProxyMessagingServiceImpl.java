package fr.aluny.gameimpl.proxy;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import fr.aluny.gameapi.proxy.ProxyMessagingService;
import java.util.Arrays;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class ProxyMessagingServiceImpl implements ProxyMessagingService {

    private final JavaPlugin plugin;

    public ProxyMessagingServiceImpl(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void connect(Player player, String server) {
        sendData(player, "Connect", server);
    }

    @Override
    public void connectOther(Player sender, String targetName, String server) {
        sendData(sender, "ConnectOther", targetName, server);
    }

    @Override
    public void kickFromProxy(Player sender, String targetName, Component reason) {
        sendData(sender, "KickPlayer", targetName, LegacyComponentSerializer.legacySection().serialize(reason));
    }

    @Override
    public void sendMessage(Player sender, String targetName, BaseComponent message) {
        sendData(sender, "MessageRaw", targetName, ComponentSerializer.toString(message));
    }

    @Override
    public void sendMessage(Player sender, String targetName, Component message) {
        sendData(sender, "MessageRaw", targetName, GsonComponentSerializer.gson().serialize(message));
    }

    @Override
    public void sendMessage(Player sender, String targetName, String message) {
        sendData(sender, "Message", targetName, message);
    }

    @Override
    public void initialize() {
        this.plugin.getServer().getMessenger().registerOutgoingPluginChannel(this.plugin, "BungeeCord");
    }

    private void sendData(Player player, String... utfData) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        Arrays.stream(utfData).forEach(out::writeUTF);

        player.sendPluginMessage(this.plugin, "BungeeCord", out.toByteArray());
    }
}
