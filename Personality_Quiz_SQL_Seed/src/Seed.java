import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Seed {


    static void connectWithDatabase() {
        Connection c = null;
        Statement stmt = null;
        final String SQL_INSERT_QUESTIONS = "INSERT INTO questions (question_id, question_text) VALUES (?,?)";
        final String SQL_INSERT_ANSWERS = "INSERT INTO answers (answer_text, question) VALUES (?, ?)";
        final String SQL_INSERT_PERSONALITIES = "INSERT INTO personalities (personality_text, description) VALUES (?, ?)";
        final String SQL_INSERT_ANSWER_PERSONALITY = "INSERT INTO answer_personalities (answer_id, personality_id) VALUES (?, ?)";

        try {
            c = DriverManager.getConnection("jdbc:postgresql://localhost:5432/personality_quiz", "lucdi", "9200");
            stmt = c.createStatement();

            //PREPARE STATEMENTS FOR POPULATING QUESTIONS AND ANSWERS
            PreparedStatement preparedStatementQuestions = c.prepareStatement(SQL_INSERT_QUESTIONS);
            PreparedStatement preparedStatementAnswers = c.prepareStatement(SQL_INSERT_ANSWERS);

            //POPULATE QUESTIONS, INSERTING ID AND QUESTION_TEXT
            for (int i = 0; i < PersonalityQuiz.finalQuestions.size(); i++) {
                Question currentQuestion = PersonalityQuiz.finalQuestions.get(i);
                String insertQuestion = currentQuestion.getQuestion();
                preparedStatementQuestions.setInt(1, i);
                preparedStatementQuestions.setString(2, insertQuestion);
                preparedStatementQuestions.executeUpdate();

                ResultSet rs = stmt.executeQuery("SELECT question_id FROM questions WHERE question_id =" + i);
                //currently unused
                int questionIDforAnswerLoop = 0;
                while (rs.next()) {
                    questionIDforAnswerLoop = rs.getInt("question_id");
                }
                rs.close();


                //POPULATE ANSWERS, INSERTING ANSWER TEXT
                for (int j = 0; j < 3; j++) {
                    String insertAnswer = currentQuestion.getAnswers().get(j).getAnswer();
                    preparedStatementAnswers.setString(1, insertAnswer);
                    preparedStatementAnswers.setInt(2, i);
                    preparedStatementAnswers.executeUpdate();
                }
            }
            //POPULATE PERSONALITIES
            PreparedStatement psPersonality = c.prepareStatement(SQL_INSERT_PERSONALITIES);
            for (String entry : PersonalityQuiz.finalResultsCall.keySet()) {
                psPersonality.setString(1, entry);
                psPersonality.setString(2, PersonalityQuiz.finalResultsCall.get(entry));
                psPersonality.executeUpdate();
            }
            psPersonality.close();

            //POPULATE JUNCTION TABLE answer_personalities
            //TODO
            PreparedStatement queryAnswers = c.prepareStatement("SELECT * FROM answers WHERE answer_text = ?");
            PreparedStatement queryPersonalities = c.prepareStatement("SELECT * FROM personalities WHERE personality_text = ?");
            PreparedStatement psAnswerPersonality = c.prepareStatement(SQL_INSERT_ANSWER_PERSONALITY);
            for(Answer answer : PersonalityQuiz.allAnswers){
               queryAnswers.setString(1, answer.getAnswer());
               ResultSet answersFromSet = queryAnswers.executeQuery();
               while(answersFromSet.next()){
                   System.out.println(answersFromSet.getString("answer_text"));
                   for(String personality: answer.getCounters().keySet()){
                       queryPersonalities.setString(1, personality);
                       ResultSet personalitiesPerAnswer = queryPersonalities.executeQuery();
                       while(personalitiesPerAnswer.next()){
                           System.out.println(personalitiesPerAnswer.getString("personality_text"));
                           psAnswerPersonality.setInt(1, answersFromSet.getInt("answer_id"));
                           psAnswerPersonality.setInt(2, personalitiesPerAnswer.getInt("personality_id"));
                           psAnswerPersonality.executeUpdate();
                       }
                   }
               }
            }

            //SET TEST USER
            String sqlUser = "INSERT INTO user (username) VALUES (?)";
            PreparedStatement createTestUser = c.prepareStatement(sqlUser);
            createTestUser.setString(1, "testuser");
            createTestUser.executeUpdate();

            stmt.close();
            psPersonality.close();
            psAnswerPersonality.close();
            preparedStatementAnswers.close();
            preparedStatementQuestions.close();
            createTestUser.close();
            c.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public static void main(String[]args){
        PersonalityQuiz.initializeAnswersAndQuestions();
        PersonalityQuiz.initializeFinalResultCall();
        connectWithDatabase();
    }
}
