export class KnowledgeNode {
  name?: string;
  description?: string;
  websafeKey: string;
}

export class KnowledgeEdge {
  parentKey: string;
  childKey: string;
  websafeKey: string;
}
