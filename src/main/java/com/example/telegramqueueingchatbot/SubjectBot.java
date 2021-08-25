package com.example.telegramqueueingchatbot;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;

public class SubjectBot {

    private final Map<String, Integer> inQueue = new TreeMap<>();
    private final LinkedList<String> queue = new LinkedList<>();
    private final boolean[][] swapStatus = new boolean[200][200];
    private final String nameSubject;
    private final String shortName;

    public SubjectBot(String nameSubject, String shortName){
        this.nameSubject = nameSubject;
        this.shortName = shortName;
    }

    public boolean add(String targetID){
        return register(targetID);
    }

    public boolean remove(String targetID, int userChatID){
        if(inQueue.containsKey(targetID)) {
            queue.remove(targetID);
            inQueue.remove(targetID);
            Arrays.fill(swapStatus[userChatID], false);
            return true;
        }
        return false;
    }

    public void clear(){
        for (boolean[] booleans : swapStatus) {
            Arrays.fill(booleans, false);
        }
        inQueue.clear();
        queue.clear();
    }

    public boolean register(String userID){
        if(inQueue.containsKey(userID)){
            return false;
        } else{
            queue.add(userID);
            inQueue.put(userID, queue.size());
            return true;
        }
    }

    public boolean cancel(String userID){
        if(inQueue.containsKey(userID)){
            inQueue.remove(userID);
            queue.remove(userID);
            return true;
        } else {
            return false;
        }
    }

    public String show(Map<String, String> usernameByID){
        int cnt = 0;
        StringBuilder stringBuilder = new StringBuilder("текущая очередь " + nameSubject + ":\n");
        for( String id : queue ){
            ++cnt;
            stringBuilder.append(cnt).append(": ").append(usernameByID.get(id)).append("\n");
        }
        if(cnt == 0){
            return "Очередь пуста! Самое время записаться.";
        } else{
            return stringBuilder.toString();
        }
    }

    public boolean pushMeEnd(String userID){
        if(inQueue.containsKey(userID)){
            queue.remove(userID);
            queue.add(userID);
            return true;
        } else {
            return false;
        }
    }

    public String[] swap(int targetPosition, String userID, String displayName){
        String[] response = new String[3];
        if(!inQueue.containsKey(userID)) {
            response[0] = "You have not registered before. Write \"" + shortName + " register\" to be in queue";
            return response;
        }
        int myPosition = 0;
        for( String id : queue){
            ++myPosition;
            if( id.equals(userID)) break;
        }
        if(myPosition == targetPosition) {
            response[0] = "Hey! You just swapped with yourself";
            return response;
        }
        if(targetPosition > 0 && targetPosition <= queue.size()){
            swapStatus[myPosition][targetPosition] = true;
            String hisID = queue.get(targetPosition-1);
            response[2] = hisID;
            if(swapStatus[targetPosition][myPosition]){
                queue.set(targetPosition-1, userID);
                queue.set(myPosition-1, hisID);
                response[0] = "Вы успешно поменялись местами!";
                response[1] = "Вы успешно поменялись местами!";

                swapStatus[targetPosition][myPosition] = false;
                swapStatus[myPosition][targetPosition] = false;
            }
            else{
                response[0] = "Ожидается подтверждение! Уведоми человека на этом месте о своем предложении.";
                response[1] = displayName + " на позиции " + myPosition + " просит поменяться с тобой местами в предмете "
                        + nameSubject + "! Если ты согласен(на), напиши \"" + shortName + " swap with " + myPosition + " \" ";
            }
        } else{
            response[0] = "На этой позиции никого нет! Перепроверь список.";
        }
        return response;
    }
}
