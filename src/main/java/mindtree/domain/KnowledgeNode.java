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
   * The child nodes' web safe keys which the current node depends on.
   */
  private Set<String> children = new HashSet<>();

  /**
   * The parent nodes' web safe keys who depend on the current node.
   */
  private Set<String> parents = new HashSet<>();

  /**
   * //TODO(du6): use builder pattern
   * Constructor.
   * @param name the name of the node
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

  /**
   * Add a child
   * @param childKey
   */
  public void addChild(String childKey) {
    this.children.add(childKey);
  }

  /**
   * Delete a child
   * @param childKey
   */
  public void deleteChild(String childKey) {
    this.children.remove(childKey);
  }

  /**
   * Add a parent
   * @param parentKey
   */
  public void addParent(String parentKey) {
    this.parents.add(parentKey);
  }

  /**
   * Delete a parent
   * @param parentKey
   */
  public void deleteParent(String parentKey) {
    this.parents.remove(parentKey);
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

  public Set<String> getChildren() {
    return children;
  }

  public Set<String> getParents() {
    return parents;
  }

  public Long getId() {
    return id;
  }

  private KnowledgeNode() {}
}
