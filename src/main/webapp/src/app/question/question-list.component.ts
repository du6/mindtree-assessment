import {
  Component, 
  Optional, 
  Input,
  trigger,
  state,
  style,
  transition,
  animate } from '@angular/core';
import { GapiService } from '../services/gapi.service';
import { MdSnackBar } from '@angular/material';
import { List } from 'immutable'

import { Question } from '../common/question';
import { KnowledgeNode } from '../common/knowledge-node';

@Component({
  selector: 'mind-tree-question-list',
  templateUrl: 'question-list.component.html',
  styleUrls: ['question-list.component.scss'],
  animations: [
    trigger('fadeInOut', [
      state('in', style({
        height: '100%',
        opacity: 1,
      })),
      transition('void => *', [
        style({
          height: 0,
          opacity: 0
        }),
        animate(250, style({
          height: '100%',
          opacity: 1
        }))
      ]),
      transition('* => void', [
        style({
          height: '100%',
          opacity: 1
        }),
        animate(250, style({
          height: 0,
          opacity: 0
        }))
      ])
    ])
  ]
})
export class QuestionListComponent {
  @Input() questions: List<Question>;
  knowledgeNodes: List<KnowledgeNode>;
  loading: boolean = true;

  constructor(private gapi_: GapiService, private _snackbar: MdSnackBar) {
    this.loading = true;
    this.gapi_.loadAllKnowledgeNodes().then(
      (nodes) => {
        this.knowledgeNodes = List<KnowledgeNode>(nodes);
        this.loading = false;
      });
  }

  onQuestionDeleted(question: Question) {
    const index = this.questions.findIndex(((q) => q.websafeKey == question.websafeKey));
    if (index >= 0) {
      this.questions = this.questions.delete(index);
    }
  }

  onQuestionUpdated(question: Question) {
    const index = this.questions.findIndex(((q) => q.websafeKey == question.websafeKey));
    if (index >= 0) {
      this.questions[index] = question;
    }
  }
}