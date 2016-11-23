package main.java.mindtree.form;

/**
 * Pojo representing a Question Tag form on the client side.
 */
public class QuestionTagForm implements MindTreeForm {
  /**
   * The web safe key of the quiz.
   */
  private String questionKey;

  /**
   * The web safe key of the knowledge node.
   */
  private String nodeKey;

  public String getQuestionKey() {
    return questionKey;
  }

  public String getNodeKey() {
    return nodeKey;
  }
}
