

--QUESTIONS
CREATE TABLE questions (
    question_id SERIAL PRIMARY KEY,
    question_text TEXT NOT NULL
);

--ANSWERS
CREATE TABLE answers (
    answer_id SERIAL PRIMARY KEY,
    answer_text TEXT NOT NULL,
    question SERIAL NOT NULL REFERENCES questions(question_id)
);

--PERSONALITIES
CREATE TABLE personalities (
    personality_id SERIAL PRIMARY KEY,
    personality_text VARCHAR(40) NOT NULL,
    description TEXT NOT NULL
);

--JUNCTION FOR ANSWERS AND PERSONALITIES
CREATE TABLE answer_personalities (
    answer_id SERIAL,
    personality_id SERIAL,
    CONSTRAINT answer_pers_pk PRIMARY KEY (answer_id, personality_id),
    CONSTRAINT FK_answer FOREIGN KEY (answer_id) REFERENCES answers (answer_id),
    CONSTRAINT FK_personality FOREIGN KEY (personality_id) REFERENCES personalities(personality_id)
);

--USERS
CREATE TABLE users (
    user_id SERIAL PRIMARY KEY,
    username VARCHAR(40) NOT NULL UNIQUE,
);

--JUNCTION FOR USERS AND ANSWERS
CREATE TABLE user_answers (
    answer_id SERIAL,
    user_id SERIAL,
    CONSTRAINT answer_user_pk PRIMARY KEY (answer_id, user_id),
    CONSTRAINT FK_answer FOREIGN KEY (answer_id) REFERENCES answers (answer_id),
    CONSTRAINT FK_user FOREIGN KEY (user_id) REFERENCES users (user_id)
)



