export class Question {
  description: string;
  options: string[];
  answer: number;
  websafeKey?: string;
}

export class QuestionTag {
  nodeKey: string;
  questionKey: string;
  websafeKey?: string;
}