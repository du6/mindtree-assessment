import { Component } from '@angular/core';

@Component({
  selector: 'mind-tree-quiz',
  templateUrl: 'quiz.component.html',
  styleUrls: ['quiz.component.scss'],
})
export class QuizComponent {
  knowledgeGraph: any;

  constructor() {
  }
}