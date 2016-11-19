package main.java.mindtree.domain;

import com.google.api.server.spi.config.AnnotationBoolean;
import com.google.api.server.spi.config.ApiResourceProperty;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

import main.java.mindtree.form.EdgeForm;

/**
 * The edge entity connection parent and child knowledge node.
 */
@Entity
public class Edge {
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
   * @param parentKey the web safe key of parent node
   * @param childKey the web safe key of the child node
   */
  public Edge (
      Long id,
      String parentKey,
      String childKey,
      String createdBy) {
    this.id = id;
    this.parentKey = parentKey;
    this.childKey = childKey;
    this.createdBy = createdBy;
    this.strength = 1.0;
  }

  public void updateWithEdgeForm(EdgeForm edgeForm) {
    this.parentKey = edgeForm.getParentKey();
    this.childKey = edgeForm.getChildKey();
  }

  // Get a String version of the key
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
