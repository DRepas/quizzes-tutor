import ExternalUser from './ExternalUser';

export default class Course {
  academicTerm: string | undefined;
  acronym: string | undefined;
  courseExecutionId: number | undefined;
  courseExecutionType: string = 'EXTERNAL';
  courseId: number | undefined;
  courseType: string = 'EXTERNAL';
  name: string | undefined;
  numberOfQuestions: number | undefined;
  numberOfQuizzes: number | undefined;
  numberOfStudents: number | undefined;
  numberOfTeachers: number | undefined;
  status: string | undefined;
  courseExecutionUsers: ExternalUser[] | undefined;

  constructor(jsonObj?: Course) {
    if (jsonObj) {
      this.academicTerm = jsonObj.academicTerm;
      this.acronym = jsonObj.acronym;
      this.courseExecutionId = jsonObj.courseExecutionId;
      this.courseExecutionType = jsonObj.courseExecutionType;
      this.courseId = jsonObj.courseId;
      this.courseType = jsonObj.courseType;
      this.name = jsonObj.name;
      this.numberOfQuestions = jsonObj.numberOfQuestions;
      this.numberOfQuizzes = jsonObj.numberOfQuizzes;
      this.numberOfStudents = jsonObj.numberOfStudents;
      this.numberOfTeachers = jsonObj.numberOfTeachers;
      this.status = jsonObj.status;
      if(jsonObj.courseExecutionUsers){
        this.courseExecutionUsers = jsonObj.courseExecutionUsers
          .map((externalUser: ExternalUser) => new ExternalUser(externalUser));
      }
    }
  }
}
