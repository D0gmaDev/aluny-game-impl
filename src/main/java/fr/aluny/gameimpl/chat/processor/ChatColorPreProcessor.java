package fr.aluny.gameimpl.chat.processor;

import fr.aluny.gameapi.chat.ChatPreProcessor;
import fr.aluny.gameapi.chat.ProcessedChat;
import fr.aluny.gameapi.player.PlayerAccountService;
import fr.aluny.gameapi.player.rank.Rank;
import fr.aluny.gameimpl.player.GamePlayerImpl;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;

public class ChatColorPreProcessor implements ChatPreProcessor {

    private final PlayerAccountService accountService;

    public ChatColorPreProcessor(PlayerAccountService accountService) {
        this.accountService = accountService;
    }

    @Override
    public void accept(ProcessedChat processedChat) {
        Rank highestRank = processedChat.getSender() instanceof GamePlayerImpl gamePlayer ? gamePlayer.getCachedHighestRank() : accountService.getPlayerAccount(processedChat.getSender()).getHighestRank();

        TextColor color = highestRank.hasPermission("fr.aluny.chat.white_message") ? NamedTextColor.WHITE : NamedTextColor.GRAY;
        processedChat.setMessage(processedChat.getMessage().color(color));
    }
}
