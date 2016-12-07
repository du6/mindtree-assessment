import { Component, Input, Output, EventEmitter } from '@angular/core';

import { Question } from '../common/question';

@Component({
  selector: 'mind-tree-quiz-item',
  templateUrl: 'quiz-item.component.html',
  styleUrls: ['quiz-item.component.scss'],
})
export class QuizItemComponent {
  @Input() question: Question;
  @Output() correctAnswer: EventEmitter<string> = new EventEmitter();
  @Output() wrongAnswer: EventEmitter<string> = new EventEmitter();
  answer: number = -1;

  constructor() {}

  getIndex(option: string): number {
    return this.question.options.indexOf(option);
  }

  onSelectChange(change: any) {
    if (change.value == this.question.answer) {
      this.correctAnswer.emit(this.question.websafeKey);
    } else {
      this.wrongAnswer.emit(this.question.websafeKey);
    }
  }
}