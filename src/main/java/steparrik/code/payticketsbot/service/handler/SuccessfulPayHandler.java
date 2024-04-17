package steparrik.code.payticketsbot.service.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.AnswerPreCheckoutQuery;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.payments.PreCheckoutQuery;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import steparrik.code.payticketsbot.entity.User;
import steparrik.code.payticketsbot.repositories.UserRepository;
import steparrik.code.payticketsbot.service.factory.MessageFactory;
import steparrik.code.payticketsbot.telegram.Bot;

@Component
public class SuccessfulPayHandler {
    private final MessageFactory messageFactory;
    private final UserRepository userRepository;

    @Autowired
    public SuccessfulPayHandler(MessageFactory messageFactory, UserRepository userRepository) {
        this.messageFactory = messageFactory;
        this.userRepository = userRepository;
    }

    public BotApiMethod<?> answer(Message message, Bot bot) {
        User user = userRepository.findByChatId(message.getChatId()+"").orElse(null);
        user.setAcces(true);
        user.setName(message.getSuccessfulPayment().getOrderInfo().getName());
        user.setPhoneNumber(message.getSuccessfulPayment().getOrderInfo().getPhoneNumber());
        userRepository.save(user);
        try {
            bot.execute(DeleteMessage.builder().messageId(message.getMessageId() - 1
            ).chatId(user.getChatId()).build());
        } catch (TelegramApiException e) {
            return null;
        }
        return messageFactory.sendMes(user.getChatId(), "*Вы стали одним из участников розыгрыша!\n\n" +
                "Ваш билет:*\n*Имя* - " +user.getName() +
                "\n*Номер телефона* - "+user.getPhoneNumber()+
                "\n*Номер билета* - " + user.getId(), null, bot);
    }
}
