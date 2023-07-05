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
    public void accept(ProcessedChat chat) {
        if (moderationService.isMuted(chat.getSender().getUuid())) {
            chat.setCancelled(true);
            chat.getSender().getMessageHandler().sendMessage("moderation_cancelled_muted", Formatter.date("date", moderationService.getUnMuteDate(chat.getSender().getUuid())));
        }
    }
}
