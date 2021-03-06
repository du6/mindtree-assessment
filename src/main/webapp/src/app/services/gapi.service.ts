import { ANY_STATE } from '@angular/core/src/animation/animation_constants';
import { Injectable } from '@angular/core';

import { KnowledgeNode, KnowledgeEdge } from '../common/knowledge-node';

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
    return this.gapi_.client.mindTreeApi.deleteEdges({
      parentKey: fromNode,
      childKey: toNode,
    });
  }
}
