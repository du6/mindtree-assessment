package main.java.mindtree.spi;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiMethod.HttpMethod;
import com.google.api.server.spi.config.DefaultValue;
import com.google.api.server.spi.response.ConflictException;
import com.google.api.server.spi.response.ForbiddenException;
import com.google.api.server.spi.response.NotFoundException;
import com.google.api.server.spi.response.UnauthorizedException;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.users.User;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.Work;
import com.googlecode.objectify.cmd.Query;

import java.util.List;
import java.util.logging.Logger;

import javax.inject.Named;

import main.java.mindtree.Constants;
import main.java.mindtree.domain.KnowledgeNode;
import main.java.mindtree.form.EdgeForm;
import main.java.mindtree.form.KnowledgeNodeForm;
import main.java.mindtree.domain.AppEngineUser;
import main.java.mindtree.domain.Profile;
import main.java.mindtree.form.ProfileForm;

import static main.java.mindtree.service.OfyService.factory;
import static main.java.mindtree.service.OfyService.ofy;

/**
 * Defines mind tree APIs.
 */
@Api(name = "mindTreeApi", version = "v1",
    scopes = { Constants.EMAIL_SCOPE }, clientIds = {
    Constants.WEB_CLIENT_ID,
    Constants.API_EXPLORER_CLIENT_ID },
    description = "API for the Mind Tree Backend application.")
public class MindTreeApi {

  private static final String DEFAULT_QUERY_LIMIT = "10";
  private static final Logger LOG = Logger.getLogger(MindTreeApi.class.getName());

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
   * // The transaction that returns KnowledgeNode object.
   * TxResult<KnowledgeNode> result = ofy().transact(new Work<TxResult<KnowledgeNode>>() {
   *     public TxResult<KnowledgeNode> run() {
   *         // Code here.
   *         // To throw 404
   *         return new TxResult<>(new NotFoundException("No such knowledge node"));
   *         // To return a knowledge node.
   *         KnowledgeNode knowledgeNode = somehow.getKnowledgeNode();
   *         return new TxResult<>(knowledgeNode);
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
   * Creates a new KnowledgeNode object and stores it to the datastore.
   *
   * @param user A user who invokes this method, null when the user is not signed in.
   * @param knowledgeNodeForm A KnowledgeNodeForm object representing user's inputs.
   * @return A newly created KnowledgeNode Object.
   * @throws UnauthorizedException when the user is not signed in.
   */
  @ApiMethod(name = "createKnowledgeNode", path = "createKnowledgeNode", httpMethod = HttpMethod.POST)
  public KnowledgeNode createKnowledgeNode(final User user, final KnowledgeNodeForm knowledgeNodeForm)
      throws UnauthorizedException {
    if (user == null) {
      throw new UnauthorizedException("Authorization required");
    }
    final String userId = getUserId(user);

    // Start a transaction.
    KnowledgeNode knowledgeNode = ofy().transact(new Work<KnowledgeNode>() {
      @Override
      public KnowledgeNode run() {
        // Fetch user's Profile.
        Key<KnowledgeNode> knowledgeNodeKey = factory().allocateId(KnowledgeNode.class);
        KnowledgeNode knowledgeNode =
            new KnowledgeNode(
                knowledgeNodeKey.getId(),
                knowledgeNodeForm.getName(),
                knowledgeNodeForm.getDescription(),
                userId);
        ofy().save().entity(knowledgeNode).now();
        return knowledgeNode;
      }
    });
    return knowledgeNode;
  }

  /**
   * Creates an edge that connects a parent node and a child node
   *
   * @param user A user who invokes this method, null when the user is not signed in.
   * @param edgeForm An EdgeForm object representing user's inputs.
   * @throws NotFoundException when there is no knowledge with the given key.
   * @throws ForbiddenException when the parent and child nodes are the same.
   */
  @ApiMethod(
      name = "createEdge",
      path = "createEdge",
      httpMethod = HttpMethod.POST)
  public void createEdge(final User user, final EdgeForm edgeForm)
      throws NotFoundException, ForbiddenException {
    if (edgeForm.getChildKey().equals(edgeForm.getParentKey())) {
      throw (new ForbiddenException("Cannot create an edge from and to the same node."));
    }

    Key<KnowledgeNode> parentKnowledgeNodeKey = Key.create(edgeForm.getParentKey());
    KnowledgeNode parentNode = ofy().load().key(parentKnowledgeNodeKey).now();
    if (parentNode == null) {
      throw (new NotFoundException("Parent knowledge node not found with the key: "
          + parentKnowledgeNodeKey));
    }

    Key<KnowledgeNode> childKnowledgeNodeKey = Key.create(edgeForm.getChildKey());
    KnowledgeNode childNode = ofy().load().key(childKnowledgeNodeKey).now();
    if (childNode == null) {
      throw (new NotFoundException("Child knowledge node not found with the key: "
          + childKnowledgeNodeKey));
    }

    parentNode.addChild(edgeForm.getChildKey());
    childNode.addParent(edgeForm.getParentKey());
  }

  /**
   * Deletes an edge that connects a parent node and a child node
   *
   * @param user A user who invokes this method, null when the user is not signed in.
   * @param edgeForm An EdgeForm object representing user's inputs.
   * @throws NotFoundException when there is no knowledge with the given key.
   */
  @ApiMethod(
      name = "deleteEdge",
      path = "deleteEdge",
      httpMethod = HttpMethod.POST)
  public void deleteEdge(final User user, final EdgeForm edgeForm) throws NotFoundException {
    Key<KnowledgeNode> parentKnowledgeNodeKey = Key.create(edgeForm.getParentKey());
    KnowledgeNode parentNode = ofy().load().key(parentKnowledgeNodeKey).now();
    if (parentNode == null) {
      throw (new NotFoundException("Parent knowledge node not found with the key: "
          + parentKnowledgeNodeKey));
    }

    Key<KnowledgeNode> childKnowledgeNodeKey = Key.create(edgeForm.getParentKey());
    KnowledgeNode childNode = ofy().load().key(childKnowledgeNodeKey).now();
    if (childNode == null) {
      throw (new NotFoundException("Child knowledge node not found with the key: "
          + childKnowledgeNodeKey));
    }

    parentNode.deleteChild(edgeForm.getChildKey());
    childNode.deleteParent(edgeForm.getParentKey());
  }

  /**
   * Updates the existing knowledge node with the given web safe key.
   *
   * @param user A user who invokes this method, null when the user is not signed in.
   * @param knowledgeNodeForm A KnowledgeNodeForm object representing user's inputs.
   * @param websafeKnowledgeNodeKey The String representation of the KnowledgeNode key.
   * @return Updated KnowledgeNode object.
   * @throws UnauthorizedException when the user is not signed in.
   * @throws NotFoundException when there is no knowledge with the given key.
   * @throws ForbiddenException when the user is not the owner of the knowledge node.
   */
  @ApiMethod(
      name = "updateKnowledgeNode",
      path = "updateKnowledgeNode/{websafeKnowledgeNodeKey}",
      httpMethod = HttpMethod.PUT
  )
  public KnowledgeNode updateKnowledgeNode(final User user, final KnowledgeNodeForm knowledgeNodeForm,
                         @Named("websafeKnowledgeNodeKey")
                         final String websafeKnowledgeNodeKey)
      throws UnauthorizedException, NotFoundException, ForbiddenException, ConflictException {
    // If not signed in, throw a 401 error.
    if (user == null) {
      throw new UnauthorizedException("Authorization required");
    }
    final String userId = getUserId(user);
    // Update the knowledgeNode with the knowledgeNodeForm sent from the client.
    TxResult<KnowledgeNode> result = ofy().transact(new Work<TxResult<KnowledgeNode>>() {
      @Override
      public TxResult<KnowledgeNode> run() {
        // If there is no knowledge node with the id, throw a 404 error.
        Key<KnowledgeNode> knowledgeNodeKey = Key.create(websafeKnowledgeNodeKey);
        KnowledgeNode knowledgeNode = ofy().load().key(knowledgeNodeKey).now();
        if (knowledgeNode == null) {
          return new TxResult<>(
              new NotFoundException("No knowledge node found with the key: "
                  + websafeKnowledgeNodeKey));
        }
        // If the user is not the owner, throw a 403 error.
        Profile profile = ofy().load().key(Key.create(Profile.class, userId)).now();
        if (profile == null ||
            !knowledgeNode.getCreatedBy().equals(userId)) {
          return new TxResult<>(
              new ForbiddenException("Only the owner can update the knowledge node."));
        }
        knowledgeNode.updateWithKnowledgeNodeForm(knowledgeNodeForm);
        ofy().save().entity(knowledgeNode).now();
        return new TxResult<>(knowledgeNode);
      }
    });
    // NotFoundException or ForbiddenException is actually thrown here.
    return result.getResult();
  }

  /**
   * Returns a knowledge node object with the given web safe key.
   *
   * @param websafeKnowledgeNodeKey The String representation of the knowledge node key.
   * @return a knowledge node object with the given web safe key.
   * @throws NotFoundException when there is no knowledge node with the given key.
   */
  @ApiMethod(
      name = "getKnowledgeNode",
      path = "getKnowledgeNode/{websafeKnowledgeNodeKey}",
      httpMethod = HttpMethod.GET
  )
  public KnowledgeNode getKnowledgeNode(
      @Named("websafeKnowledgeNodeKey") final String websafeKnowledgeNodeKey)
      throws NotFoundException {
    Key<KnowledgeNode> knowledgeNodeKey = Key.create(websafeKnowledgeNodeKey);
    KnowledgeNode knowledgeNode = ofy().load().key(knowledgeNodeKey).now();
    if (knowledgeNode == null) {
      throw new NotFoundException("No knowledge node found with key: " + knowledgeNodeKey);
    }
    return knowledgeNode;
  }

  /**
   * Deletes a knowledge node object with the given web safe key.
   * @param user A user who invokes this method, null when the user is not signed in.
   * @param websafeKnowledgeNodeKey The String representation of the key.
   * @throws NotFoundException when there is no knowledfge with the given key.
   */
  @ApiMethod(
      name = "deleteKnowledgeNode",
      path = "deleteKnowledgeNode/{websafeKnowledgeNodeKey}",
      httpMethod = HttpMethod.DELETE
  )
  public void deleteKnowledgeNode(
      final User user,
      @Named("websafeKnowledgeNodeKey") final String websafeKnowledgeNodeKey)
      throws NotFoundException, UnauthorizedException, ForbiddenException {
    final String userId = getUserId(user);
    // If not signed in, throw a 401 error.
    if (user == null) {
      throw new UnauthorizedException("Authorization required");
    }
    Key<KnowledgeNode> knowledgeNodeKey = Key.create(websafeKnowledgeNodeKey);
    KnowledgeNode knowledgeNode = ofy().load().key(knowledgeNodeKey).now();
    if (knowledgeNode == null) {
      throw new NotFoundException("No knowledge node found with key: " + websafeKnowledgeNodeKey);
    } else {
      // If the user is not the owner, throw a 403 error.
      Profile profile = ofy().load().key(Key.create(Profile.class, userId)).now();
      if (profile == null ||
          !knowledgeNode.getCreatedBy().equals(userId)) {
        throw new ForbiddenException("Only the owner can delete the knowledge node.");
      }
      deleteKnowledgeNodeFromParents(knowledgeNode);
      ofy().delete().key(knowledgeNodeKey).now();
    }
  }

  // TODO(du6): do this in task queue
  private void deleteKnowledgeNodeFromParents(KnowledgeNode knowledgeNode) {
    for (String parentKey : knowledgeNode.getParents()) {
      Key<KnowledgeNode> knowledgeNodeKey = Key.create(parentKey);
      KnowledgeNode parentNode = ofy().load().key(knowledgeNodeKey).now();
      parentNode.deleteChild(knowledgeNode.getWebsafeKey());
    }
  }

  /**
   * Returns a list of knowledge nodes that the user created.
   * In order to receive the websafeKnowledgeNodeKey via the JSON params, uses a POST method.
   *
   * @param user An user who invokes this method, null when the user is not signed in.
   * @return a list of knowledge nodes that the user created.
   * @throws UnauthorizedException when the user is not signed in.
   */
  @ApiMethod(
      name = "getKnowledgeNodesCreatedBy",
      path = "getKnowledgeNodesCreatedBy",
      httpMethod = HttpMethod.POST
  )
  public List<KnowledgeNode> getKnowledgeNodesCreated(
      final User user,
      @Named("limit") @DefaultValue(DEFAULT_QUERY_LIMIT) final int limit)
      throws UnauthorizedException {
    // If not signed in, throw a 401 error.
    if (user == null) {
      throw new UnauthorizedException("Authorization required");
    }
    String userId = getUserId(user);
    return queryByOwner(userId).limit(limit).list();
  }
  
  private static Query<KnowledgeNode> queryByOwner(final String userId) {
    final Filter ownerFilter = new FilterPredicate("createdBy", FilterOperator.EQUAL, userId);
    return ofy().load().type(KnowledgeNode.class).filter(ownerFilter);
  }

  /**
   * Returns all knowledge nodes.
   * In order to receive the websafeKnowledgeNodeKey via the JSON params, uses a POST method.
   *
   * @param user An user who invokes this method, null when the user is not signed in.
   * @return all knowledge nodes.
   */
  @ApiMethod(
      name = "getAllKnowledgeNodes",
      path = "getAllKnowledgeNodes",
      httpMethod = HttpMethod.POST
  )
  public List<KnowledgeNode> getAllKnowledgeNodes(
      final User user,
      @Named("limit") @DefaultValue(DEFAULT_QUERY_LIMIT) final int limit)
      throws UnauthorizedException {
    return ofy().load().type(KnowledgeNode.class).limit(limit).list();
  }
}