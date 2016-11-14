package main.java.run.sites.domain;

import com.google.api.server.spi.config.AnnotationBoolean;
import com.google.api.server.spi.config.ApiResourceProperty;
import com.google.common.base.Preconditions;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;

import java.net.MalformedURLException;
import java.net.URL;

import main.java.run.sites.form.SiteForm;

import static main.java.run.sites.service.OfyService.ofy;

/**
 * Site class stores site information.
 */
@Entity
@Cache
public class Site {
  /**
   * The name of the site.
   */
  @Id
  private String name;

  /**
   * The source URL of the site.
   */
  private URL source;

  /**
   * The description of the site.
   */
  private String description;

  /**
   * The latest update time.
   */
  @Index
  private DateTime updatedOn;

  /**
   * The userId of the owner.
   */
  @Index
  @ApiResourceProperty(ignored = AnnotationBoolean.TRUE)
  private String ownerUserId;

  /**
   * Just making the default constructor private.
   */
  private Site() {
  }

  public Site(final String ownerUserId,
              final SiteForm siteForm) {
    Preconditions.checkNotNull(siteForm.getName(), "The name is required");
    Preconditions.checkNotNull(siteForm.getSource(), "The source is required");
    this.ownerUserId = ownerUserId;
    updateWithSiteForm(siteForm);
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

  public String getUpdatedOn() {
    return updatedOn.toString(ISODateTimeFormat.dateTime());
  }

  // Get a String version of the key
  public String getWebsafeKey() {
    return Key.create(Site.class, name).getString();
  }

  public String getOwnerUserId() {
    return ownerUserId;
  }

  /**
   * Returns owner's display name.
   *
   * @return owner's display name. If there is no Profile, return his/her userId.
   */
  public String getOwnerDisplayName() {
    Profile owner = ofy().load().type(Profile.class).id(ownerUserId).now();
    if (owner == null) {
      return ownerUserId;
    } else {
      return owner.getDisplayName();
    }
  }

  /**
   * Updates the Site with SiteForm.
   * This method is used upon object creation as well as updating existing Sites.
   *
   * @param siteForm contains form data sent from the client.
   */
  public void updateWithSiteForm(SiteForm siteForm) {
    try {
      this.name = siteForm.getName();
      if (this.name.isEmpty() || this.name.charAt(0) == '_') {
        throw new IllegalArgumentException("Illegal name: " + this.name);
      }
      this.source = new URL(siteForm.getSource());
      this.description = siteForm.getDescription();
      this.updatedOn = DateTime.now();
    } catch (MalformedURLException e) {
      throw new IllegalArgumentException("Illegal URL: " + siteForm.getSource());
    }
  }

  @Override
  public String toString() {
    StringBuilder stringBuilder = new StringBuilder("")
        .append("Name: " + name + "\n")
        .append("Source: " + source + "\n");
    return stringBuilder.toString();
  }
}
