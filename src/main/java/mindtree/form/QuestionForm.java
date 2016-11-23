package main.java.mindtree.form;

import java.util.List;

/**
 * This question is created for demo purpose only. We should consider use third party question
 * editor such as learnosity.
 * For demo only supports single option questions.
 */
public class QuestionForm implements MindTreeForm {
  /**
   * The question description.
   */
  private String description;

  /**
   * The possible options.
   */
  private List<String> options;

  /**
   * The correct answer.
   */
  private int answer;

  public String getDescription() {
    return description;
  }

  public List<String> getOptions() {
    return options;
  }

  public int getAnswer() {
    return answer;
  }
}
