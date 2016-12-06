package main.java.mindtree.form;

import com.google.common.collect.ImmutableSet;

import java.util.Set;

/**
 * Pojo representing a ReleasedQuestions form on the client side.
 */
public class ReleasedQuestionsForm implements MindTreeForm {
  /**
   * The list of question keys to release.
   */
  private Set<String> questionKeys;

  public ImmutableSet<String> getQuestionKeys() {
    return ImmutableSet.copyOf(questionKeys);
  }
}
