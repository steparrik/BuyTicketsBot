package steparrik.code.payticketsbot.telegram;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.bots.TelegramWebhookBot;
import org.telegram.telegrambots.meta.api.methods.AnswerPreCheckoutQuery;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.invoices.CreateInvoiceLink;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.payments.LabeledPrice;
import org.telegram.telegrambots.meta.api.objects.payments.PreCheckoutQuery;
import org.telegram.telegrambots.meta.api.objects.payments.SuccessfulPayment;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import steparrik.code.payticketsbot.entity.User;
import steparrik.code.payticketsbot.repositories.UserRepository;
import steparrik.code.payticketsbot.service.UpdateDispatcher;


import java.util.Collections;
import java.util.List;
import java.util.Optional;


@Component
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Bot extends TelegramLongPollingBot {
    final UpdateDispatcher updateDispatcher;
    final TelegramProperties telegramProperties;
    final UserRepository userRepository;
    private static volatile Integer GlobalPrice = 10000;
    private static volatile String GlobalPassword = "123456789";

    @Autowired
    public Bot(UpdateDispatcher updateDispatcher, TelegramProperties telegramProperties, UserRepository userRepository) {
        super(telegramProperties.getToken());
        this.updateDispatcher = updateDispatcher;
        this.telegramProperties = telegramProperties;
        this.userRepository = userRepository;
    }


    @Override
    public String getBotUsername() {
        return telegramProperties.getUsername();
    }


    @Override
    public void onUpdateReceived(Update update) {
        try {
            this.execute(updateDispatcher.distribute(update, this));
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }
}

