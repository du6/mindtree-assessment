package main.java.mindtree.domain;

import com.google.api.server.spi.config.AnnotationBoolean;
import com.google.api.server.spi.config.ApiResourceProperty;
import com.google.common.collect.ImmutableSet;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

import java.util.HashSet;
import java.util.Set;

import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;

import main.java.mindtree.form.ReleasedQuestionsForm;

/**
 * The quiz entity.
 */
@Entity
@Cache
public class ReleasedQuestions extends MindTreeEntity<ReleasedQuestions, ReleasedQuestionsForm> {
  /**
   * Use automatic id assignment.
   */
  @Id
  @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
  private Long id;

  /**
   * The user id who created the release.
   */
  @Index
  @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
  private String createdBy;

  /**
   * The latest update time.
   */
  @Index
  private DateTime updatedOn;

  /**
   * The question keys to release.
   */
  private Set<String> questionKeys = new HashSet<>();

  /**
   * Constructor.
   * @param id the id of the quiz
   * @param createdBy the user id who creates the quiz
   * @param releasedQuestionsForm the client side form
   */
  public ReleasedQuestions(
      Long id,
      String createdBy,
      ReleasedQuestionsForm releasedQuestionsForm) {
    this.id = id;
    this.createdBy = createdBy;
    this.updateWithForm(releasedQuestionsForm);
  }

  @Override
  public void updateWithForm(ReleasedQuestionsForm releasedQuestionsForm) {
    this.questionKeys.addAll(releasedQuestionsForm.getQuestionKeys());
    this.updatedOn = DateTime.now();
  }

  // Get a String version of the key
  @Override
  public String getWebsafeKey() {
    return Key.create(ReleasedQuestions.class, this.id).getString();
  }

  public Long getId() {
    return id;
  }

  public String getCreatedBy() {
    return createdBy;
  }

  public ImmutableSet<String> getQuestionKeys() {
    return ImmutableSet.copyOf(questionKeys);
  }

  public String getUpdatedOn() {
    return updatedOn.toString(ISODateTimeFormat.dateTime());
  }

  private ReleasedQuestions() {}
}
