import { Component, destroyPlatform, Optional } from '@angular/core';
import { FormBuilder, FormGroup, FormControl, Validators } from '@angular/forms';
import { MdDialogRef } from '@angular/material';

import { Question } from '../common/question'

@Component({
  templateUrl: 'create-question-dialog.component.html',
  styleUrls: ['create-question-dialog.component.scss'],
})
export class CreateQuestionDialog {
  questionForm: FormGroup;
  description: FormControl;
  optionA: FormControl;
  optionB: FormControl;
  optionC: FormControl;
  optionD: FormControl;
  answer: number;
  confirmLabel: string;
  question: Question;

  constructor(
    @Optional() private _dialogRef: MdDialogRef<CreateQuestionDialog>,
    private _fb: FormBuilder,) { 
      this.description = new FormControl('', Validators.maxLength(500));
      this.optionA = new FormControl('', Validators.maxLength(100));
      this.optionB = new FormControl('', Validators.maxLength(100));
      this.optionC = new FormControl('', Validators.maxLength(100));
      this.optionD = new FormControl('', Validators.maxLength(100));
      this.answer = 2; //defaut to option C
      this.confirmLabel = 'Create';

      this.questionForm = _fb.group({
        description: this.description,
        optionA: this.optionA,
        optionB: this.optionB,
        optionC: this.optionC,
        optionD: this.optionD,
        answer: this.answer,
      });
  }

  ngOnInit() {
    if (this.question) {
      this.description.setValue(this.question.description);
      this.optionA.setValue(this.question.options[0]);
      this.optionB.setValue(this.question.options[1]);
      this.optionC.setValue(this.question.options[2]);
      this.optionD.setValue(this.question.options[3]);
      this.answer = this.question.answer;
      this.confirmLabel = 'Update';
    }
  }

  cancel(event: any) {
    this._dialogRef.close();
  }

  confirm(event: any) {
    // Prevent page reloading
    event.preventDefault();
    this.question = Object.assign(this.question || {}, {
      description: this.description.value,
      options: [this.optionA.value, this.optionB.value, this.optionC.value, this.optionD.value],
      answer: this.answer,
    })
    this._dialogRef.close(this.question);
  }
}