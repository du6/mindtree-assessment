package main.java.mindtree.domain;

import com.google.api.server.spi.config.AnnotationBoolean;
import com.google.api.server.spi.config.ApiResourceProperty;
import com.google.appengine.api.datastore.Query;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

import java.net.MalformedURLException;
import java.net.URL;

import main.java.mindtree.form.QuizForm;

/**
 * The quiz entity.
 */
@Entity
@Cache
public class Quiz extends MindTreeEntity<Quiz, QuizForm> {
  private enum Status {
    ACTIVE,
    DRAFT,
    EXPIRED,
  }

  // Only get active quiz
  public static Query.Filter activeQuizFilter() {
    return new Query.FilterPredicate("status", Query.FilterOperator.EQUAL, Status.ACTIVE.toString());
  }

  /**
   * Use automatic id assignment.
   */
  @Id
  @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
  private Long id;

  /**
   * The name of the quiz.
   */
  @Index
  private String name;

  /**
   * The status of the quiz.
   */
  @Index
  private Status status;

  /**
   * The external URL of the quiz.
   */
  private URL url;

  /**
   * The description of the quiz.
   */
  private String description;

  /**
   * The user id who created the edge.
   */
  @Index
  @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
  private String createdBy;

  /**
   * Constructor.
   * @param id the id of the quiz
   * @param createdBy the user id who creates the quiz
   * @param quizForm the client side form
   * @throws MalformedURLException when url is not valid
   */
  public Quiz(
      Long id,
      String createdBy,
      QuizForm quizForm) {
    this.id = id;
    this.createdBy = createdBy;
    this.status = Status.ACTIVE;
    this.updateWithForm(quizForm);
  }

  @Override
  public void updateWithForm(QuizForm quizForm) {
    try {
      this.name = quizForm.getName();
      this.description = quizForm.getDescription();
      if (quizForm.getUrl() != null) {
        this.url = new URL(quizForm.getUrl());
      }
    } catch (MalformedURLException e) {
      throw new RuntimeException(e);
    }
  }

  // Get a String version of the key
  @Override
  public String getWebsafeKey() {
    return Key.create(Quiz.class, this.id).getString();
  }

  // Delete a quiz by setting the status expired
  public void delete() {
    this.status = Status.EXPIRED;
  }

  public Long getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public URL getUrl() {
    return url;
  }

  public String getDescription() {
    return description;
  }

  public Status getStatus() {
    return status;
  }

  public String getCreatedBy() {
    return createdBy;
  }

  private Quiz() {}
}
