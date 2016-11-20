package main.java.mindtree.domain;

import com.google.api.server.spi.config.AnnotationBoolean;
import com.google.api.server.spi.config.ApiResourceProperty;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

import main.java.mindtree.form.QuizTagForm;

/**
 * The quiz tag entity connecting a quiz and a knowledge node.
 */
@Entity
public class QuizTag extends MindTreeEntity<QuizTag, QuizTagForm> {
  /**
   * Use automatic id assignment.
   */
  @Id
  @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
  private Long id;

  /**
   * The quiz key.
   */
  @Index
  private String quizKey;

  /**
   * The knowledge node key.
   */
  @Index
  private String nodeKey;

  /**
   * How strong the quiz testing the knowledge node.
   * Default is 1.0.
   */
  @Index
  private double strength;

  /**
   * The user id who created the tag.
   */
  @Index
  @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
  private String createdBy;

  /**
   * Constructor
   * @param id the id of the edge
   * @param createdBy the user id who creates the quiz
   * @param tagForm the client side form
   */
  public QuizTag(
      Long id,
      String createdBy,
      QuizTagForm tagForm) {
    this.id = id;
    this.createdBy = createdBy;
    this.strength = 1.0;
    this.updateWithForm(tagForm);
  }

  @Override
  public void updateWithForm(QuizTagForm tagForm) {
    this.quizKey = tagForm.getQuizKey();
    this.nodeKey = tagForm.getNodeKey();
  }

  // Get a String version of the key
  @Override
  public String getWebsafeKey() {
    return Key.create(QuizTag.class, this.id).getString();
  }

  public Long getId() {
    return id;
  }

  public String getQuizKey() {
    return quizKey;
  }

  public String getNodeKey() {
    return nodeKey;
  }

  public double getStrength() {
    return strength;
  }

  public String getCreatedBy() {
    return createdBy;
  }

  private QuizTag() {}
}
