package ru.smyshlyaev.telegrambot.ZSKHelper.Controllers;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.smyshlyaev.telegrambot.ZSKHelper.Configuration.BotConfig;
import ru.smyshlyaev.telegrambot.ZSKHelper.Buttons.BotCommands;
import ru.smyshlyaev.telegrambot.ZSKHelper.Buttons.Buttons;
import ru.smyshlyaev.telegrambot.ZSKHelper.Models.Message;
import ru.smyshlyaev.telegrambot.ZSKHelper.Services.MessageService;
import ru.smyshlyaev.telegrambot.ZSKHelper.Services.PeopleService;
import ru.smyshlyaev.telegrambot.ZSKHelper.Utils.MessageSenderException;
import ru.smyshlyaev.telegrambot.ZSKHelper.Utils.RegistrationException;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Optional;

import org.modelmapper.ModelMapper;

@Component
@Controller
public class SenderTelegramBot extends TelegramLongPollingBot implements BotCommands {

    private static final Logger LOGGER=Logger.getLogger(SenderTelegramBot.class);

    private final PeopleService peopleService;
    private final MessageService messageService;
    private final BotConfig config;
    private final ModelMapper modelMapper;

    @Autowired
    public SenderTelegramBot(PeopleService peopleService, MessageService messageService, BotConfig config, ModelMapper modelMapper) {

        this.peopleService = peopleService;
        this.messageService = messageService;
        this.config = config;
        this.modelMapper = modelMapper;
        try {
            this.execute(new SetMyCommands(LIST_OF_COMMANDS, new BotCommandScopeDefault(), null));
        } catch (TelegramApiException e){
            LOGGER.error(e.getMessage());
        }
    }

    @Override
    public String getBotUsername() {
        return config.getBotName();
    }

    @Override
    public String getBotToken() {
        return config.getBotToken();
    }

    @Override
    public void onUpdateReceived(@NotNull Update update) {
        long chatId = 0;
        long userId = 0; //это нам понадобится позже
        String fullName = null;
        String userName = null;
        String receivedMessage;

        //если получено сообщение текстом
        if(update.hasMessage()) {
            chatId = update.getMessage().getChatId();
            userId = update.getMessage().getFrom().getId();
            fullName = update.getMessage().getFrom().getFirstName();
            userName=update.getMessage().getFrom().getUserName();

            if (update.getMessage().hasText()) {
                receivedMessage = update.getMessage().getText();
                botAnswerUtils(receivedMessage, chatId, fullName, userId ,userName);
            }

            //если нажата одна из кнопок бота
        } else if (update.hasCallbackQuery()) {
            chatId = update.getCallbackQuery().getMessage().getChatId();
            userId = update.getCallbackQuery().getFrom().getId();
            fullName = update.getCallbackQuery().getFrom().getFirstName();
            userName=update.getCallbackQuery().getFrom().getUserName();
            receivedMessage = update.getCallbackQuery().getData();
            botAnswerUtils(receivedMessage, chatId, fullName,userId,userName);

        }
    }

    @GetMapping("/message/response")
    public String getResponse(@ModelAttribute String getMessage,
                              @ModelAttribute long id) throws MessageSenderException {
       String responseMessage= modelMapper.map(getMessage,String.class);
       messageService.updateMessage(responseMessage,id);
       return responseMessage;

    }

    private void botAnswerUtils(String receivedMessage, long chatId, String fullName,long userId,String userName) {
        switch (receivedMessage){
            case "/start":
                startBot(chatId,userId,fullName, userName);
                break;
            case "/help":
                sendHelpText(chatId, HELP_TEXT);
                break;
           case "/registration":
                try{
                    peopleService.saveUser(userId,fullName,userName);
                    sendHelpText(chatId,REGISTRATION_TEXT);}

                catch(RegistrationException e){
                    sendHelpText(chatId,e.getMessage());}
                break;
            case "/my applications":
                List<Message> messages=peopleService.findMessageTelegramId(userId);
                if(messages.isEmpty()) {
                    sendHelpText(chatId,APP_TEXT);
                }
                else {
                    for (Message message: messages) {
                        String sendingMessage="Сообщение " +"'"+message.getMessage() +"'"+" дата отправки: " + message.getCreatedAt()
                                + "статус " + message.getStatus();
                        sendHelpText(chatId,sendingMessage);
                        }
                    }
                break;

            default:
                try{
                    if(peopleService.findOneUser(userId).isPresent()) {
                        messageService.saveMessage(receivedMessage,userId);
                            sendHelpText(chatId,SENDER_TEXT);}
                }
                catch (MessageSenderException e) {
                    sendHelpText(chatId,e.getMessage());}
                    break;
            }
    }

    private void startBot(long chatId,long userId, String fullName,String userName) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("Привет, " + fullName + "! Добро пожаловать в мой телеграмм бот. Нажми на интересующую кнопку");
        message.setReplyMarkup(Buttons.inlineMarkup());

        try {
            execute(message);
            LOGGER.info("Reply sent");
        } catch (TelegramApiException e){
            LOGGER.error(e.getMessage());
        }
    }

    private void sendHelpText(long chatId, String textToSend){
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(textToSend);

        try {
            execute(message);
            LOGGER.info("Reply sent");
        } catch (TelegramApiException e){
            LOGGER.error(e.getMessage());
        }
    }

}


