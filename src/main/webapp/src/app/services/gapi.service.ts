import { Response } from '@angular/http';
import { ANY_STATE } from '@angular/core/src/animation/animation_constants';
import { Injectable } from '@angular/core';

import { KnowledgeNode, KnowledgeEdge } from '../common/knowledge-node';
import { Question, QuestionTag } from '../common/question';

// Google's login API namespace
declare var gapi: { client: { mindTreeApi: any } };

@Injectable()
export class GapiService {
  private gapi_: { client: { mindTreeApi: any } };
  static QUERY_LIMIT: number = 10000;

  constructor() {
    this.gapi_ = gapi;
  }

  loadAllKnowledgeNodes(limit: number = GapiService.QUERY_LIMIT): Promise<KnowledgeNode[]> {
    return new Promise((resolve,reject) => 
        this.gapi_.client.mindTreeApi.getAllKnowledgeNodes({ limit: limit })
            .execute((resp) => {
              if (resp.error) {
                reject(resp.error);
              } else if (resp.result) {
                resolve(<KnowledgeNode[]> resp.result.items);
              }
            }));
  }

  loadAllKnowledgeEdges(limit: number = GapiService.QUERY_LIMIT): Promise<KnowledgeEdge[]> {
    return new Promise((resolve,reject) => 
        this.gapi_.client.mindTreeApi.getAllEdges({ limit: limit })
            .execute((resp) => {
              if (resp.error) {
                reject(resp.error);
              } else if (resp.result) {
                resolve(<KnowledgeEdge[]> resp.result.items);
              }
            }));
  }

  loadAllQuestions(limit: number = GapiService.QUERY_LIMIT): Promise<Question[]> {
    return new Promise((resolve, reject) => 
        this.gapi_.client.mindTreeApi.getAllActiveQuestions({ limit: limit })
            .execute((resp) => {
              if (resp.error) {
                reject(resp.error);
              } else if (resp.result) {
                resolve(<Question[]> resp.result.items);
              }
            }));
  } 

  addNode(name?: string, description?: string): Promise<any> {
    return this.gapi_.client.mindTreeApi.createKnowledgeNode({
      name: name,
      description: description,
    });
  }

  addEdge(fromNode: string, toNode: string): Promise<any> {
    return this.gapi_.client.mindTreeApi.createEdge({
      parentKey: fromNode,
      childKey: toNode,
    });
  }

  deleteNode(nodeKey: string) {
    return this.gapi_.client.mindTreeApi.deleteKnowledgeNode({
      websafeKnowledgeNodeKey: nodeKey
    });
  }

  deleteEdge(fromNode: string, toNode: string): Promise<any> {
    return this.gapi_.client.mindTreeApi.deleteEdges({
      parentKey: fromNode,
      childKey: toNode,
    });
  }

  createQuestion(question: Question) {
    return this.gapi_.client.mindTreeApi.createQuestion(question);
  }

  deleteQuestion(questionKey: string) {
    return this.gapi_.client.mindTreeApi.deleteQuestion({
      websafeQuestionKey: questionKey
    });
  }

  updateQuestion(question: Question) {
    return this.gapi_.client.mindTreeApi.updateQuestion(Object.assign({
      websafeQuestionKey: question.websafeKey,
    }, question));
  }

  getQuestionTags(
    questionKey: string, 
    limit: number = GapiService.QUERY_LIMIT): Promise<QuestionTag[]> {
      return new Promise((resolve, reject) => 
        this.gapi_.client.mindTreeApi.getQuestionTags({
          websafeQuestionKey: questionKey,
          limit: limit,
        }).execute((resp) => {
          if (resp.error) {
            reject(resp.error);
          } else if (resp.result) {
            resolve(<QuestionTag[]> resp.result.items);
          }
        }));
  }

  addQuestionTag(nodeKey: string, questionKey: string): Promise<{result: QuestionTag}> {
    return this.gapi_.client.mindTreeApi.createQuestionTag({
      nodeKey: nodeKey,
      questionKey: questionKey,
    });
  }

  deleteQuestionTag(tagKey: string) {
    return this.gapi_.client.mindTreeApi.deleteQuestionTag({
      websafeQuestionTagKey: tagKey
    });
  }
}