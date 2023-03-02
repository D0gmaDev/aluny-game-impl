package fr.aluny.gameimpl.chat.processor;

import fr.aluny.gameapi.chat.ChatProcessor;
import fr.aluny.gameapi.chat.ProcessedChat;
import fr.aluny.gameapi.player.PlayerAccountService;
import fr.aluny.gameapi.player.rank.Rank;
import fr.aluny.gameapi.translation.TranslationService;
import fr.aluny.gameimpl.player.GamePlayerImpl;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

public class DefaultChatProcessor implements ChatProcessor {

    private static final String MESSAGE_PLACEHOLDER = "§message§";

    private final TranslationService   translationService;
    private final PlayerAccountService accountService;

    public DefaultChatProcessor(TranslationService translationService, PlayerAccountService accountService) {
        this.translationService = translationService;
        this.accountService = accountService;
    }

    @Override
    public void accept(ProcessedChat processedChat) {
        Rank highestRank = processedChat.getSender() instanceof GamePlayerImpl gamePlayer ? gamePlayer.getCachedHighestRank() : accountService.getPlayerAccount(processedChat.getSender()).getHighestRank();

        String chat = this.translationService.getDefaultLocale().translate("default_chat_format", highestRank.getPrefix(), processedChat.getSender().getPlayerName(), MESSAGE_PLACEHOLDER);
        Bukkit.getOnlinePlayers().forEach(player -> player.sendMessage(chat.replaceFirst(MESSAGE_PLACEHOLDER, processedChat.getContentForPlayer(player.getUniqueId()))));
        Bukkit.getLogger().info("[CHAT] " + processedChat.getSender().getPlayerName() + ": " + ChatColor.stripColor(processedChat.getMessageContent()));
    }
}
