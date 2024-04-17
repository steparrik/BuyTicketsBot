package steparrik.code.payticketsbot.service;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import steparrik.code.payticketsbot.service.factory.MessageFactory;
import steparrik.code.payticketsbot.service.handler.MessageHandler;
import steparrik.code.payticketsbot.service.handler.PreCheckoutHandler;
import steparrik.code.payticketsbot.service.handler.SuccessfulPayHandler;
import steparrik.code.payticketsbot.telegram.Bot;


@Component
@Slf4j
public class UpdateDispatcher {
    private final SuccessfulPayHandler successfulPayHandler;
    private final MessageHandler messageHandler;
    private final PreCheckoutHandler preCheckoutHandler;
    private final MessageFactory messageFactory;



    @Autowired
    public UpdateDispatcher(SuccessfulPayHandler successfulPayHandler, MessageHandler messageHandler, PreCheckoutHandler preCheckoutHabdler, MessageFactory messageFactory) {
        this.successfulPayHandler = successfulPayHandler;
        this.messageHandler = messageHandler;
        this.preCheckoutHandler = preCheckoutHabdler;

        this.messageFactory = messageFactory;
    }

    public BotApiMethod<?> distribute(Update update, Bot bot) {
        if (update.hasPreCheckoutQuery()) {
            return preCheckoutHandler.answer(update.getPreCheckoutQuery(), bot);
        }
        if (update.hasMessage()) {
            Message message = update.getMessage();
            if (message.hasText()) {
                return messageHandler.answer(message, bot);
            }else if(message.hasSuccessfulPayment()){
                return successfulPayHandler.answer(message, bot);
            }
        }
        log.error("Unsupported update: " + update);
        return messageFactory.sendMes(update.getMessage().getChatId()+"", "*Данный формат сообщения не поддерживается*", null, bot);
    }




}
