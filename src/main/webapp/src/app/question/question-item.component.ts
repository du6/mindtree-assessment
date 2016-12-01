import { Component, Input, Output, EventEmitter, ViewContainerRef } from '@angular/core';
import { MdSnackBar } from '@angular/material';
import { MdDialog, MdDialogRef, MdDialogConfig } from '@angular/material';
import { List } from 'immutable'

import { GapiService } from '../services/gapi.service';
import { CreateQuestionDialog } from './create-question-dialog.component';
import { Question, QuestionTag } from '../common/question';
import { KnowledgeNode } from '../common/knowledge-node';

@Component({
  selector: 'mind-tree-question-item',
  templateUrl: 'question-item.component.html',
  styleUrls: ['question-item.component.scss'],
})
export class QuestionItemComponent {
  @Input() question: Question;
  @Input() knowledgeNodes: List<KnowledgeNode>;
  @Output() questionDeleted: EventEmitter<Question> = new EventEmitter();
  @Output() questionUpdated: EventEmitter<Question> = new EventEmitter();
  questionTags: List<QuestionTag> = List<QuestionTag>();
  nodeMap: Map<string, string> = new Map();

  constructor(
    private gapi_:GapiService, 
    private _snackbar: MdSnackBar,
    private _dialog: MdDialog,
    public viewContainerRef: ViewContainerRef) {
    }

  ngOnInit() {
    this.gapi_.getQuestionTags(this.question.websafeKey).then(
      (tags) => this.questionTags = List<QuestionTag>(tags));
    
    this.knowledgeNodes.forEach((node) => this.nodeMap.set(node.websafeKey, node.name));
  }

  getNodeName(nodeKey: string): string {
    return this.nodeMap.get(nodeKey);
  }

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

  tagKnowledgeNode(nodeKey: string) {
    this.gapi_.addQuestionTag(nodeKey, this.question.websafeKey).then((tag) => {
      this.questionTags = this.questionTags.push(tag);
    });
  }
}