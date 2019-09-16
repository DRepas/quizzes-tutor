package pt.ulisboa.tecnico.socialsoftware.tutor.quiz;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.ulisboa.tecnico.socialsoftware.tutor.ResourceNotFoundException;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException;
import pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain.QuizzesXmlExport;
import pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain.QuizzesXmlImport;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.QuestionDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.QuestionRepository;
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.Quiz;
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.QuizQuestion;
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.repository.QuizRepository;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserRepository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException.ExceptionError.*;

@Service
public class QuizService {

    @Autowired
    private QuizRepository quizRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private UserRepository userRepository;

    @PersistenceContext
    EntityManager entityManager;


    public List<Quiz> findAll(int pageIndex, int pageSize) {
        return quizRepository.findAll(PageRequest.of(pageIndex, pageSize)).getContent();
    }

    @Transactional
    public QuizDto findById(Integer quizId) {
        return this.quizRepository.findById(quizId).map(quiz -> new QuizDto(quiz, true))
                .orElseThrow(() -> new ResourceNotFoundException("Quiz not found with id " + quizId));
    }

    @Transactional
    public List<QuizDto> findAllNonGenerated() {
        Comparator<Quiz> comparator = Comparator.comparing(Quiz::getYear, Comparator.nullsFirst(Comparator.reverseOrder()))
                .thenComparing(Quiz::getSeries, Comparator.nullsFirst(Comparator.reverseOrder()))
                .thenComparing(Quiz::getVersion, Comparator.nullsFirst(Comparator.reverseOrder()));
        return quizRepository.findAllNonGenerated().stream()
                .sorted(comparator).map(quiz -> new QuizDto(quiz, false))
                .collect(Collectors.toList());
    }


    public Integer getMaxQuizNumber() {
        Integer maxQuizNumber = quizRepository.getMaxQuizNumber();
        return maxQuizNumber != null ? maxQuizNumber : 0;
    }

    @Transactional
    public QuizDto createQuiz(QuizDto quizDto) {
        if (quizDto.getNumber() == null) {
            quizDto.setNumber(getMaxQuizNumber() + 1);
        }
        Quiz quiz = new Quiz(quizDto);

        if (quizDto.getQuestions() != null) {
            for (QuestionDto questionDto : quizDto.getQuestions()) {
                Question question = questionRepository.findById(questionDto.getId())
                        .orElseThrow(() -> new TutorException(QUESTION_NOT_FOUND, Integer.toString(questionDto.getId())));
                new QuizQuestion(quiz, question, quiz.getQuizQuestions().size());
            }
        }

        entityManager.persist(quiz);

        return new QuizDto(quiz, true);
    }

    @Transactional
    public QuizDto updateQuiz(Integer quizId, QuizDto quizDto) {
        Quiz quiz = quizRepository.findById(quizId).orElseThrow(() ->new TutorException(QUIZ_NOT_FOUND, Integer.toString(quizId)));

        quiz.setTitle(quizDto.getTitle());
        quiz.setAvailableDate(quizDto.getAvailableDate());
        quiz.setScramble(quizDto.getScramble());

        Set<QuizQuestion> quizQuestions = new HashSet<>(quiz.getQuizQuestions());

        quizQuestions.forEach(QuizQuestion::remove);
        quizQuestions.forEach(quizQuestion -> entityManager.remove(quizQuestion));

        if (quizDto.getQuestions() != null) {
            for (QuestionDto questionDto : quizDto.getQuestions()) {
                Question question = questionRepository.findById(questionDto.getId())
                        .orElseThrow(() -> new TutorException(QUESTION_NOT_FOUND, Integer.toString(questionDto.getId())));
                QuizQuestion quizQuestion = new QuizQuestion(quiz, question, quiz.getQuizQuestions().size());
                entityManager.persist(quizQuestion);
            }
        }

        return new QuizDto(quiz, true);
    }

    @Transactional
    public QuizQuestion addQuestionToQuiz(int questionId, int quizId) {
        Quiz quiz = quizRepository.findById(quizId).orElseThrow(() ->new TutorException(QUIZ_NOT_FOUND, Integer.toString(quizId)));
        Question question = questionRepository.findById(questionId).orElseThrow(() ->new TutorException(QUESTION_NOT_FOUND, Integer.toString(questionId)));

        QuizQuestion quizQuestion = new QuizQuestion(quiz, question, quiz.getQuizQuestions().size());

        entityManager.persist(quizQuestion);

        return quizQuestion;
    }

    @Transactional
    public void removeQuiz(Integer quizId) {
        Quiz quiz = quizRepository.findById(quizId).orElseThrow(() ->new TutorException(QUIZ_NOT_FOUND, Integer.toString(quizId)));

        quiz.checkCanRemove();

        Set<QuizQuestion> quizQuestions = new HashSet<>(quiz.getQuizQuestions());

        quizQuestions.forEach(QuizQuestion::remove);
        quizQuestions.forEach(quizQuestion -> entityManager.remove(quizQuestion));

        entityManager.remove(quiz);
    }

    @Transactional
    public String exportQuizzes() {
        QuizzesXmlExport xmlExport = new QuizzesXmlExport();

        return xmlExport.export(quizRepository.findAll());
    }

    @Transactional
    public void importQuizzes(String quizzesXml) {
        QuizzesXmlImport xmlImport = new QuizzesXmlImport();

        xmlImport.importQuizzes(quizzesXml, this, questionRepository);
    }

}