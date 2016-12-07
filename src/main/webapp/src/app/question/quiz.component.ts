import { Component } from '@angular/core';
import { List } from 'immutable';
import { MdSnackBar } from '@angular/material';

import { GapiService } from '../services/gapi.service';
import { Question } from '../common/question';

@Component({
  selector: 'mind-tree-quiz',
  templateUrl: 'quiz.component.html',
  styleUrls: ['quiz.component.scss'],
})
export class QuizComponent {
  questions: List<Question> = List<Question>();
  failedQuestionKeys: List<string> = List<string>();
  loadingQuestions: boolean = true;

  constructor(private gapi_: GapiService, private _snackbar: MdSnackBar) {
    this.loadingQuestions = true;
    this.gapi_.loadReleasedQuestions().then(
      (questions) => {
        this.questions = List<Question>(questions);
        this.failedQuestionKeys = List<string>(questions.map(question => question.websafeKey));
        this.loadingQuestions = false;
      }
    );
  }

  onCorrectAnswer(questionKey: string) {
    this.failedQuestionKeys = this.failedQuestionKeys.remove(
      this.failedQuestionKeys.indexOf(questionKey));
  }

  onWrongAnswer(questionKey: string) {
    this.failedQuestionKeys = this.failedQuestionKeys.push(questionKey);
  }

  loadFollowupQuestions() {
    this.loadingQuestions = true;
    if (this.failedQuestionKeys.isEmpty()) {
      this.questions = List<Question>();
      this.loadingQuestions = false;
    } else {
      this.gapi_.loadFollowupQuestions(this.failedQuestionKeys.toArray()).then(
          (questions) => {
            this.questions = List<Question>(questions);
            this.failedQuestionKeys = List<string>((questions || []).map(question => question.websafeKey));
            this.loadingQuestions = false;
          }
      );
    }
  }
}