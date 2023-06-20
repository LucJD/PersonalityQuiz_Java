import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.Array;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
public class PersonalityQuiz {
    static String[] resultStr = {"The Fool", "The Magician", "The High Priestess", "The Empress", "The Emperor", "The Hierophant", "The Lovers", "The Chariot", "Justice", "The Hermit", "The Wheel of Fortune", "Strength", "The Hanged Man", "Death", "Temperance", "The Devil", "The Tower", "The Stars", "The Moon", "The Sun", "Judgement", "The World"};

    static ArrayList<Answer> allAnswers = new ArrayList<>();
    static ArrayList<Question> finalQuestions = new ArrayList<>(); //final fully initialized questions to print out
    static HashMap<String, Integer> finalResults = new HashMap<String, Integer>(); // final personalities mapped to score
    static HashMap<String, String> finalResultsCall = new HashMap<String, String>(); //final text output


    //COMMUNICATING WITH DATABASE//
    static void connectWithDatabase(){
        Connection c = null;
        Statement stmt = null;

        try {
            c = DriverManager.getConnection("jdbc:postgresql://localhost:5432/personality_quiz", "lucdi", "9200");
            c.setAutoCommit(false);
            System.out.println("Opened database successfully");

            stmt = c.createStatement();
        }catch(SQLException e){
            e.printStackTrace();
        }
    }

    static void initializeAnswersAndQuestions() {
        ArrayList<Answer> answersToCopy = new ArrayList<>();
        ArrayList<Question> questionsToCopy = new ArrayList<>();
        ArrayList<String> personalitiesToCopy = new ArrayList<>();
        ArrayList<Integer> personalityCountersToCopy = new ArrayList<>();
        try {

            File file = new File("PersonalityQuizQuestionsandAnswers.txt");
            Scanner myRead = new Scanner(file);
            while (myRead.hasNextLine()) { //till end of file
                String questionData = myRead.nextLine();
                for (int i = 0; i < 3; i++) { //till end of answer line
                    String answerData = myRead.nextLine();
                    for (int j = 0; j < 3; j++) { //till end of personality and counter
                        String personalityData = myRead.nextLine();
                        Integer personalityCount = Integer.parseInt(myRead.nextLine());
                        personalitiesToCopy.add(personalityData);
                        personalityCountersToCopy.add(personalityCount);
                    }
                    ArrayList<String> personalityStrTemp = new ArrayList<>(personalitiesToCopy);
                    ArrayList<Integer> personalityCounterTemp = new ArrayList<>(personalityCountersToCopy);
                    Answer answer = new Answer(personalityStrTemp, personalityCounterTemp, answerData);
                    answersToCopy.add(answer);
                    personalitiesToCopy.clear();
                    personalityCountersToCopy.clear();
                }
                ArrayList<Answer> answersTemp = new ArrayList<>(answersToCopy);
                Question question = new Question(questionData, answersTemp);
                finalQuestions.add(question);
                allAnswers.addAll(answersTemp);
                answersToCopy.clear();

            }
            myRead.close();
        } catch (FileNotFoundException e) {
            System.out.println("This file could not be found.");
            e.printStackTrace();
        }


    }

    static void initializeFinalResults() {
        for (String result : resultStr) {
            finalResults.put(result, 0);
        }
    }

    static void initializeFinalResultCall(){
        try {
            File file = new File("FinalResultsCall.txt");
            Scanner myRead = new Scanner(file);
            while(myRead.hasNextLine()){
                String resultTitle = myRead.nextLine();
                finalResultsCall.put(resultTitle, myRead.nextLine());
            }
            myRead.close();
        }catch(FileNotFoundException e){
            System.out.println("File could not be found.");
            e.printStackTrace();
        }
    }

    static void printQuestions() {
        for (Question question : finalQuestions) {
            System.out.println(question.getQuestion());
            for (Answer answer : question.getAnswers()) {
                System.out.println(answer.getAnswer());
            }
            getAnswerFromUserAndParse(question);
        }
    }

    static void getAnswerFromUserAndParse(Question question) {
        Scanner scanner = new Scanner(System.in);
        boolean answered = false;
        do {
            System.out.print("> ");
            char userInput = scanner.nextLine().toLowerCase().charAt(0);
            switch (userInput) {
                case 'a':
                    parseAnswerIntoFinalResult(question.getAnswers().get(0));
                    answered = true;
                    break;
                case 'b':
                    parseAnswerIntoFinalResult((question.getAnswers().get(1)));
                    answered = true;
                    break;
                case 'c':
                    parseAnswerIntoFinalResult(question.getAnswers().get(2));
                    answered = true;
                    break;
                default:
                    System.out.println("Invalid Response.");
                    break;
            }
        } while (!answered);

    }

    static String calculateResults(){
        String maxResultStr = "The Fool";
        int maxResult = finalResults.get(maxResultStr);
        for(String tempResult : finalResults.keySet()){
            if (finalResults.get(tempResult) > finalResults.get(maxResultStr)){
                maxResultStr = tempResult;
            }
            maxResult = finalResults.get(maxResultStr);
        }
        return maxResultStr;
    }

    static void parseAnswerIntoFinalResult(Answer answer) {
        for (String personality : answer.getCounters().keySet()) {
            finalResults.put(personality, answer.getCounters().get(personality));
        }
    }


    public static void main(String[] args) {
        connectWithDatabase();
        initializeAnswersAndQuestions();
        initializeFinalResults();
        initializeFinalResultCall();

        printQuestions();

        String userResult = calculateResults();
        System.out.println(finalResultsCall.get(userResult));
    }
}
