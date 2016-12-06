package main.java.mindtree.form;

/**
 * Pojo representing a Quiz form on the client side.
 */
public class QuizForm implements MindTreeForm {
  /**
   * The name of the quiz.
   */
  private String name;

  /**
   * The description of the quiz.
   */
  private String description;

  /**
   * The external url of the quiz.
   */
  private String url;

  public String getName() {
    return name;
  }

  public String getDescription() {
    return description;
  }

  public String getUrl() {
    return url;
  }
}
