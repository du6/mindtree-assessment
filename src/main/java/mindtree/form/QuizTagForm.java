package main.java.mindtree.form;

/**
 * Pojo representing a Quiz Tag form on the client side.
 */
public class QuizTagForm {
  /**
   * The web safe key of the quiz.
   */
  private String quizKey;

  /**
   * The web safe key of the knowledge node.
   */
  private String nodeKey;

  public String getQuizKey() {
    return quizKey;
  }

  public String getNodeKey() {
    return nodeKey;
  }
}
