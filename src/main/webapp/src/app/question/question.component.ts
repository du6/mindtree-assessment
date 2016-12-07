import { Component, Optional } from '@angular/core';
import { MdDialog, MdDialogRef, MdSnackBar } from '@angular/material';
import { List } from 'immutable';

import { GapiService } from '../services/gapi.service';
import { CreateQuestionDialog } from './create-question-dialog.component';
import { Question } from '../common/question';
import { HomeComponent } from '../home/home.component';

@Component({
  selector: 'mind-tree-question',
  templateUrl: 'question.component.html',
  styleUrls: ['question.component.scss'],
})
export class QuestionComponent {
  questionList: List<Question> = List<Question>();
  loadingList: boolean;

  constructor(private gapi_: GapiService, private _dialog: MdDialog, private _snackbar: MdSnackBar) {
    this.loadingList = true;
    this.gapi_.loadAllQuestions().then((questions) => {
      this.loadingList = false;
      this.questionList = List<Question>(questions);
    }, (error) => {
      this.loadingList = false;
      this._snackbar.open('Error loading questions', 'DISMISS');
    })
  }

  openCreateQuestionDialog() {
    let dialogRef = this._dialog.open(CreateQuestionDialog);
    dialogRef.afterClosed().subscribe(question => {
      if (question) {
        this.gapi_.createQuestion(question).then((data: any) => {
          this.questionList = this.questionList.unshift(data.result);
        }, (error) => this._snackbar.open('Failed to save the question', 'DISMISS'));
      }
    })
  }

  isTeacher() {
    return HomeComponent.isTeacher();
  }
}