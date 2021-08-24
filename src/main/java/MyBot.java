import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.*;

public class MyBot extends TelegramLongPollingBot {

    private final Map<String, Integer> inQueueWeb = new TreeMap<>();
    private final Map<String, Integer> inQueueProg = new TreeMap<>();

    private final LinkedList<String> queueWeb = new LinkedList<>();
    private final LinkedList<String> queueProg = new LinkedList<>();

    private final Map<String, String> usernameByID = new HashMap<>();

    private final Map<String, String> chatIdByUserID = new HashMap<>();
    private final Map<String, Integer> userChatID = new HashMap<>();

    private final boolean[][] swapStatusWeb = new boolean[200][200];
    private final boolean[][] swapStatusProg = new boolean[200][200];

    private final String adminID = "822910469";

    private final String help = "Write \"/help\" to see the guideline\n\n" +
            "Write \"web register\" or \"prog register\" to register for the upcoming practice lesson.\n\n" +
            "Write \"web cancel\" or \"prog cancel\" to cancel your turn.\n\n" +
            "Write \"web show\" to take a look at the current WEB queue\n\n" +
            "Write \"prog show\" to take a look at the current prog " +
            "queue\n\n" +
            "Write \"web push me end\" or \"prog push me end\" to push yourself at the end of the queue.\n\n" +
            "Write \"web swap with x\" or \"prog swap with x\" (x is position with whom you want to swap)\n\n";

    private final String admin_help = " admin get all : get info of all users\n\n" +
            "admin web clear: clear web queue\n\n" +
            "admin prog clear: clear prog queue\n\n" +
            "admin web remove x: remove x-th position from web queue\n\n" +
            "admin prog remove x: remove x-th position from prog queue\n\n" +
            "admin web add userID: add user to web queue by userID\n\n" +
            "admin prog add userID: add user to prog queue by userID\n\n";
    @Override
    public String getBotUsername() {
        return "P3233_AvtoOchered_bot";
    }

    @Override
    public String getBotToken() {
        return "secret";
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
                System.out.println(displayName + ' ' + id);
            } else if(! usernameByID.get(userID).equals(displayName)){
/*                int oldID = chatIdByUsername.get(usernameByID.get(userID));
                chatIdByUsername.remove(usernameByID.get(userID));
                chatIdByUsername.put(displayName, oldID);
                System.out.println("fix name -> id in chat: \n" + displayName + ' ' + oldID);*/
                usernameByID.remove(userID);
                usernameByID.put(userID, displayName);

            }



            if(messageText.equals("/start")){
                send(help, chatID);
            }
            if(messageText.equals("/help")){
                send(help, chatID);
            }
            if(userID.equals(adminID) && messageText.equals("/help_admin")){
                send(admin_help, chatID);
            }
            if(messageText.equals("web register")){
                if(inQueueWeb.containsKey(userID)){
                    send("You are already registered! If you want to cancel your turn, " +
                            "write \"web cancel\" or \"prog cancel\"", chatID);
                } else{
                    send("Successfully! Write \"web show\" to check the list", chatID);
                    queueWeb.add(userID);
                    inQueueWeb.put(userID, queueWeb.size());
                }
            }

            if(messageText.equals("prog register")){
                if(inQueueProg.containsKey(userID)){
                    send("You are already registered! If you want to cancel your turn, " +
                            "write \"web cancel\" or \"prog cancel\"", chatID);
                } else{
                    send("Successfully! Write \"prog show\" to check the list", chatID);
                    queueProg.add(userID);
                    inQueueProg.put(userID, queueProg.size());
                }
            }

            if(messageText.equals("web cancel")){
                if(inQueueWeb.containsKey(userID)){
                    inQueueWeb.remove(userID);
                    queueWeb.remove(userID);
                    send("You've been removed from the queue of WEB", chatID);
                } else {
                    send("Don't worry! You have not registered before", chatID);
                }
            }

            if(messageText.equals("prog cancel")){
                if(inQueueProg.containsKey(userID)){
                    inQueueProg.remove(userID);
                    queueProg.remove(userID);
                    send("You've been removed from the queue of prog", chatID);

                } else {
                    send("Don't worry! You have not registered before", chatID);
                }
            }

            if(messageText.equals("web show")){
                int cnt = 0;
                StringBuilder stringBuilder = new StringBuilder();
                for( String id : queueWeb ){
                    ++cnt;
                    stringBuilder.append(cnt).append(": ").append(usernameByID.get(id)).append("\n");
                }
                if(cnt == 0){
                    send("Queue for prog is empty! Let's register", chatID);
                } else{
                    send(stringBuilder.toString(), chatID);
                }
            }
            if(messageText.equals("prog show")){
                int cnt = 0;
                StringBuilder stringBuilder = new StringBuilder();
                for( String id : queueProg ){
                    ++cnt;
                    stringBuilder.append(cnt).append(": ").append(usernameByID.get(id)).append("\n");
                }
                if(cnt == 0){
                    send("Queue for prog is empty! Let's register", chatID);
                } else{
                    send(stringBuilder.toString(), chatID);
                }
            }

            if(messageText.equals("web push me end")){
                if(inQueueWeb.containsKey(userID)){
                    queueWeb.remove(userID);
                    queueWeb.add(userID);
                    send("You've been pushed to the end of WEB queue", chatID);
                } else {
                    send("You have not registered before. Write \"web register\" to be in queue", chatID);
                }
            }

            if(messageText.equals("prog push me end")){
                if(inQueueProg.containsKey(userID)){
                    queueProg.remove(userID);
                    queueProg.add(userID);
                    send("You've been pushed to the end of WEB queue", chatID);
                } else {
                    send("You have not registered before. Write \"prog register\" to be in queue", chatID);
                }
            }

            if(messageText.contains("swap")) {


                int lastIndexOfSpace = messageText.lastIndexOf(' ');
                int firstIndexOfSpace = messageText.indexOf(' ');
                String subject = messageText.substring(0, firstIndexOfSpace);

                if (lastIndexOfSpace >= 0) {
                    String swapCommand = messageText.substring(firstIndexOfSpace + 1, lastIndexOfSpace);
                    String position = messageText.substring(lastIndexOfSpace + 1, messageText.length());
                    int pos = 0;
                    try {
                        pos = Integer.parseInt(position);
                    } catch(NumberFormatException e){
                        e.printStackTrace();
                        return ;
                    }
                    if (subject.equals("web")) {
                        int me = 0;
                        for( String id : queueWeb){
                            ++me;
                            if( id.equals(userID)) break;
                        }
                        if(me == 0) {
                            send("You have not registered before. Write \"web register\" to be in queue", chatID);
                            return ;
                        }
                        if(pos > 0 && pos <= queueWeb.size()){
                            swapStatusWeb[me][pos] = true;
                            String hisID = queueWeb.get(pos-1);
                            if(swapStatusWeb[pos][me]){
                                queueWeb.set(pos-1, userID);
                                queueWeb.set(me-1, hisID);
                                send("Swap successfully!", chatID);
                                send("Swap successfully!", chatIdByUserID.get(hisID));
                                swapStatusWeb[pos][me] = false;
                                swapStatusWeb[me][pos] = false;
                            }
                            else{
                                send("Waiting for her/him acceptance! Tell her/him about that!", chatID);
                                send(displayName + " at position " + me + " is asking you for swapping in WEB! If you agree, please write \"web swap with " + me + " \" ", chatIdByUserID.get(hisID));
                            }
                        } else{
                            send("There is nobody at this position! Please check the list again!", chatID);
                        }
                    } else if(subject.equals("prog")) {
                        int me = 0;
                        for( String id : queueProg){
                            ++me;
                            if( id.equals(userID)) break;
                        }
                        if(me == 0) {
                            send("You have not registered before. Write \"web register\" to be in queue", chatID);
                            return ;
                        }
                        if (pos > 0 && pos <= queueProg.size()) {
                            System.out.println("him by alias id " + pos);
                            swapStatusProg[me][pos] = true;
                            String hisID = queueProg.get(pos - 1);
                            if (swapStatusProg[pos][me]) {
                                queueProg.set(pos - 1, userID);
                                queueProg.set(me - 1, hisID);
                                swapStatusProg[pos][me] = false;
                                swapStatusProg[me][pos] = false;
                                send("Swap successfully!", chatID);
                                send("Swap successfully", chatIdByUserID.get(hisID));
                            } else {
                                send("Waiting for her/him acceptance! Tell her/him about that!", chatID);
                                send(displayName + " at position " + me + " is asking you for swapping in prog! If you agree, please write \"web swap with " + me + " \" ", chatIdByUserID.get(hisID));
                            }
                        } else {
                            send("There is nobody at this position! Please check the list again!", chatID);
                        }
                    }
                }
            }


            // admin feature
            if(userID.equals(adminID)){
                int firstIndexOfSpace = messageText.indexOf(' ');
                if(firstIndexOfSpace < 0) return;
                String role = messageText.substring(0, firstIndexOfSpace);
                if(role.equals("admin")){
                    // clear : clear the queue
                    // web remove x : remove x-th position from the WEB queue
                    // prog remove x: remove x-th position from the prog queue
                    // web add userID: add userID to the web queue
                    // prog add userID: add userID prog queue
                    // all userID: get all userID in the room

                    // clear : clear the queue
                    if(messageText.contains("web clear")){
                        for (boolean[] booleans : swapStatusWeb) {
                            Arrays.fill(booleans, false);
                        }
                        inQueueWeb.clear();
                        queueWeb.clear();
                        send("Web clear!", chatID);
                    }
                    if(messageText.contains("prog clear")){
                        for (boolean[] booleans : swapStatusProg) {
                            Arrays.fill(booleans, false);
                        }
                        inQueueProg.clear();
                        queueProg.clear();
                        send("prog clear!", chatID);
                    }

                    // web remove x
                    if(messageText.contains("web remove")){
                        int lastIndexOfSpace = messageText.lastIndexOf(' ');
                        if(lastIndexOfSpace < 0) return;

                        String position = messageText.substring(lastIndexOfSpace + 1, messageText.length());
                        int pos = 0;
                        try {
                            pos = Integer.parseInt(position);
                        } catch(NumberFormatException e){
                            System.out.println("return");
                            return ;
                        }
                        if(pos > 0 && pos <= queueWeb.size()){
                            String hisUserID = queueWeb.get(pos-1);
                            queueWeb.remove(hisUserID);
                            System.out.println("removed " + hisUserID);
                            inQueueWeb.remove(hisUserID);
                            int hisUserChatId = userChatID.get(hisUserID);
                            Arrays.fill(swapStatusWeb[hisUserChatId], false);
                            send("User at position " + pos + " has been removed from WEB queue!", chatID);
                        } else {
                            send("There is nobody at this position! Please check the list again!", chatID);
                        }
                    } else if( messageText.contains("prog remove")){
                        int lastIndexOfSpace = messageText.lastIndexOf(' ');
                        if(lastIndexOfSpace < 0) return;

                        String position = messageText.substring(lastIndexOfSpace + 1, messageText.length());
                        int pos = 0;
                        try {
                            pos = Integer.parseInt(position);
                        } catch(NumberFormatException e){
                            System.out.println("return");
                            return ;
                        }

                        if(pos > 0 && pos <= queueProg.size()){
                            String hisUserID = queueProg.get(pos-1);
                            queueProg.remove(hisUserID);
                            System.out.println("removed " + hisUserID);
                            inQueueProg.remove(hisUserID);
                            int hisUserChatId = userChatID.get(hisUserID);
                            Arrays.fill(swapStatusProg[hisUserChatId], false);
                            send("User at position " + pos + " has been removed from prog queue!", chatID);
                        } else {
                            send("There is nobody at this position! Please check the list again!", chatID);
                        }
                    }

                    // get all UserId
                    if(messageText.contains("get all")){
                        for( Map.Entry<String, String> userInfo : usernameByID.entrySet()){
                            send(userInfo.getKey() + " " + userInfo.getValue(), chatID);
                        }
                    }

                    // add user by id
                    if(messageText.contains("add") && messageText.contains("web")){
                        int lastIndexOfSpace = messageText.lastIndexOf(' ');
                        if(lastIndexOfSpace < 0) return;

                        String hisID = messageText.substring(lastIndexOfSpace + 1, messageText.length());
                        if(!usernameByID.containsKey(hisID)){
                            send("Wrong id! Please check it carefully", chatID);
                            return;
                        }
                        if(!inQueueWeb.containsKey(hisID)){
                            queueWeb.add(hisID);
                            inQueueWeb.put(hisID, queueWeb.size());
                            send("Successfully! Write \"web show\" to check the list", chatID);
                        } else {
                            send("User is already in the queue!", chatID);
                        }
                    }
                    if(messageText.contains("add") && messageText.contains("prog")){
                        int lastIndexOfSpace = messageText.lastIndexOf(' ');
                        if(lastIndexOfSpace < 0) return;

                        String hisID = messageText.substring(lastIndexOfSpace + 1, messageText.length());
                        if(!usernameByID.containsKey(hisID)){
                            send("Wrong id! Please check it carefully", chatID);
                            return;
                        }
                        if(!inQueueProg.containsKey(hisID)){
                            queueWeb.add(hisID);
                            inQueueProg.put(hisID, queueProg.size());
                            send("Successfully! Write \"prog show\" to check the list", chatID);
                        } else{
                            send("User is already in the queue!", chatID);
                        }
                    }
                }
            }
        }
    }
}
