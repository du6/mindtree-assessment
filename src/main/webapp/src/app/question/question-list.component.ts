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
  enableEditRelease: boolean = false;
  knowledgeNodes: List<KnowledgeNode>;
  loadingKnowledgeNodes: boolean = true;
  loadingReleasedQuestions: boolean = true;
  releasedQuestionKeys: Set<string> = new Set();

  constructor(private gapi_: GapiService, private _snackbar: MdSnackBar) {
    this.loadingKnowledgeNodes = true;
    this.gapi_.loadAllKnowledgeNodes().then(
      (nodes) => {
        this.knowledgeNodes = List<KnowledgeNode>(nodes);
        this.loadingKnowledgeNodes = false;
      });

    this.loadingReleasedQuestions = true;
    this.gapi_.loadReleasedQuestions().then(
      (questions) => {
        questions.forEach((question) => this.releasedQuestionKeys.add(question.websafeKey));
        this.loadingReleasedQuestions = false;
      }
    )
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

  isReleased(question: Question) {
    return this.releasedQuestionKeys.has(question.websafeKey);
  }

  onAddRelease(questionKey: string) {
    this.releasedQuestionKeys.add(questionKey);
  }
  
  onDeleteRelease(questionKey: string) {
    this.releasedQuestionKeys.delete(questionKey);
  }

  toggleEditRelease() {
    if (this.enableEditRelease) {
      this.gapi_.saveReleasedQuestions(this.releasedQuestionKeys).then(
        () => this.enableEditRelease = !this.enableEditRelease, 
        (error) => this._snackbar.open("Failed to save release", "DISMISS"));
    } else {
      this.enableEditRelease = !this.enableEditRelease;
    }
  }
}