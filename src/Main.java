import javax.xml.transform.Result;
import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Collections.reverseOrder;

public class Main {

    //STATIC VARIABLES//
    static Connection c = null;
    static Statement stmt = null;
    static String user = null;
    static ResultSet userSet = null;

    static String[] personalities = {"The Fool", "The Magician", "The High Priestess", "The Empress", "The Hierophant", "The Lovers", "The Chariot", "Justice", "The Hermit", "The Wheel of Fortune", "Strength", "The Hanged Man", "Death", "Temperance", "The Devil", "The Tower", "The Stars", "The Moon", "The Sun", "Judgement", "The World", "The Emperor"};

    //CONNECT WITH DB//
    public static void connectWithDB(){
        try{
            c = DriverManager.getConnection("jdbc:postgresql://localhost:5432/personality_quiz", "lucdi", "9200" );
            stmt = c.createStatement();

        }catch(SQLException e){
            e.printStackTrace();
        }
    }


    //LOG IN USER
    //takes user input and upserts into database
    //calls grabCurrentUser
    public static void logInUser(){
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter your Username to Log In or Sign Up.");
        user = scanner.nextLine();
        String sqlInsertUsername = "INSERT INTO users(username)" +
                "VALUES (?)" +
                "ON CONFLICT (username)"+
                "DO NOTHING";
        try{
            PreparedStatement ps = c.prepareStatement(sqlInsertUsername);
            ps.setString(1, user);
            ps.executeUpdate();
            userSet = grabCurrentUser();
        }catch(SQLException e){
            e.printStackTrace();
        }
    }

    //GRAB CURRENT USER
    //called by loginUser
    public static ResultSet grabCurrentUser(){
        final String QUERY_USER = "SELECT * FROM users WHERE username = (?)";
        try{
            PreparedStatement getUser = c.prepareStatement(QUERY_USER);
            getUser.setString(1, user);
            userSet = getUser.executeQuery();
            userSet.next();
        }catch(SQLException e){
            e.printStackTrace();
        }
        return userSet;
    }

    //PRINT QUESTIONS
    //loops through questions and their respective answers using JOIN
    //calls getAnswerFromUser after each question-answer map
    public static void printQuestions() {

        try {
            PreparedStatement psQuestions = c.prepareStatement("SELECT * FROM questions", ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            ResultSet questions = psQuestions.executeQuery();
            while(questions.next()){
                ResultSet questionsAndAnswers = null;
                PreparedStatement psQuestionAndAnswerQuery = null;
                psQuestionAndAnswerQuery = c.prepareStatement("SELECT questions.question_text, answers.answer_text, answers.question " +
                        "FROM answers " +
                        "INNER JOIN questions " +
                        "ON answers.question = questions.question_id " +
                        "WHERE answers.question = (?)"
                , ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
                System.out.println(questions.getString("question_text"));
                psQuestionAndAnswerQuery.setInt(1, questions.getInt("question_id"));
                questionsAndAnswers = psQuestionAndAnswerQuery.executeQuery();
                while(questionsAndAnswers.next()){
                    System.out.println(questionsAndAnswers.getString("answer_text"));
                }
                getAnswerFromUser(questionsAndAnswers, questions);
            }

        }catch(SQLException e){
            e.printStackTrace();
        }
    }

    //GET ANSWER FROM USER
    //called by printQuestions
    //processes user answer to compare to given answers of current question
    //error-handles invalid input
    //calls UpsertAnswerIntoUserAnswers if valid input
    // if not valid input, rewinds the cursor and leaves method to go back to printQuestions
    public static void getAnswerFromUser(ResultSet questionsAndAnswers, ResultSet questions){

        Scanner scanner = new Scanner(System.in);
        String userAnswerToProcess = scanner.nextLine();
        char userAnswer = userAnswerToProcess.isEmpty() ? '0' : userAnswerToProcess.toUpperCase().charAt(0);
        System.out.println(userAnswer);

        boolean found = false;
        boolean breakWhile = false;


        try{
            String answerStr = "";

            questionsAndAnswers.beforeFirst();
            while(questionsAndAnswers.next() && !breakWhile){
                answerStr = questionsAndAnswers.getString("answer_text");
                if(userAnswer == answerStr.charAt(0)){
                    found = true;
                    breakWhile = true;
                }
            }
            if(found){
                upsertAnswerIntoUserAnswers(answerStr);
            }else{
                System.out.println("No answer given or incorrect input.");
                questions.previous();
            }
        }catch(SQLException e){
            e.printStackTrace();
        }
    }

    // DELETE USER'S OLD QUESTIONS
    // if user decides to retake the quiz, all old questions are deleted
    public static void deleteUserOldQuestions(){
        try{
            stmt.executeUpdate("DELETE FROM user_answers WHERE user_id = " +  userSet.getInt("user_id"));
        }catch(SQLException e){
            e.printStackTrace();
        }

    }

    //UPSERT ANSWER INTO USER ANSWERS
    //if no restraint conflict on answer_id and user_id being the same, inserts user answer
    //NOTE: don't believe conflict can ever occur, because user answers are deleted before this method is reached
    public static void upsertAnswerIntoUserAnswers(String answerStr){
        ResultSet userAnswer = null;

        try{

            PreparedStatement psUserAnswer = c.prepareStatement("SELECT answer_id, answer_text FROM answers WHERE answer_text = (?)");
            psUserAnswer.setString(1, answerStr);
            userAnswer = psUserAnswer.executeQuery();
            final String UPSERT_STRING = "INSERT INTO user_answers(user_id, answer_id)" +
                    "VALUES(?, ?)" +
                    "ON CONFLICT (answer_id, user_id)" +
                    "DO "+
                    "UPDATE SET answer_id = (?)";
            PreparedStatement ps_upsert_user_answers = c.prepareStatement(UPSERT_STRING);
            userAnswer.next();
            System.out.println(userAnswer.getString("answer_text"));
            ps_upsert_user_answers.setInt(1, userSet.getInt("user_id"));
            ps_upsert_user_answers.setInt(2, userAnswer.getInt("answer_id"));
            ps_upsert_user_answers.setInt(3, userAnswer.getInt("answer_id"));
            ps_upsert_user_answers.executeUpdate();

        }catch(SQLException e){
            e.printStackTrace();
        }
    }

    //INITIALIZE USER MAP
    //calls printUserStats
    //maps all personalities that are joined on the questions to user
    //
    public static void initializeUserMap(){
        HashMap<String, Integer> userMapToPersonality = new HashMap<>();
        final String sql = "SELECT answer_personalities.answer_id, answer_personalities.personality_id, personalities.personality_text, users.username " +
                "FROM users " +
                "INNER JOIN user_answers " +
                "ON users.user_id = user_answers.user_id " +
                "INNER JOIN answer_personalities " +
                "ON user_answers.answer_id = answer_personalities.answer_id " +
                "INNER JOIN personalities " +
                "ON personalities.personality_id = answer_personalities.personality_id " +
                "WHERE user_answers.user_id = (?)";
        try{
            PreparedStatement ps = c.prepareStatement(sql);
            ps.setInt(1, userSet.getInt("user_id"));
            ResultSet userStats = ps.executeQuery();
            while(userStats.next()){
                String persToParse = userStats.getString("personality_text");
                userMapToPersonality.merge(persToParse, 1, Integer::sum);
            }
        }catch(SQLException e){
            e.printStackTrace();
        }
        printUserStats(userMapToPersonality);

    }

    //PRINT USER STATS
    //sorts stats and prints them from hashmap provided by initializeUserMap
    //called by initializeUserMap
    public static void printUserStats(HashMap<String, Integer> map){
        System.out.println("Here are your stats:");
        //Sort map using Streams
        LinkedHashMap<String, Integer> sortedMap = map.entrySet()
                .stream()
                .sorted(reverseOrder(Map.Entry.comparingByValue()))
                .collect(Collectors.toMap(Map.Entry::getKey,
                        Map.Entry::getValue,
                        (oldValue, newValue) -> oldValue, LinkedHashMap::new));

        //print results
        for(String p : sortedMap.keySet()){
            double personality_ratio = (double) (sortedMap.get(p) * 100) / personalities.length;
            System.out.printf("%s: %.0f%%%n", p, personality_ratio);
        }
    }

    //INPUT HELPER
    //
    public static char inputHelper(){
        Scanner scanner = new Scanner(System.in);
        System.out.println("Would you like to see your [P]ersonality stats, [R]etake the quiz, or [Q]uit and sign out?");
        char userAnswer = scanner.nextLine().toUpperCase().charAt(0);
        return userAnswer;
    }

    //MAIN
    public static void main(String[] args) {
        connectWithDB();
        logInUser();
        char userInput = inputHelper();
        while(userInput != 'Q'){
            if(userInput == 'P'){
                initializeUserMap();
            }
            else if (userInput == 'R'){
                deleteUserOldQuestions();
                printQuestions();
            }
            userInput = inputHelper();
        }
    }
}
