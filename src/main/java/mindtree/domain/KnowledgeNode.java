package main.java.mindtree.domain;

import com.google.api.server.spi.config.AnnotationBoolean;
import com.google.api.server.spi.config.ApiResourceProperty;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

import main.java.mindtree.form.KnowledgeNodeForm;

/**
 * The knowledge node entity for the knowledge graph.
 */
@Entity
@Cache
public class KnowledgeNode extends MindTreeEntity<KnowledgeNode, KnowledgeNodeForm> {
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
   * @param createdBy the user id who creates the node
   * @param nodeForm the client side form
   */
  public KnowledgeNode(
      Long id,
      String createdBy,
      KnowledgeNodeForm nodeForm) {
    this.id = id;
    this.createdBy = createdBy;
    this.updateWithForm(nodeForm);
  }

  @Override
  public void updateWithForm(KnowledgeNodeForm knowledgeNodeForm) {
    this.name = knowledgeNodeForm.getName();
    this.description = knowledgeNodeForm.getDescription();
  }

  // Get a String version of the key
  @Override
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
