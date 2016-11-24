package main.java.mindtree.domain;

import com.google.api.server.spi.config.AnnotationBoolean;
import com.google.api.server.spi.config.ApiResourceProperty;
import com.google.appengine.api.datastore.Query;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

import java.util.ArrayList;
import java.util.List;

import main.java.mindtree.form.QuestionForm;

/**
 * This question is created for demo purpose only. We should consider use third party question
 * editor such as learnosity.
 */
@Entity
@Cache
public class Question extends MindTreeEntity<Question, QuestionForm> {
  private enum Status {
    ACTIVE,
    DRAFT,
    EXPIRED,
  }

  // Only get active questions
  public static Query.Filter activeQuestionFilter() {
    return new Query.FilterPredicate("status", Query.FilterOperator.EQUAL, Status.ACTIVE.toString());
  }

  /**
   * Use automatic id assignment.
   */
  @Id
  @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
  private Long id;

  /**
   * The description of the question.
   */
  private String description;

  /**
   * The status of the question.
   */
  @Index
  private Status status;

  /**
   * The user id who created the question.
   */
  @Index
  @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
  private String createdBy;

  /**
   * The possible options.
   */
  private List<String> options = new ArrayList<>();

  /**
   * The correct answer.
   */
  private int answer;

  /**
   * Constructor
   * @param id id of the question
   * @param createdBy the user id who creates the question
   * @param questionForm the client side form
   */
  public Question(Long id, String createdBy, QuestionForm questionForm) {
    this.id = id;
    this.createdBy = createdBy;
    this.status = Status.ACTIVE;
    this.updateWithForm(questionForm);
  }

  @Override
  public void updateWithForm(QuestionForm questionForm) {
    this.description = questionForm.getDescription();
    this.options = new ArrayList<>(questionForm.getOptions());
    this.answer = questionForm.getAnswer();
  }

  @Override
  public String getWebsafeKey() {
    return Key.create(Question.class, this.id).getString();
  }

  /**
   * Deleting a question makes the status expired.
   */
  public void delete() {
    this.status = Status.EXPIRED;
  }

  public Long getId() {
    return id;
  }

  public String getDescription() {
    return description;
  }

  public String getCreatedBy() {
    return createdBy;
  }

  public List<String> getOptions() {
    return options;
  }

  public int getAnswer() {
    return answer;
  }

  public Status getStatus() {
    return status;
  }

  private Question() {}
}
