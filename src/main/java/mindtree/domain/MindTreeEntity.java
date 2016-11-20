package main.java.mindtree.domain;

/**
 * Abstract class for data store Entity.
 */
public abstract class MindTreeEntity<E, F> {
  public abstract String getWebsafeKey();
  public abstract void updateWithForm(F form);
}
