import java.util.ArrayList;

public class Question {
    public String question;
    public ArrayList<Answer> answers = new ArrayList<Answer>();

    Question(String question, ArrayList<Answer> answers){
        this.question = question;
        this.answers = answers;
    }
    //no setters, attributes should be unchanged after initialization
    public String getQuestion() {
        return question;
    }

    public ArrayList<Answer> getAnswers() {
        return answers;
    }
}
