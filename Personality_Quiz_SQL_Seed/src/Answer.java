import java.util.ArrayList;
import java.util.HashMap;

public class Answer {
    public String answer;
    public HashMap<String, Integer> counters = new HashMap<>();

    Answer(ArrayList<String> personalities, ArrayList<Integer> personalityCounters, String answertxt){
        this.answer = answertxt;
        for(int i = 0; i < 3; i++){
            counters.put(personalities.get(i), personalityCounters.get(i));
        }
    }
//no setters, variables should be unchanged after initialization
    public String getAnswer() {
        return answer;
    }

    public HashMap<String, Integer> getCounters() {
        return counters;
    }


}
