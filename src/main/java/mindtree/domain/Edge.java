package main.java.mindtree.domain;

import com.google.api.server.spi.config.AnnotationBoolean;
import com.google.api.server.spi.config.ApiResourceProperty;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

import main.java.mindtree.form.EdgeForm;

/**
 * The edge entity connection parent and child knowledge node.
 */
@Entity
@Cache
public class Edge extends MindTreeEntity<Edge, EdgeForm> {
  /**
   * Use automatic id assignment.
   */
  @Id
  @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
  private Long id;

  /**
   * The parent node key.
   */
  @Index
  private String parentKey;

  /**
   * The child node key.
   */
  @Index
  private String childKey;

  /**
   * How strong the parent node depends on the child node.
   * Default is 1.0.
   */
  @Index
  private double strength;

  /**
   * The user id who created the edge.
   */
  @Index
  @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
  private String createdBy;

  /**
   * Constructor
   * @param id the id of the edge
   * @param createdBy the user id who creates the node
   * @param edgeForm the client side form
   */
  public Edge (
      Long id,
      String createdBy,
      EdgeForm edgeForm) {
    this.id = id;
    this.createdBy = createdBy;
    this.strength = 1.0;
    this.updateWithForm(edgeForm);
  }

  @Override
  public void updateWithForm(EdgeForm edgeForm) {
    this.parentKey = edgeForm.getParentKey();
    this.childKey = edgeForm.getChildKey();
  }

  // Get a String version of the key
  @Override
  public String getWebsafeKey() {
    return Key.create(Edge.class, this.id).getString();
  }

  public Long getId() {
    return id;
  }

  public String getParentKey() {
    return parentKey;
  }

  public String getChildKey() {
    return childKey;
  }

  public double getStrength() {
    return strength;
  }

  public String getCreatedBy() {
    return createdBy;
  }

  private Edge() {}
}
