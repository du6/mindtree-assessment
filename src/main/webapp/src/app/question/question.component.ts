import { Component, Optional } from '@angular/core';
import { GapiService } from '../services/gapi.service';
import { MdDialog, MdDialogRef, MdSnackBar } from '@angular/material';

import { CreateQuestionDialog } from './create-question-dialog.component'

@Component({
  selector: 'mind-tree-question',
  templateUrl: 'question.component.html',
  styleUrls: ['question.component.scss'],
})
export class QuestionComponent {
  knowledgeGraph: any;

  constructor(private gapi_: GapiService, private _dialog: MdDialog, private _snackbar: MdSnackBar) {
  }

  openCreateQuestionDialog() {
    let dialogRef = this._dialog.open(CreateQuestionDialog);
    dialogRef.afterClosed().subscribe(question => {
      if (question) {
        this.gapi_.createQuestion(question).then((data: any) => {
        }, (error) => this._snackbar.open('Failed to save the question', 'DISMISS'));
      }
    })
  }
}