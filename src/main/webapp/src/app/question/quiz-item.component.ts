import { Component, Input } from '@angular/core';

import { Question } from '../common/question';

@Component({
  selector: 'mind-tree-quiz-item',
  templateUrl: 'quiz-item.component.html',
  styleUrls: ['quiz-item.component.scss'],
})
export class QuizItemComponent {
  @Input() question: Question;
  answer: number = -1;

  constructor() {}

  getIndex(option: string): number {
    return this.question.options.indexOf(option);
  }
}