package com.example.telegramqueueingchatbot;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.*;

public class Bot extends TelegramLongPollingBot {

    private final Map<String, Integer> subjectID = new HashMap<>();
    private final LinkedList<SubjectBot> subjectBots = new LinkedList<>();

    private final Map<String, String> usernameByID = new HashMap<>();
    private final Map<String, String> chatIdByUserID = new HashMap<>();
    private final Map<String, Integer> userChatID = new HashMap<>();

    private final String adminID1 = "822910469";
    private final String adminID2 = "490965657";


    private final String start = "Синтаксис команд:\n" +
            "\n" +
            "[предмет] [команда]\n" +
            "\n" +
            "Например, \"prog register\" или \"web show\"\n" +
            "\n" +
            "Доступные предметы:\n" +
            "\n" +
            "(*) prog\n" +
            "(*) web\n" +
            "(*) c\n" +
            "(*) opd\n" +
            "\n" +
            "Доступные команды:\n" +
            "\n" +
            "(*) register -- занять место в очереди на сдачу лабы\n" +
            "(*) cancel -- отказаться от места в очереди на сдачу\n" +
            "(*) show -- посмотреть текущую очередь\n" +
            "(*) push me end -- уйти в конец очереди\n" +
            "(*) swap with X -- поменяться местами с человеком на позиции X (потребует подтверждения этого человека)\n" +
            "\n" +
            "Напиши /help, чтобы вывести этот экран снова в будущем.\n" +
            "Напиши /start, чтобы вывести синтаксис снова в будущем.\n\n" +
            "Author: Nguyen Ngoc Duc - https://github.com/ndwannafly\n";

    private final String help = "Доступные команды:\n" +
            "\n" +
            "(*) \"[предмет] register\" -- занять место в очереди на сдачу лабы\n" +
            "(*) \"[предмет] cancel\" -- отказаться от места в очереди на сдачу\n" +
            "(*) \"[предмет] show\" -- посмотреть текущую очередь\n" +
            "(*) \"[предмет] push me end\" -- уйти в конец очереди\n" +
            "(*) \"[предмет] swap with X\" -- поменяться местами с человеком на позиции X (потребует подтверждения этого человека)\n" +
            "\n" +
            "Напиши /help, чтобы вывести этот экран снова в будущем.\n" +
            "Напиши /start, чтобы вывести синтаксис снова в будущем.\n";

    private final String admin_help = " admin get all : get info of all users\n\n" +
            "admin [subject] clear: clear [subject] queue\n\n" +
            "admin [subject] remove x: remove x-th position from [subject] queue\n\n" +
            "admin [subject] add userID: add user to [subject] queue by userID\n\n";
    @Override
    public String getBotUsername() {
        return "P3233_AvtoOchered_bot";
    }

    @Override
    public String getBotToken() {
        return "1982606218:AAHRyu-Os4aKMDueW2oY2pld2SXcbXs2kb4";
    }

    public void send(String text, String chatId){
        SendMessage sendMessage = new SendMessage();
        sendMessage.setText(text);
        sendMessage.setChatId(String.valueOf(chatId));
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onUpdateReceived(Update update) {
        if(update.hasMessage() && update.getMessage().hasText()){
            String messageText = update.getMessage().getText();
            String userID = update.getMessage().getFrom().getId().toString();
            String chatID = update.getMessage().getChatId().toString();
            String firstName = update.getMessage().getFrom().getFirstName();
            String lastName = update.getMessage().getFrom().getLastName();
            String displayName = firstName + ' ' + (lastName == null ? "" : lastName);
            if(! usernameByID.containsKey(userID)){
                usernameByID.put(userID, displayName);
                int id =userChatID.size() + 1;
                userChatID.put(userID, id);
                chatIdByUserID.put(userID, chatID);
            } else if(! usernameByID.get(userID).equals(displayName)){
                usernameByID.remove(userID);
                usernameByID.put(userID, displayName);
            }


            if(messageText.equals("/start")){
                send(start, chatID);
            }
            if(messageText.equals("/help")){
                send(help, chatID);
            }
            boolean isAdmin = userID.equals(adminID1) || userID.equals(adminID2);
            if((isAdmin) && messageText.equals("/help_admin")){
                send(admin_help, chatID);
                return;
            }

            subjectID.put("c", 0);
            subjectBots.add(new SubjectBot("Языки программирования", "c"));
            
            subjectID.put("web", 1);
            subjectBots.add(new SubjectBot("Веб-программирование", "web"));
            
            subjectID.put("prog", 2);
            subjectBots.add(new SubjectBot("Программирование", "prog"));
            
            subjectID.put("opd", 3);
            subjectBots.add(new SubjectBot("ОПД", "opd"));
            
            if(messageText.contains("admin")){
                
                if(isAdmin) {
                    try {

                        if (messageText.equals("admin get all")) {
                            StringBuilder stringBuilder = new StringBuilder();
                            for (Map.Entry<String, String> userInfo : usernameByID.entrySet()) {
                                stringBuilder.append(userInfo.getKey()).append(" ").append(userInfo.getValue()).append("\n");
                            }
                            send(stringBuilder.toString(), chatID);
                            return;
                        }

                        String subject = messageText.split(" ")[1];
                        int botID = subjectID.get(subject);
                        String command = messageText.split(" ")[2];
                        switch (command) {
                            case "clear":
                                subjectBots.get(botID).clear();
                                send(subject + " clear!", chatID);
                                break;
                            case "remove": {
                                String targetID = messageText.split(" ")[3];
                                if (subjectBots.get(botID).remove(targetID, userChatID.get(targetID)))
                                    send("User with userID " + targetID + " has been removed from " + subject + " queue!", chatID);
                                else send("Fail to remove! User is not in the queue yet!", chatID);
                                break;
                            }
                            case "add": {
                                String targetID = messageText.split(" ")[3];
                                if (!usernameByID.containsKey(targetID)) {
                                    send("Wrong id! Please check it carefully", chatID);
                                } else if (subjectBots.get(botID).add(targetID)) {
                                    send("Successfully! Write \"" + subject + " show\" to check the list", chatID);
                                } else send("Fail to add! User is already in the queue!", chatID);
                                break;
                            }
                        }
                    } catch (ArrayIndexOutOfBoundsException arrayIndexOutOfBoundsException) {
                        send("Wrong format!", chatID);
                        System.out.println("array out");
                    } catch (NullPointerException nullPointerException) {
                        send("Wrong format!", chatID);
                        System.out.println("null pointer");
                    }
                } else {
                    send("You are not admin!", chatID);
                }
            } else{
                int lastIndexOfSpace = messageText.lastIndexOf(' ');
                if(lastIndexOfSpace < 0) return;
                int firstIndexOfSpace = messageText.indexOf(' ');
                String subject = messageText.substring(0, firstIndexOfSpace);
                if(subjectID.get(subject) == null){
                    send("Wrong format!", chatID);
                } else {
                    int botID = subjectID.get(subject);
                    if(messageText.contains("register")){
                        if(subjectBots.get(botID).register(userID)){
                            send("Успешно! Воспользуйся командой show, чтобы увидеть список.", chatID);
                        } else{
                            send("Ты уже занял(а) место в очереди! Чтобы отказаться от места в очереди, воспользуйся командой cancel.", chatID);
                        }
                    } else if(messageText.contains("cancel")){
                        if(subjectBots.get(botID).cancel(userID)){
                            send("Ты был(а) убран(а) из очереди на предмет.", chatID);
                        } else{
                            send("Не волнуйся! Тебя и не было в очереди :)", chatID);
                        }
                    } else if(messageText.contains("show")){

                        String currentQueue = subjectBots.get(botID).show(usernameByID);
                        send(currentQueue, chatID);
                    } else if(messageText.contains("push me end")){
                        if(subjectBots.get(botID).pushMeEnd(userID)){
                            send("Ты был(а) отправлена в конец очереди.", chatID);
                        } else{
                            send("Тебя нет в очереди! Воспользуйся командой register, чтобы записаться.", chatID);
                        }
                    } else if(messageText.contains("swap with")){
                        String stringPosition = messageText.substring(lastIndexOfSpace + 1);
                        int targetPosition;
                        try {
                            targetPosition = Integer.parseInt(stringPosition);
                        } catch(NumberFormatException e){
                            e.printStackTrace();
                            send("Incorrect format! Write /help to see the guideline", chatID);
                            return;
                        }
                        String[] response = subjectBots.get(botID).swap(targetPosition, userID, displayName);
                        send(response[0], chatID);
                        if(response[1] != null){
                            send(response[1], chatIdByUserID.get(response[2]));
                            send(subjectBots.get(botID).show(usernameByID), chatIdByUserID.get(response[2]));
                        }
                    }
                }
            }
        }
    }
}

