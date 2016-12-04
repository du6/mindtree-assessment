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
  @Input() taggableKnowledgeNodes: List<KnowledgeNode> = List<KnowledgeNode>();
  @Input() questionTags: List<QuestionTag> = List<QuestionTag>();
  @Output() questionDeleted: EventEmitter<Question> = new EventEmitter();
  @Output() questionUpdated: EventEmitter<Question> = new EventEmitter();
  nodeMap: Map<string, KnowledgeNode> = new Map();

  constructor(
    private gapi_:GapiService, 
    private _snackbar: MdSnackBar,
    private _dialog: MdDialog,
    public viewContainerRef: ViewContainerRef) {
    }

  ngOnInit() {
    this.gapi_.getQuestionTags(this.question.websafeKey).then((tags) => {
      this.questionTags = List<QuestionTag>(tags);
      this.taggableKnowledgeNodes = this.knowledgeNodes.filter(node => 
        this.questionTags.findIndex(tag => tag.nodeKey == node.websafeKey) < 0
      ).toList();
    });
    
    this.knowledgeNodes.forEach((node) => this.nodeMap.set(node.websafeKey, node));
  }

  getNodeName(nodeKey: string): string {
    return this.nodeMap.get(nodeKey).name;
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
    this.gapi_.addQuestionTag(nodeKey, this.question.websafeKey).then((data) => {
      this.questionTags = this.questionTags.push(data.result);
      const index = this.taggableKnowledgeNodes.findIndex(node => node.websafeKey == nodeKey);
      if (index >= 0) {
        this.taggableKnowledgeNodes = this.taggableKnowledgeNodes.delete(index);
      }
    });
  }

  onTagDeleted(tag: QuestionTag) {
    const index = this.questionTags.findIndex(questionTag => questionTag.websafeKey == tag.websafeKey);
    if (index >= 0) {
      this.questionTags = this.questionTags.delete(index);
    }
    if (this.taggableKnowledgeNodes.findIndex(node => node.websafeKey == tag.nodeKey) < 0) {
      this.taggableKnowledgeNodes = this.taggableKnowledgeNodes.push(this.nodeMap.get(tag.nodeKey));
    }
  }
}