package fr.aluny.gameimpl.chat.processor;

import fr.aluny.gameapi.chat.ChatPreProcessor;
import fr.aluny.gameapi.chat.ProcessedChat;
import fr.aluny.gameapi.moderation.ModerationService;
import net.kyori.adventure.text.minimessage.tag.resolver.Formatter;

public class MutePreProcessor implements ChatPreProcessor {

    private final ModerationService moderationService;

    public MutePreProcessor(ModerationService moderationService) {
        this.moderationService = moderationService;
    }

    @Override
    public void accept(ProcessedChat processedChat) {
        if (moderationService.isMuted(processedChat.getSender().getUuid())) {
            processedChat.setCancelled(true);
            processedChat.getSender().getMessageHandler().sendMessage("moderation_cancelled_muted", Formatter.date("date", moderationService.getUnMuteDate(processedChat.getSender().getUuid())));
        }
    }
}
