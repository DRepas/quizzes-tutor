package pt.ulisboa.tecnico.socialsoftware.tutor.questionsubmission.service

import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import pt.ulisboa.tecnico.socialsoftware.tutor.SpockTest
import pt.ulisboa.tecnico.socialsoftware.tutor.BeanConfiguration
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Option
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question
import pt.ulisboa.tecnico.socialsoftware.tutor.questionsubmission.domain.QuestionSubmission
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User
import spock.lang.Unroll

@DataJpaTest
class SetQuestionSubmissionVisibilityTest extends SpockTest{
    def student
    def question
    def optionOK
    def questionSubmission

    def setup() {
        student = new User(USER_1_NAME, USER_1_USERNAME, USER_1_EMAIL, User.Role.STUDENT, true, false)
        student.setEnrolledCoursesAcronyms(externalCourseExecution.getAcronym())
        userRepository.save(student)
        question = new Question()
        question.setKey(1)
        question.setTitle(QUESTION_1_TITLE)
        question.setContent(QUESTION_1_CONTENT)
        question.setCourse(externalCourse)
        question.setStatus(Question.Status.SUBMITTED)
        questionRepository.save(question)
        optionOK = new Option()
        optionOK.setContent(OPTION_1_CONTENT)
        optionOK.setCorrect(true)
        optionOK.setSequence(0)
        optionOK.setQuestion(question)
        optionRepository.save(optionOK)
        questionSubmission = new QuestionSubmission()
        questionSubmission.setQuestion(question)
        questionSubmission.setSubmitter(student)
        questionSubmission.setCourseExecution(externalCourseExecution)
        questionSubmission.setStatus(QuestionSubmission.Status.IN_REVISION)
        externalCourseExecution.addQuestionSubmission(questionSubmission)
        student.addQuestionSubmission(questionSubmission)
        questionSubmissionRepository.save(questionSubmission)
    }

    @Unroll
    def "set submission visibility"() {
        when:
        if (forTeacher) {
            questionSubmissionService.setTeacherSubmissionVisibility(questionSubmission.getId(), hasRead)
        } else {
            questionSubmissionService.setStudentSubmissionVisibility(questionSubmission.getId(), hasRead)
        }

        then: "the question submission visibility is changed"
        questionSubmissionRepository.count() == 1L
        def result = questionSubmissionRepository.findAll().get(0)
        if (forTeacher) {
            result.hasTeacherRead() == hasRead
        } else {
            result.hasStudentRead() == hasRead
        }

        where:
        forTeacher | hasRead
        true       | false
        false      | false
        true       | true
        false      | true

    }

    @TestConfiguration
    static class LocalBeanConfiguration extends BeanConfiguration {}
}





