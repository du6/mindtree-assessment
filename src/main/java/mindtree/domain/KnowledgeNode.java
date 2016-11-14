package main.java.mindtree.domain;

import com.google.api.server.spi.config.AnnotationBoolean;
import com.google.api.server.spi.config.ApiResourceProperty;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

import java.util.HashSet;
import java.util.Set;

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
  private Long id;

  /**
   * The name of the node.
   */
  @Index
  private String name;

  /**
   * The user id who created the node.
   */
  @Index
  @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
  private String createdBy;

  /**
   * The child nodes which the current node depends on.
   */
  private Set<String> children;

  /**
   * Constructor.
   * @param name the name of the node
   */
  public KnowledgeNode(Long id, String name, String createdBy) {
    this.id = id;
    this.name = name;
    this.createdBy = createdBy;
    this.children = new HashSet<>();
  }

  public void updateWithKnowledgeNodeForm(KnowledgeNodeForm knowledgeNodeForm) {
    this.name = knowledgeNodeForm.getName();
  }

  /**
   * Add a child
   * @param childId
   */
  public void addChild(String childId) {
    this.children.add(childId);
  }

  // Get a String version of the key
  public String getWebsafeKey() {
    return Key.create(KnowledgeNode.class, this.id).getString();
  }

  public String getName() {
    return name;
  }

  public String getCreatedBy() {
    return createdBy;
  }

  public Set<String> getChildren() {
    return children;
  }

  public Long getId() {
    return id;
  }

  private KnowledgeNode() {}
}
