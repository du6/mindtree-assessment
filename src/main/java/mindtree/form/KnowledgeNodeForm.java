package main.java.mindtree.form;

/**
 * Pojo representing a KnowledgeNode form on the client side.
 */

public class KnowledgeNodeForm implements MindTreeForm {
  /**
   * The name of the node.
   */
  private String name;

  /**
   * The description of the node.
   */
  private String description;

  public String getName() {
    return name;
  }

  public String getDescription() {
    return description;
  }
}
