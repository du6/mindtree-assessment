package main.java.run.sites.form;

import java.net.URL;

/**
 * A simple Java object (POJO) representing a Site form sent from the client.
 */
public class SiteForm {
  /**
   * The name of the site.
   */
  private String name;

  /**
   * The source of the site.
   */
  private URL source;

  /**
   * The description of the site.
   */
  private String description;

  private SiteForm() {}

  /**
   * Public constructor is solely for Unit Test.
   * @param name
   * @param source
   * @param description
   */
  public SiteForm(String name, URL source, String description) {
    this.name = name;
    this.source = source;
    this.description = description;
  }

  public String getName() {
    return name;
  }

  public String getDescription() {
    return description;
  }

  public String getSource() {
    return source.toString();
  }
}
