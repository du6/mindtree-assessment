import { Component, Optional } from '@angular/core';
import { GapiService } from '../services/gapi.service';
import { MdDialog, MdDialogRef, MdSnackBar } from '@angular/material';

import { KnowledgeNode } from '../common/knowledge-node';
import { AddNodeDialog } from './add-node-dialog.component';

export class Node {
  id: string;
  label: string;
}

export class Edge {
  id: string;
  from: string;
  to: string;
}

@Component({
  selector: 'knowledge-graph',
  templateUrl: 'knowledge-graph.component.html',
  styleUrls: ['knowledge-graph.component.scss'],
})
export class KnowledgeGraphComponent {
  knowledgeGraph: any;

  constructor(private gapi_: GapiService, private _dialog: MdDialog, private _snackbar: MdSnackBar) {
  }

  ngAfterViewInit() {
    this.gapi_.loadAllKnowledgeNodes().then(nodes => {
      this.knowledgeGraph = this.loadKnowledgeGraphDrawer_(nodes);
      this.knowledgeGraph.on("click", (params) => this.onGraphClicked_(params));
    });
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
          const fromNodeKey = knowledgeNode.websafeKey;
          const toNodeKey = knowledgeNodeMap.get(child).websafeKey;  
          edges.push({
            id: this.createEdgeId(fromNodeKey, toNodeKey),
            from: fromNodeKey,
            to: toNodeKey,
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
      },

      manipulation: {
        addNode: (nodeData, callback) => {
          this.openAddNodeDialog(callback);
        },

        deleteNode: (params, callback) => {
          if (!params) {
            return;
          }
          if (params.nodes && params.nodes.length == 1) {
            this.gapi_.deleteNode(params.nodes[0]).then(() => callback(params));
          }
        },

        addEdge: (edgeData, callback) => {
          if (edgeData.from !== edgeData.to) {
            edgeData.id = this.createEdgeId(edgeData.from, edgeData.to);
            callback(edgeData);
            this.onAddEdge_(edgeData);
          }
        },

        deleteEdge: (params, callback) => {
          if (!params) {
            return;
          }
          if (params.edges && params.edges.length == 1) {
            this.deleteEdge_(params.edges[0]).then(() => callback(params));
          }
        }
      }
    };

    // initialize your network!
    return new vis.Network(container, data, options);
  }

  private createEdgeId(from: string, to: string): string {
    return from + ',' + to;
  }

  private onGraphClicked_(params: any) {
    if (!params) {
      return;
    }
    if (params.nodes && params.nodes.length == 1) {
      this.onNodeClicked_(params.nodes[0]);
    } else if (params.edges && params.edges.length == 1) {
      this.onEdgeClicked_(params.edges[0]);
    }
  }

  private onNodeClicked_(nodeKey: String) {
  }

  private onEdgeClicked_(edgeKey: String) {
  }

  openAddNodeDialog(addNodeCallback: any) {
    let dialogRef = this._dialog.open(AddNodeDialog);
    dialogRef.afterClosed().subscribe(node => {
      if (node) {
        this.gapi_.addNode(node.name, node.description).then((data: any) => {
          const knowledgeNode = data.result;
          const nodeData = {
            id: knowledgeNode.websafeKey,
            label: knowledgeNode.name,
          }
          addNodeCallback(nodeData);
        }, (error) => this._snackbar.open('Failed to add a node', 'DISMISS'));
      }
    })
  }

  private onAddEdge_(edgeData: Edge) {
    this.gapi_.addEdge(edgeData.from, edgeData.to).then(()=>{}, (error) => {
      this._snackbar.open('Failed to add an edge', 'DISMISS');
      this.knowledgeGraph.selectEdges([edgeData.id]);
      this.knowledgeGraph.deleteSelected();
    });
  }

  private deleteEdge_(edgeId: string): Promise<any> {
    const nodeKeys = edgeId.split(',');
    return this.gapi_.deleteEdge(nodeKeys[0], nodeKeys[1]);
  }
}