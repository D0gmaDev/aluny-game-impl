package fr.aluny.gameimpl.chat.processor;

import fr.aluny.gameapi.chat.ChatProcessor;
import fr.aluny.gameapi.chat.ProcessedChat;
import fr.aluny.gameapi.message.MessageService;
import fr.aluny.gameapi.player.PlayerAccountService;
import fr.aluny.gameapi.player.rank.Rank;
import fr.aluny.gameapi.translation.TranslationService;
import fr.aluny.gameimpl.player.GamePlayerImpl;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;

public class DefaultChatProcessor implements ChatProcessor {

    private final TranslationService   translationService;
    private final PlayerAccountService accountService;

    private String chatFormat;

    public DefaultChatProcessor(TranslationService translationService, PlayerAccountService accountService) {
        this.translationService = translationService;
        this.accountService = accountService;
    }

    @Override
    public void accept(ProcessedChat processedChat) {

        if (this.chatFormat == null)
            this.chatFormat = translationService.getDefaultLocale().translate("default_chat_format");

        Rank highestRank = processedChat.getSender() instanceof GamePlayerImpl gamePlayer ? gamePlayer.getCachedHighestRank() : accountService.getPlayerAccount(processedChat.getSender()).getHighestRank();

        Component prefix = Component.text(highestRank.getPrefix(), highestRank.getTextColor());
        Component name = Component.text(processedChat.getSender().getPlayerName(), highestRank.getTextColor());

        Bukkit.getOnlinePlayers().forEach(player -> player.sendMessage(MessageService.COMPONENT_PARSER.deserialize(this.chatFormat,
                Placeholder.component("prefix", prefix),
                Placeholder.component("name", name),
                Placeholder.component("message", processedChat.getContentForPlayer(player.getUniqueId()))
        )));

        Bukkit.getLogger().info("[CHAT] " + processedChat.getSender().getPlayerName() + ": " + processedChat.getMessageContent().content());
    }
}
