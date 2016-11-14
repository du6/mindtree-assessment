package main.java.run.sites.spi;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiMethod.HttpMethod;
import com.google.api.server.spi.config.DefaultValue;
import com.google.api.server.spi.response.ConflictException;
import com.google.api.server.spi.response.ForbiddenException;
import com.google.api.server.spi.response.NotFoundException;
import com.google.api.server.spi.response.UnauthorizedException;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.google.appengine.api.users.User;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.Work;
import com.googlecode.objectify.cmd.Query;

import java.util.List;
import java.util.logging.Logger;

import javax.inject.Named;
import javax.persistence.EntityExistsException;

import main.java.run.sites.Constants;
import main.java.run.sites.domain.Announcement;
import main.java.run.sites.domain.AppEngineUser;
import main.java.run.sites.domain.Profile;
import main.java.run.sites.domain.Site;
import main.java.run.sites.form.ProfileForm;
import main.java.run.sites.form.SiteForm;

import static main.java.run.sites.service.OfyService.ofy;

/**
 * Defines sites run APIs.
 */
@Api(name = "sitesRunApi", version = "v1",
    scopes = { Constants.EMAIL_SCOPE }, clientIds = {
    Constants.WEB_CLIENT_ID,
    Constants.API_EXPLORER_CLIENT_ID },
    description = "API for the Sites Run Backend application.")
public class SitesRunApi {

  private static final DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
  private  static final String ANONYMOUS_USER_ID = "anonymous@sites.run";
  private  static final String DEFAULT_QUERY_LIMIT = "10";
  private static final Logger LOG = Logger.getLogger(SitesRunApi.class.getName());

  private static String extractDefaultDisplayNameFromEmail(String email) {
    return email == null ? null : email.substring(0, email.indexOf("@"));
  }

  private static Profile getProfileFromUser(User user, String userId) {
    // First fetch it from the datastore.
    Profile profile = ofy().load().key(
        Key.create(Profile.class, userId)).now();
    if (profile == null) {
      // Create a new Profile if not exist.
      String email = user.getEmail();
      profile = new Profile(userId,
          extractDefaultDisplayNameFromEmail(email), email);
    }
    return profile;
  }

  /**
   * This is an ugly workaround for null userId for Android clients.
   *
   * @param user A User object injected by the cloud endpoints.
   * @return the App Engine userId for the user.
   */
  private static String getUserId(User user) {
    String userId = user.getUserId();
    if (userId == null) {
      LOG.info("userId is null, so trying to obtain it from the datastore.");
      AppEngineUser appEngineUser = new AppEngineUser(user);
      ofy().save().entity(appEngineUser).now();
      // Begin new session for not using session cache.
      Objectify objectify = ofy().factory().begin();
      AppEngineUser savedUser = objectify.load().key(appEngineUser.getKey()).now();
      userId = savedUser.getUser().getUserId();
      LOG.info("Obtained the userId: " + userId);
    }
    return userId;
  }

  /**
   * Just a wrapper for Boolean.
   */
  public static class WrappedBoolean {

    private final Boolean result;

    public WrappedBoolean(Boolean result) {
      this.result = result;
    }

    public Boolean getResult() {
      return result;
    }
  }

  /**
   * A wrapper class that can embrace a generic result or some kind of exception.
   *
   * Use this wrapper class for the return type of objectify transaction.
   * <pre>
   * {@code
   * // The transaction that returns Site object.
   * TxResult<Site> result = ofy().transact(new Work<TxResult<Site>>() {
   *     public TxResult<Site> run() {
   *         // Code here.
   *         // To throw 404
   *         return new TxResult<>(new NotFoundException("No such site"));
   *         // To return a site.
   *         Site site = somehow.getSite();
   *         return new TxResult<>(site);
   *     }
   * }
   * // Actually the NotFoundException will be thrown here.
   * return result.getResult();
   * </pre>
   *
   * @param <ResultType> The type of the actual return object.
   */
  private static class TxResult<ResultType> {

    private ResultType result;

    private Throwable exception;

    private TxResult(ResultType result) {
      this.result = result;
    }

    private TxResult(Throwable exception) {
      if (exception instanceof NotFoundException ||
          exception instanceof ForbiddenException ||
          exception instanceof ConflictException) {
        this.exception = exception;
      } else {
        throw new IllegalArgumentException("Exception not supported.");
      }
    }

    private ResultType getResult() throws NotFoundException, ForbiddenException, ConflictException {
      if (exception instanceof NotFoundException) {
        throw (NotFoundException) exception;
      }
      if (exception instanceof ForbiddenException) {
        throw (ForbiddenException) exception;
      }
      if (exception instanceof ConflictException) {
        throw (ConflictException) exception;
      }
      return result;
    }
  }

  /**
   * Returns a Profile object associated with the given user object. The cloud endpoints system
   * automatically inject the User object.
   *
   * @param user A User object injected by the cloud endpoints.
   * @return Profile object.
   * @throws UnauthorizedException when the User object is null.
   */
  @ApiMethod(name = "getProfile", path = "profile", httpMethod = HttpMethod.GET)
  public Profile getProfile(final User user) throws UnauthorizedException {
    if (user == null) {
      throw new UnauthorizedException("Authorization required");
    }
    return ofy().load().key(Key.create(Profile.class, getUserId(user))).now();
  }

  /**
   * Creates or updates a Profile object associated with the given user object.
   *
   * @param user A User object injected by the cloud endpoints.
   * @param profileForm A ProfileForm object sent from the client form.
   * @return Profile object just created.
   * @throws UnauthorizedException when the User object is null.
   */
  @ApiMethod(name = "saveProfile", path = "profile", httpMethod = HttpMethod.POST)
  public Profile saveProfile(final User user, final ProfileForm profileForm)
      throws UnauthorizedException {
    if (user == null) {
      throw new UnauthorizedException("Authorization required");
    }
    String displayName = profileForm.getDisplayName();

    Profile profile = ofy().load().key(Key.create(Profile.class, getUserId(user))).now();
    if (profile == null) {
      // Populate displayName and teeShirtSize with the default values if null.
      if (displayName == null) {
        displayName = extractDefaultDisplayNameFromEmail(user.getEmail());
      }
      profile = new Profile(getUserId(user), displayName, user.getEmail());
    } else {
      profile.update(displayName);
    }
    ofy().save().entity(profile).now();
    return profile;
  }

  /**
   * Creates a new Site object and stores it to the datastore.
   *
   * @param user A user who invokes this method, null when the user is not signed in.
   * @param siteForm A SiteForm object representing user's inputs.
   * @return A newly created Site Object.
   * @throws UnauthorizedException when the user is not signed in.
   */
  @ApiMethod(name = "createSite", path = "createSite", httpMethod = HttpMethod.POST)
  public Site createSite(final User user, final SiteForm siteForm)
      throws UnauthorizedException {
    checkEntityExistence(siteForm.getName());
    if (user == null) {
      throw new UnauthorizedException("Authorization required");
    }
    final String userId = getUserId(user);
    // Start a transaction.
    Site site = ofy().transact(new Work<Site>() {
      @Override
      public Site run() {
        // Fetch user's Profile.
        Profile profile = getProfileFromUser(user, userId);
        Site site = new Site(userId, siteForm);
        // Save Site and Profile.
        ofy().save().entities(site, profile).now();
        return site;
      }
    });
    return site;
  }

  /**
   * Creates a new Site object without logging in and stores it to the datastore.
   *
   * @param user A user who invokes this method, null when the user is not signed in.
   * @param siteForm A SiteForm object representing user's inputs.
   * @return A newly created Site Object.
   */
  @ApiMethod(
      name = "createSiteAnonymously",
      path = "createSiteAnonymously",
      httpMethod = HttpMethod.POST)
  public Site createSiteAnonymously(final User user, final SiteForm siteForm) {
    checkEntityExistence(siteForm.getName());
    // Start a transaction.
    Site site = ofy().transact(new Work<Site>() {
      @Override
      public Site run() {
        Site site = new Site(ANONYMOUS_USER_ID, siteForm);
        // Save Site.
        ofy().save().entity(site).now();
        return site;
      }
    });
    return site;
  }

  /**
   * Updates the existing Site with the given siteId.
   *
   * @param user A user who invokes this method, null when the user is not signed in.
   * @param siteForm A SiteForm object representing user's inputs.
   * @param websafeSiteKey The String representation of the Site key.
   * @return Updated Site object.
   * @throws UnauthorizedException when the user is not signed in.
   * @throws NotFoundException when there is no Site with the given siteId.
   * @throws ForbiddenException when the user is not the owner of the Site.
   */
  @ApiMethod(
      name = "updateSite",
      path = "updateSite/{websafeSiteKey}",
      httpMethod = HttpMethod.PUT
  )
  public Site updateSite(final User user, final SiteForm siteForm,
                                     @Named("websafeSiteKey")
                                     final String websafeSiteKey)
      throws UnauthorizedException, NotFoundException, ForbiddenException, ConflictException {
    // If not signed in, throw a 401 error.
    if (user == null) {
      throw new UnauthorizedException("Authorization required");
    }
    final String userId = getUserId(user);
    // Update the site with the siteForm sent from the client.
    TxResult<Site> result = ofy().transact(new Work<TxResult<Site>>() {
      @Override
      public TxResult<Site> run() {
        // If there is no Site with the id, throw a 404 error.
        Key<Site> siteKey = Key.create(websafeSiteKey);
        Site site = ofy().load().key(siteKey).now();
        if (site == null) {
          return new TxResult<>(
              new NotFoundException("No Site found with the key: "
                  + websafeSiteKey));
        }
        if (site.getOwnerUserId().equals(ANONYMOUS_USER_ID)) {
          return new TxResult<>(
              new ForbiddenException("Sites created anonymously cannot be edited."));
        }
        // If the user is not the owner, throw a 403 error.
        Profile profile = ofy().load().key(Key.create(Profile.class, userId)).now();
        if (profile == null ||
            !site.getOwnerUserId().equals(userId)) {
          return new TxResult<>(
              new ForbiddenException("Only the owner can update the site."));
        }
        if (!site.getName().equals(siteForm.getName())) {
          checkEntityExistence(siteForm.getName());
        }
        site.updateWithSiteForm(siteForm);
        ofy().save().entity(site).now();
        return new TxResult<>(site);
      }
    });
    // NotFoundException or ForbiddenException is actually thrown here.
    return result.getResult();
  }

  /**
   * Returns a Site object with the given siteId.
   *
   * @param websafeSiteKey The String representation of the Site Key.
   * @return a Site object with the given siteId.
   * @throws NotFoundException when there is no Site with the given siteId.
   */
  @ApiMethod(
      name = "getSite",
      path = "getSite/{websafeSiteKey}",
      httpMethod = HttpMethod.GET
  )
  public Site getSite(
      @Named("websafeSiteKey") final String websafeSiteKey)
      throws NotFoundException {
    Key<Site> siteKey = Key.create(websafeSiteKey);
    Site site = ofy().load().key(siteKey).now();
    if (site == null) {
      throw new NotFoundException("No Site found with key: " + websafeSiteKey);
    }
    return site;
  }

  /**
   * Returns a Site object with the given name.
   *
   * @param name
   * @return a Site object with the given name.
   * @throws NotFoundException when there is no Site with the given name.
   */
  @ApiMethod(
      name = "getSiteByName",
      path = "getSiteByName/{name}",
      httpMethod = HttpMethod.GET
  )
  public Site getSiteByName(
      @Named("name") final String name)
      throws NotFoundException {
    Site site = ofy().load().type(Site.class).id(name).now();
    if (site == null) {
      throw new NotFoundException("No Site found with name: " + name);
    };
    return site;
  }

  /**
   * Deletes a Site object with the given siteId.
   * @param user A user who invokes this method, null when the user is not signed in.
   * @param websafeSiteKey The String representation of the Site Key.
   * @throws NotFoundException when there is no Site with the given siteId.
   */
  @ApiMethod(
      name = "deleteSite",
      path = "deleteSite/{websafeSiteKey}",
      httpMethod = HttpMethod.DELETE
  )
  public void deleteSite(
      final User user,
      @Named("websafeSiteKey") final String websafeSiteKey)
      throws NotFoundException, UnauthorizedException, ForbiddenException {
    final String userId = getUserId(user);
    // If not signed in, throw a 401 error.
    if (user == null) {
      throw new UnauthorizedException("Authorization required");
    }
    Key<Site> siteKey = Key.create(websafeSiteKey);
    Site site = ofy().load().key(siteKey).now();
    if (site == null) {
      throw new NotFoundException("No Site found with key: " + websafeSiteKey);
    } else {
      if (site.getOwnerUserId().equals(ANONYMOUS_USER_ID)) {
        throw new ForbiddenException("Sites created anonymously cannot be deleted.");
      }
      // If the user is not the owner, throw a 403 error.
      Profile profile = ofy().load().key(Key.create(Profile.class, userId)).now();
      if (profile == null ||
          !site.getOwnerUserId().equals(userId)) {
        throw new ForbiddenException("Only the owner can delete the site.");
      }
      ofy().delete().key(siteKey).now();
    }
  }

  /**
   * Deletes a Site object with the given siteId.
   * @param user A user who invokes this method, null when the user is not signed in.
   * @param name The name of the site.
   * @throws NotFoundException when there is no Site with the given siteId.
   */
  @ApiMethod(
      name = "deleteSiteByName",
      path = "deleteSiteByName/{name}",
      httpMethod = HttpMethod.DELETE
  )
  public void deleteSiteByName(
      final User user,
      @Named("name") final String name)
      throws NotFoundException, UnauthorizedException, ForbiddenException {
    final String userId = getUserId(user);
    // If not signed in, throw a 401 error.
    if (user == null) {
      throw new UnauthorizedException("Authorization required");
    }
    Site site = ofy().load().type(Site.class).id(name).now();
    if (site == null) {
      throw new NotFoundException("No Site found with name: " + name);
    } else {
      if (site.getOwnerUserId().equals(ANONYMOUS_USER_ID)) {
        throw new ForbiddenException("Sites created anonymously cannot be deleted.");
      }
      // If the user is not the owner, throw a 403 error.
      Profile profile = ofy().load().key(Key.create(Profile.class, userId)).now();
      if (profile == null ||
          !site.getOwnerUserId().equals(userId)) {
        throw new ForbiddenException("Only the owner can delete the site.");
      }
      ofy().delete().type(Site.class).id(name).now();
    }
  }

  /**
   * Returns a list of Sites that the user created.
   * In order to receive the websafeSiteKey via the JSON params, uses a POST method.
   *
   * @param user An user who invokes this method, null when the user is not signed in.
   * @return a list of Sites that the user created.
   * @throws UnauthorizedException when the user is not signed in.
   */
  @ApiMethod(
      name = "getSitesCreated",
      path = "getSitesCreated",
      httpMethod = HttpMethod.POST
  )
  public List<Site> getSitesCreated(
      final User user,
      @Named("limit") @DefaultValue(DEFAULT_QUERY_LIMIT) final int limit)
      throws UnauthorizedException {
    // If not signed in, throw a 401 error.
    if (user == null) {
      throw new UnauthorizedException("Authorization required");
    }
    String userId = getUserId(user);
    return queryByOwner(userId).order("-updatedOn").limit(limit).list();
  }

  /**
   * Returns a list of Sites that the user created anonymously.
   * In order to receive the websafeSiteKey via the JSON params, uses a POST method.
   *
   * @param user An user who invokes this method, null when the user is not signed in.
   * @return a list of Sites that the user created.
   * @throws UnauthorizedException when the user is not signed in.
   */
  @ApiMethod(
      name = "getSitesCreatedAnonymously",
      path = "getSitesCreatedAnonymously",
      httpMethod = HttpMethod.POST
  )
  public List<Site> getSitesCreatedAnonymously(
      final User user,
      @Named("limit") @DefaultValue(DEFAULT_QUERY_LIMIT) final int limit)
      throws UnauthorizedException {
    return queryByOwner(ANONYMOUS_USER_ID).order("-updatedOn").limit(limit).list();
  }

  @ApiMethod(
      name = "getAnnouncement",
      path = "announcement",
      httpMethod = HttpMethod.GET
  )
  public Announcement getAnnouncement() {
    MemcacheService memcacheService = MemcacheServiceFactory.getMemcacheService();
    Object message = memcacheService.get(Constants.MEMCACHE_ANNOUNCEMENTS_KEY);
    if (message != null) {
      return new Announcement(message.toString());
    }
    return null;
  }

  private static Query<Site> queryByOwner(final String userId) {
    final Filter ownerFilter = new FilterPredicate("ownerUserId", FilterOperator.EQUAL, userId);
    return ofy().load().type(Site.class).filter(ownerFilter);
  }

  private static void checkEntityExistence(String name) {
    Site entity = ofy().load().type(Site.class).id(name).now();
    if (entity != null) {
      throw new EntityExistsException("Site name already exists: " + name);
    }
  }
}