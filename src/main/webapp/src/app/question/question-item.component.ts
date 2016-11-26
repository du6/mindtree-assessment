import { Component, Input, Output, EventEmitter, ViewContainerRef } from '@angular/core';
import { MdSnackBar } from '@angular/material';
import { MdDialog, MdDialogRef, MdDialogConfig } from '@angular/material';

import { GapiService } from '../services/gapi.service';
import { CreateQuestionDialog } from './create-question-dialog.component'
import { Question } from '../common/question'

@Component({
  selector: 'mind-tree-question-item',
  templateUrl: 'question-item.component.html',
  styleUrls: ['question-list.component.scss'],
})
export class QuestionItemComponent {
  @Input() question: Question;
  @Output() questionDeleted: EventEmitter<Question> = new EventEmitter();
  @Output() questionUpdated: EventEmitter<Question> = new EventEmitter();

  constructor(
    private gapi_:GapiService, 
    private _snackbar: MdSnackBar,
    private _dialog: MdDialog,
    public viewContainerRef: ViewContainerRef) {}

  deleteQuestion() {
    this.gapi_.deleteQuestion(this.question.websafeKey).then((resp) => {
      this.questionDeleted.emit(this.question)
    }, (error) => {
      this._snackbar.open('Failed to delete question', 'DISMISS');
    });
  }

  editQuestion() {
    let config = new MdDialogConfig();
    config.viewContainerRef = this.viewContainerRef;
    let dialogRef = this._dialog.open(CreateQuestionDialog, config);
    dialogRef.componentInstance.question = this.question;
    dialogRef.afterClosed().subscribe(question => {
      if (question) {
        this.gapi_.updateQuestion(question).then((data: any) => {
          this.questionUpdated.emit(this.question)
        }, (error) => this._snackbar.open('Failed to update the question', 'DISMISS'));
      }
    })
  }
}