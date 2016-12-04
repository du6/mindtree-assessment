import { Component, Input, Output, EventEmitter, NgZone } from '@angular/core';

import { GapiService } from '../services/gapi.service';
import { QuestionTag } from '../common/question';

@Component({
  selector: 'mind-tree-question-tag',
  templateUrl: 'question-tag.component.html',
  styleUrls: ['question-tag.component.scss'],
})
export class QuestionTagComponent {
  @Input() tag: QuestionTag;
  @Input() name: string;
  @Output() tagDeleted: EventEmitter<QuestionTag> = new EventEmitter();

  constructor(private gapi_:GapiService, private ngZone_: NgZone) {}

  deleteTag() {
    this.gapi_.deleteQuestionTag(this.tag.websafeKey).then(() => {
      this.ngZone_.run(() => this.tagDeleted.emit(this.tag))});
  }
}