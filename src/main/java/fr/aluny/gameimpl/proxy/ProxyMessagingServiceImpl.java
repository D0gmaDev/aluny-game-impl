package fr.aluny.gameimpl.proxy;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import fr.aluny.gameapi.proxy.ProxyMessagingService;
import java.util.Arrays;
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
    public void kickFromProxy(Player sender, String targetName, String reason) {
        sendData(sender, "KickPlayer", targetName, reason);
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
