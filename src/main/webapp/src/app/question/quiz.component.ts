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
  loadingReleasedQuestions: boolean = true;

  constructor(private gapi_: GapiService, private _snackbar: MdSnackBar) {
    this.loadingReleasedQuestions = true;
    this.gapi_.loadReleasedQuestions().then(
      (questions) => {
        this.questions = List<Question>(questions);
        this.loadingReleasedQuestions = false;
      }
    )
  }
}