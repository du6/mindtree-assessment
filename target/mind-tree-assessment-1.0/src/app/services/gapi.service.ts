import { ANY_STATE } from '@angular/core/src/animation/animation_constants';
import { Injectable } from '@angular/core';

import { KnowledgeNode } from '../common/knowledge-node';

// Google's login API namespace
declare var gapi: { client: { mindTreeApi: any } };

@Injectable()
export class GapiService {
  private gapi_: { client: { mindTreeApi: any } };

  constructor() {
    this.gapi_ = gapi;
  }

  loadAllKnowledgeNodes(limit: number = 1000): Promise<KnowledgeNode[]> {
    return new Promise((resolve,reject) => 
        this.gapi_.client.mindTreeApi.getAllKnowledgeNodes(limit)
            .execute((resp) => {
              if (resp.error) {
                reject(resp.error);
              } else if (resp.result) {
                resolve(<KnowledgeNode[]> resp.result.items);
              }
            }));
  }

  addNode(name?: string, description?: string): Promise<KnowledgeNode> {
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
    return this.gapi_.client.mindTreeApi.deleteEdge({
      parentKey: fromNode,
      childKey: toNode,
    });
  }
}
