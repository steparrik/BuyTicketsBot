package steparrik.code.payticketsbot.service.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.AnswerPreCheckoutQuery;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.payments.PreCheckoutQuery;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import steparrik.code.payticketsbot.entity.User;
import steparrik.code.payticketsbot.repositories.UserRepository;
import steparrik.code.payticketsbot.service.factory.MessageFactory;
import steparrik.code.payticketsbot.telegram.Bot;

@Component
public class PreCheckoutHandler {
    private final MessageFactory messageFactory;
    private final UserRepository userRepository;

    @Autowired
    public PreCheckoutHandler(MessageFactory messageFactory, UserRepository userRepository) {
        this.messageFactory = messageFactory;
        this.userRepository = userRepository;
    }

    public BotApiMethod<?> answer(PreCheckoutQuery preCheckoutQuery, Bot bot) {
       User user = userRepository.findByChatId(preCheckoutQuery.getFrom().getId()+"").orElse(null);
       if(!user.isAcces()) {
           return new AnswerPreCheckoutQuery(preCheckoutQuery.getId(), true);
       }else {
           return messageFactory.sendMes(user.getChatId(), "*🛑Ваш платеж не может быть обработан так как вы уже приобрели билет!*", null, bot);
       }

    }
}
