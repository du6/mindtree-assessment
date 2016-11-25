import { Component, Input, Output, EventEmitter } from '@angular/core';
import { MdSnackBar } from '@angular/material';

import { GapiService } from '../services/gapi.service';

import { Question } from '../common/question'

@Component({
  selector: 'mind-tree-question-item',
  templateUrl: 'question-item.component.html',
  styleUrls: ['question-list.component.scss'],
})
export class QuestionItemComponent {
  @Input() question: Question;
  @Output() questionDeleted: EventEmitter<Question> = new EventEmitter();

  constructor(private gapi_:GapiService, private _snackbar: MdSnackBar) {}

  deleteQuestion() {
    this.gapi_.deleteQuestion(this.question.websafeKey).then((resp) => {
      this.questionDeleted.emit(this.question)
    }, (error) => {
      this._snackbar.open('Failed to delete question', 'DISMISS');
    });
  }
}