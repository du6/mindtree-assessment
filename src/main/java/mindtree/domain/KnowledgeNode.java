package main.java.mindtree.domain;

import com.google.api.server.spi.config.AnnotationBoolean;
import com.google.api.server.spi.config.ApiResourceProperty;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

import main.java.mindtree.form.KnowledgeNodeForm;

/**
 * The knowledge node entity for the knowledge graph.
 */
@Entity
public class KnowledgeNode {
  /**
   * Use automatic id assignment.
   */
  @Id
  @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
  private Long id;

  /**
   * The name of the node.
   */
  @Index
  private String name;

  /**
   * The description of the node
   */
  private String description;

  /**
   * The user id who created the node.
   */
  @Index
  @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
  private String createdBy;

  /**
   * Constructor.
   * @param id the id of the node
   * @param name the name of the node
   * @param description the description of the node
   * @param createdBy the user id who creates the node
   */
  public KnowledgeNode(
      Long id,
      String name,
      String description,
      String createdBy) {
    this.id = id;
    this.name = name;
    this.description = description;
    this.createdBy = createdBy;
  }

  public void updateWithKnowledgeNodeForm(KnowledgeNodeForm knowledgeNodeForm) {
    this.name = knowledgeNodeForm.getName();
    this.description = knowledgeNodeForm.getDescription();
  }

  // Get a String version of the key
  public String getWebsafeKey() {
    return Key.create(KnowledgeNode.class, this.id).getString();
  }

  public String getName() {
    return name;
  }

  public String getDescription() {
    return description;
  }

  public String getCreatedBy() {
    return createdBy;
  }

  public Long getId() {
    return id;
  }

  private KnowledgeNode() {}
}
