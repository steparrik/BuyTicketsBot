package steparrik.code.payticketsbot.service.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.invoices.CreateInvoiceLink;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.payments.LabeledPrice;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import steparrik.code.payticketsbot.entity.User;
import steparrik.code.payticketsbot.repositories.UserRepository;
import steparrik.code.payticketsbot.service.factory.MessageFactory;
import steparrik.code.payticketsbot.service.globalVar.GlobalVar;
import steparrik.code.payticketsbot.telegram.Bot;
import steparrik.code.payticketsbot.telegram.TelegramProperties;

import java.util.Collections;
import java.util.List;

@Component
public class MessageHandler {
    private final MessageFactory messageFactory;
    private final UserRepository userRepository;
    private final TelegramProperties telegramProperties;
    private final GlobalVar globalVar;

    @Autowired
    public MessageHandler(MessageFactory messageFactory, UserRepository userRepository, TelegramProperties telegramProperties) {
        this.messageFactory = messageFactory;
        this.userRepository = userRepository;
        this.telegramProperties = telegramProperties;
        globalVar = GlobalVar.getInstance();
    }

    public BotApiMethod<?> answer(Message message, Bot bot) {
        User user = userRepository.findByChatId(message.getChatId() + "").orElse(null);
        String messageText = message.getText();
        if (user == null) {
            user = userRepository.save(User.builder().chatId(message.getChatId() + "").userName(message.getChat().getFirstName()).acces(false).ROLE("USER").build());
        }
        if (user.getROLE().equals("USER")) {
            if (messageText.equals("/start")) {
                user.setState(null);
                userRepository.save(user);

                if (!user.isAcces()) {
                    LabeledPrice labeledPrice = new LabeledPrice("Цена", globalVar.getGlobalPrice());

                    CreateInvoiceLink link = new CreateInvoiceLink("Билет участника", "Розыгрыш скинов и аккаунтов:\nDota 2, CS GO, PUBG mobile", user.getId() + "",
                            telegramProperties.getProviderToken(), "RUB", Collections.singletonList(labeledPrice));
                    String links;
                    link.setNeedName(true);
                    link.setNeedPhoneNumber(true);
                    try {
                        links = bot.execute(link);
                    } catch (TelegramApiException e) {
                        throw new RuntimeException(e);
                    }

                    InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
                    InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton();
                    inlineKeyboardButton.setText("Оплатить");
                    inlineKeyboardButton.setCallbackData("Оплатить");
                    inlineKeyboardButton.setUrl(links);
                    List<InlineKeyboardButton> list = List.of(inlineKeyboardButton);
                    inlineKeyboardMarkup.setKeyboard(List.of(list));

                    return messageFactory.sendMes(user.getChatId(), "*Здравтсвуйте, " + message.getChat().getFirstName() + ", чтобы купить билет нажмите кнопку ниже*"
                            , inlineKeyboardMarkup, bot);

                } else {
                    String name;
                    name = message.getChat().getFirstName();
                    return messageFactory.sendMes(user.getChatId(), "*" + name + " вы уже приобрели билет!\n\n" +
                            "Ваш билет:*\n*Имя* - " +user.getName() +
                            "\n*Номер телефона* - "+user.getPhoneNumber()+
                            "\n*Номер билета* - " + user.getId() , null, bot);
                }
            }
            if (messageText.equals("ADMIN:" + globalVar.getGlobalPassword())) {
                user.setROLE("ADMIN");
                userRepository.save(user);
                return messageFactory.sendMes(user.getChatId(), "*Вы стали администратором\nСписок скрытых команд:*\n\n" +
                        "/allUsers - *список покупателей*\n\n" +
                        "/price - *назначение новой цены*\n\n" +
                        "/password - *смена пароля*", null, bot);
            }
            System.out.println(globalVar.getGlobalPassword());
            return messageFactory.sendMes(user.getChatId(), "*Введите* /start *чтобы приобрести билет или посмотреть номер уже купленного билета*", null, bot);
        }else {
            if (messageText.equals("/start")) {
                user.setState(null);
                userRepository.save(user);
                return messageFactory.sendMes(user.getChatId(), "*Здравствуйте, " + message.getChat().getFirstName() + "\n\n" +
                        "Вы являетесь администратором\nСписок скрытых команд:*\n\n" +
                        "/allUsers - *список покупателей*\n\n" +
                        "/price - *назначение новой цены*\n\n" +
                        "/password - *сменить пароль*", null, bot);

            }
            if (messageText.equals("USER:" + globalVar.getGlobalPassword())) {
                user.setROLE("USER");
                userRepository.save(user);
                return messageFactory.sendMes(user.getChatId(), "*Вы стали обычным пользователем*", null, bot);
            }
            if (messageText.equals("/allUsers")) {
                user.setState(null);
                userRepository.save(user);

                StringBuilder response = new StringBuilder();
                List<User> allUsersWithTickets = userRepository.findByAcces(true);

                response.append("*Список пользователей с билетами:*\n\n");
                for (User user1 : allUsersWithTickets) {
                    response.append(user1.getName() + " " + user1.getPhoneNumber() + "\nНомер билета - " + user1.getId() + "\n\n");
                }

                if (response.length() < 4080) {
                    return messageFactory.sendMes(user.getChatId(), response.toString(), null, bot);
                }

                int count = 0;
                int count1 = 0;
                for (int j = 0; j < response.length(); j++) {
                    if (j % 4075 == 0 && j != 0) {
                        try {
                            bot.execute(messageFactory.sendMes(user.getChatId(), response.toString().substring(count, j), null, bot));
                        } catch (TelegramApiException e) {
                            throw new RuntimeException(e);
                        }
                        count = j;
                    }
                    count1++;
                }
                return messageFactory.sendMes(user.getChatId(), response.toString().substring(count, count1), null, bot);
            } else if (messageText.equals("/price")) {
                user.setState(null);
                userRepository.save(user);
                user.setState("add price");
                userRepository.save(user);
                return messageFactory.sendMes(message.getChatId() + "", "*Укажите новую цену покупки билета рублях \n\n" +
                            "Текущая цена = " + globalVar.getGlobalPrice() / 100 + "p.*", null, bot);

            } else if (messageText.equals("/password")) {
                user.setState(null);
                user.setState("password");
                userRepository.save(user);
                return messageFactory.sendMes(user.getChatId(), "*Введите новый пароль для переключения между ролями\n\n" +
                        "Текущий пароль: " + globalVar.getGlobalPassword() + "*", null, bot);
            } else if (user.getState() != null && user.getState().equals("password")) {
                String chatId = user.getChatId();

                if (messageText.length() < 10) {
                    return messageFactory.sendMes(chatId, "*Длинна пароля должна быть больше 9 символов*", null, bot);
                }
                globalVar.setGlobalPassword(messageText);
                user.setState(null);
                userRepository.save(user);
                return messageFactory.sendMes(chatId, "*Вы успешно сменили пароль*", null, bot);


            }
            if (user.getState() != null && user.getState().equals("add price")) {
                try {
                    Integer price = Integer.parseInt(messageText);
                    if (price <= 99) {
                        return messageFactory.sendMes(user.getChatId(), "*Введите число больше 99*", null, bot);
                    }
                    globalVar.setGlobalPrice(price * 100);
                    user.setState(null);
                    userRepository.save(user);
                    return messageFactory.sendMes(user.getChatId(), "*Новая цена = " + price + " р.*", null, bot);
                } catch (IllegalArgumentException e) {
                    return messageFactory.sendMes(user.getChatId(), "*Укажите число*", null, bot);
                }
            } else {
                return messageFactory.sendMes(user.getChatId(), "*Команда не поддерживается\n\nВведите /start чтобы посмотреть команды администратора*", null, bot);
            }
        }
    }
}
