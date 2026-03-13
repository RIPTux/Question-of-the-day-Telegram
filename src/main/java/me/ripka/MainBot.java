package me.ripka;


import me.ripka.config.Config;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatAdministrators;
import org.telegram.telegrambots.meta.api.methods.groupadministration.LeaveChat;
import org.telegram.telegrambots.meta.api.methods.pinnedmessages.PinChatMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMember;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import me.ripka.database.*;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MainBot extends TelegramLongPollingBot {
    private final Config config;
    private final LogAdminCommand logAdminCommand;
    private final GroupWhiteList groupWhiteList;
    private final TimeLogDataBase timeLogDataBase;
    private final QuestionDataBase questionDataBase;

    public MainBot(Config config) {
        this.config = config;


        this.groupWhiteList = new GroupWhiteList();
        this.groupWhiteList.createGroupWhiteListDataBase();

        this.logAdminCommand = new LogAdminCommand();
        this.logAdminCommand.createLogAdminDataBase();

        this.timeLogDataBase = new TimeLogDataBase();
        this.timeLogDataBase.createTimeLogDataBase();

        this.questionDataBase = new QuestionDataBase();
        this.questionDataBase.createQuestionDataBase();

        schelduled();
    }

    @Override
    public String getBotUsername() {
        return config.getString("bot.username");
    }

    @Override
    public String getBotToken() {
        return config.getString("bot.token");
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMyChatMember()) {
            ChatMemberUpdated cmu = update.getMyChatMember();
            Chat chat = cmu.getChat();

            String newStatus = cmu.getNewChatMember().getStatus();

            if ("member".equals(newStatus) || "administrator".equals(newStatus)) {
                if (!groupWhiteList.isGroupExists(chat.getId())) {
                    sendMessage(chat.getId(), "Данного чата нет в моем вайт листе!");
                    sendMessage(chat.getId(), "В случае если это ошибка напишите @TorvaldsUniX!");

                    LeaveChat leaveChat = new LeaveChat();
                    leaveChat.setChatId(chat.getId());

                    logAdminCommand.addLog("addToGroup", chat.getInviteLink(), chat.getId(), "denied");
                    try {
                        execute(leaveChat);
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }
                } else {
                    logAdminCommand.addLog("addToGroup", chat.getInviteLink(), chat.getId(), "allowed");
                    sendMessage(chat.getId(), config.getString("message.firstChatMessage"));
                }
            }
            return;
        }

        if (update.hasMessage() && update.getMessage().hasText()) {
            Message mess = update.getMessage();
            Chat chat = mess.getChat();

            User user = update.getMessage().getFrom();

            Long userId = user.getId();
            String firstName = user.getFirstName();
            String username = user.getUserName();


            String displayName = (username != null) ? "@" + username : firstName;

            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();
            boolean owner = vereficationOwner(userId);

            if (messageText.equals("/start") && messageText.equals("/start@SparkleMabelBot")) {
                if (Long.parseLong(config.getString("bot.owner")) == userId) {
                    sendMessage(chat.getId(), config.getString("message.startAdminMessage"));
                } else {
                    sendMessage(chat.getId(), config.getString("message.startMessage"));
                }
            } else if(messageText.startsWith("/question") || messageText.startsWith("+опрос")) {
                if (verevicationGroupOwner(userId, chat.getId()) && userId != chat.getId() || owner && userId != chat.getId()) {
                    timeLogDataBase.questionSend(chat.getId(), System.currentTimeMillis());
                    sendAndPinMessage(chat.getId(), "*Вопрос дня:*\n `" + questionDataBase.getRandomQuestion() + "`");
                }
            }


            else if (messageText.startsWith("/add ") && owner) {
                messageText = messageText.substring(5);

                sendMessage(chat.getId(), groupWhiteList.addGroup(Long.parseLong(messageText)));
            } else if(messageText.startsWith("/getGroup") && owner) {
                sendMessage(chatId, groupWhiteList.getGroupWhiteList().toString());
            } else if(messageText.startsWith("/getQuestion") && owner) {
                sendMessage(chatId, questionDataBase.getQuestion().toString());
            } else if(messageText.startsWith("/delGroup ") && owner) {
                messageText = messageText.substring(10);
                groupWhiteList.deleteGroup(Long.parseLong(messageText));
                sendMessage(user.getId(), "Группа деактивирована");
            } else if(messageText.startsWith("/configReload") && owner) {
                config.configReload();
                sendMessage(chat.getId(), "Конфиг перезагружен!");
            } else if(messageText.startsWith("/getTime") && owner) {
                sendMessage(chat.getId(), timeLogDataBase.getGroupTime().toString());
            }
            else { }
        }
    }

    public void schelduled() {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

        scheduler.scheduleAtFixedRate(() -> {
                HashMap<Long, Long> groups = timeLogDataBase.checkQuestionTime();

                if (groups == null) return;

                for (Long groupId : groups.keySet()) {
                    sendAndPinMessage(groupId, "*Вопрос дня:*\n\n `" + questionDataBase.getRandomQuestion() + "`");
                    timeLogDataBase.questionSend(groupId, System.currentTimeMillis());
                }
            }, 0, 5, TimeUnit.MINUTES);
    }

    private boolean vereficationOwner(long userId) {
        String nnnn = config.getString("bot.owner");

        if (userId == Long.parseLong(nnnn)) {
            return true;
        }
        return false;
    }

    private void sendMessage(long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(text);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public boolean verevicationGroupOwner(long userId, long chatId) {
        try {
            GetChatAdministrators getAdmins = new GetChatAdministrators();
            getAdmins.setChatId(chatId);

            List<ChatMember> admins = execute(getAdmins);
            long ownerId = 0;
            for (ChatMember member : admins) {
                if ("creator".equals(member.getStatus())) {
                    ownerId = member.getUser().getId();
                }
            }

            if (ownerId == userId && ownerId != 0 ) {
                return true;
            } else {
                return false;
            }
        } catch (TelegramApiException e) {
            System.err.println("Ошибка получения овнера: " + e);
            return false;
        }
    }

    private void sendAndPinMessage(long chatId, String text) {
        if (!groupWhiteList.isGroupExists(chatId)) {
            sendMessage(chatId, "Данного чата нет в моем вайт листе!");
            sendMessage(chatId, "В случае если это ошибка напишите @TorvaldsUniX!");

            LeaveChat leaveChat = new LeaveChat();
            leaveChat.setChatId(chatId);

            logAdminCommand.addLog("groupCheckNotWhiteList", null, chatId, "denied");
            try {
                execute(leaveChat);
                timeLogDataBase.deleteGroup(chatId);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        } else {
            SendMessage message = new SendMessage();
            message.setChatId(String.valueOf(chatId));
            message.setText(text);
            message.setParseMode("MarkdownV2");

            try {
                Message sentMessage = execute(message);

                PinChatMessage pin = new PinChatMessage();
                pin.setChatId(chatId);
                pin.setMessageId(sentMessage.getMessageId());
                execute(pin);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
    }
}