package steparrik.code.payticketsbot.service.factory;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import steparrik.code.payticketsbot.telegram.Bot;

@Component
@Slf4j
public class MessageFactory {
    public BotApiMethod<?> sendMes(String chatId, String text, InlineKeyboardMarkup inlineKeyboardMarkup, Bot bot){
            return  SendMessage.builder()
                    .chatId(chatId)
                    .text(text)
                    .parseMode("Markdown")
                    .replyMarkup(inlineKeyboardMarkup)
                    .build();
    }
}
