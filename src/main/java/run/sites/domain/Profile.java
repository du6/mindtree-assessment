package main.java.run.sites.domain;

import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;

@Entity
@Cache
public class Profile {
  /**
   *  Use userId as the datastore key.
   */
  @Id
  private String userId;

  /**
   * Any string user wants us to display him/her on this system.
   */
  private String displayName;

  /**
   * User's main e-mail address.
   */
  private String mainEmail;

  /**
   * Public constructor for Profile.
   * @param userId The user id, obtained from the email
   * @param displayName Any string user wants us to display him/her on this system.
   * @param mainEmail User's main e-mail address.
   *
   */
  public Profile (String userId, String displayName, String mainEmail) {
      this.userId = userId;
      this.displayName = displayName;
      this.mainEmail = mainEmail;
  }

  /**
   * Getter for userId.
   * @return userId.
   */
  public String getUserId() {
      return userId;
  }

  /**
   * Getter for displayName.
   * @return displayName.
   */
  public String getDisplayName() {
      return displayName;
  }

  /**
   * Getter for mainEmail.
   * @return mainEmail.
   */
  public String getMainEmail() {
      return mainEmail;
  }

  /**
   * Update the Profile with the given displayName
   * @param displayName
   */
  public void update(String displayName) {
      if (displayName != null) {
          this.displayName = displayName;
      }
  }

  /**
   * Just making the default constructor private.
   */
  private Profile() {}
}
