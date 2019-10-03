package pt.ulisboa.tecnico.socialsoftware.tutor.quiz.dto;

import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.QuizQuestion;


public class QuizQuestionDto {
    private Integer id;
    private Integer sequence;

    public QuizQuestionDto() {}

    public QuizQuestionDto(QuizQuestion quizQuestion) {
        this.id = quizQuestion.getId();
        this.sequence = quizQuestion.getSequence();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getSequence() {
        return sequence;
    }

    public void setSequence(Integer sequence) {
        this.sequence = sequence;
    }
}