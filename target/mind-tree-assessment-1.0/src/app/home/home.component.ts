import { Component } from '@angular/core';
import { Router } from '@angular/router';

import { AuthService } from '../services/auth.service';
import { GapiService } from '../services/gapi.service';
import { KnowledgeNode } from '../common/knowledge-node';

@Component({
  selector: 'mind-tree-home',
  templateUrl: 'home.component.html',
  styleUrls: ['home.component.scss'],
})
export class HomeComponent {
  constructor(private auth_: AuthService, private gapi_: GapiService) {
  }

  ngAfterViewInit() {
    this.gapi_.loadAllKnowledgeNodes().then(nodes => {
      this.loadKnowledgeGraphDrawer_(nodes);
    });
    
  }

  isSignedIn(): boolean {
    return this.auth_.isSignedIn();
  }

  private loadKnowledgeGraphDrawer_(knowledgeNodes: KnowledgeNode[]) {
    const knowledgeNodeMap = new Map();
    knowledgeNodes.forEach(knowledgeNode => {
      knowledgeNodeMap.set(knowledgeNode.websafeKey, knowledgeNode);
    });

    const nodes = knowledgeNodes.map((knowledgeNode => {
      return {
        id: knowledgeNode.websafeKey,
        label: knowledgeNode.name,
      }
    }));
    const nodeDataSet = new vis.DataSet(nodes);

    const edges = [];
    knowledgeNodes.forEach(knowledgeNode => {
      if (knowledgeNode.children) {
        knowledgeNode.children.forEach(child => {
          edges.push({
            from: knowledgeNode.websafeKey,
            to: knowledgeNodeMap.get(child).websafeKey,
          });
        });
      }
    });
    const edgeDataSet = new vis.DataSet(edges);

    // create a network
    const container = document.getElementById('knowledge-graph-drawer');

    // provide the data in the vis format
    const data = {
        nodes: nodeDataSet,
        edges: edgeDataSet,
    };

    const options = {
      edges: {
        arrows: 'to'
      }
    };

    // initialize your network!
    const network = new vis.Network(container, data, options);
  }
}
