import { List } from 'immutable'

import { QuestionTag } from '../common/question';
import { KnowledgeNode } from '../common/knowledge-node';
import { Pipe, PipeTransform } from '@angular/core';

@Pipe({name: 'excludenodes'})
export class ExcludeNodesPipe implements PipeTransform {
  transform(value: List<KnowledgeNode>, exclude: List<QuestionTag>): List<KnowledgeNode> {
    return value.filter(node => exclude.findIndex(tag => tag.nodeKey == node.websafeKey) < 0).toList();
  }
}