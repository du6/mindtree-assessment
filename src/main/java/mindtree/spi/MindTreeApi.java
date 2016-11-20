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
import com.googlecode.objectify.cmd.Query;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import javax.inject.Named;

import main.java.mindtree.Constants;
import main.java.mindtree.domain.Edge;
import main.java.mindtree.domain.KnowledgeNode;
import main.java.mindtree.form.EdgeForm;
import main.java.mindtree.form.KnowledgeNodeForm;
import main.java.mindtree.domain.Profile;
import main.java.mindtree.form.ProfileForm;

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


  /** API For Profile */


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
    checkSignedIn(user);
    return ofy().load().key(Key.create(Profile.class, ApiUtils.getUserId(user))).now();
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
    checkSignedIn(user);
    String displayName = profileForm.getDisplayName();

    Profile profile = ofy().load().key(Key.create(Profile.class, ApiUtils.getUserId(user))).now();
    if (profile == null) {
      if (displayName == null) {
        displayName = extractDefaultDisplayNameFromEmail(user.getEmail());
      }
      profile = new Profile(ApiUtils.getUserId(user), displayName, user.getEmail());
    } else {
      profile.update(displayName);
    }
    ofy().save().entity(profile).now();
    return profile;
  }


  /** API For Knowledge Graph */


  /**
   * Creates a new KnowledgeNode object and stores it to the datastore.
   *
   * @param user A user who invokes this method, null when the user is not signed in.
   * @param knowledgeNodeForm A KnowledgeNodeForm object representing user's inputs.
   * @return A newly created KnowledgeNode Object.
   * @throws UnauthorizedException when the user is not signed in.
   */
  @ApiMethod(
      name = "createKnowledgeNode",
      path = "createKnowledgeNode",
      httpMethod = HttpMethod.POST)
  public KnowledgeNode createKnowledgeNode(
      final User user,
      final KnowledgeNodeForm knowledgeNodeForm)
      throws UnauthorizedException, NotFoundException, ForbiddenException, ConflictException {
    checkSignedIn(user);
    return (KnowledgeNode) ApiUtils.createEntity(user, knowledgeNodeForm, KnowledgeNode.class);
  }

  /**
   * Creates an edge that connects a parent node and a child node
   *
   * @param user A user who invokes this method, null when the user is not signed in.
   * @param edgeForm An EdgeForm object representing user's inputs.
   * @throws UnauthorizedException when the user is not signed in.
   */
  @ApiMethod(
      name = "createEdge",
      path = "createEdge",
      httpMethod = HttpMethod.POST)
  public Edge createEdge(final User user, final EdgeForm edgeForm)
      throws UnauthorizedException, NotFoundException, ForbiddenException, ConflictException {
    checkSignedIn(user);
    return (Edge) ApiUtils.createEntity(user, edgeForm, Edge.class);
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
  public KnowledgeNode updateKnowledgeNode(
      final User user,
      final KnowledgeNodeForm knowledgeNodeForm,
      @Named("websafeKnowledgeNodeKey")
      final String websafeKnowledgeNodeKey)
      throws UnauthorizedException, NotFoundException, ForbiddenException, ConflictException {
    checkSignedIn(user);
    return (KnowledgeNode) ApiUtils.updateEntity(
        user, knowledgeNodeForm, websafeKnowledgeNodeKey, KnowledgeNode.class);
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
   * @throws NotFoundException when there is no knowledge node with the given key.
   * @throws UnauthorizedException when user is not logged in.
   */
  @ApiMethod(
      name = "deleteKnowledgeNode",
      path = "deleteKnowledgeNode/{websafeKnowledgeNodeKey}",
      httpMethod = HttpMethod.DELETE
  )
  public void deleteKnowledgeNode(
      final User user,
      @Named("websafeKnowledgeNodeKey") final String websafeKnowledgeNodeKey)
      throws NotFoundException, UnauthorizedException {
    checkSignedIn(user);
    Key<KnowledgeNode> knowledgeNodeKey = Key.create(websafeKnowledgeNodeKey);
    KnowledgeNode knowledgeNode = ofy().load().key(knowledgeNodeKey).now();
    if (knowledgeNode == null) {
      throw new NotFoundException("No knowledge node found with key: " + websafeKnowledgeNodeKey);
    } else {
      Set<Edge> relatedEdges = getEdgesForNode(websafeKnowledgeNodeKey);
      ofy().delete().keys(knowledgeNodeKey).now();
      ofy().delete().entities(relatedEdges).now();
    }
  }

  /**
   * Deletes an edge that connects a parent node and a child node
   *
   * @param user A user who invokes this method, null when the user is not signed in.
   * @param edgeForm An EdgeForm object representing user's inputs.
   * @throws NotFoundException when there is no edge with the given parent and child keys.
   * @throws UnauthorizedException when user is not logged in.
   */
  @ApiMethod(
      name = "deleteEdges",
      path = "deleteEdges",
      httpMethod = HttpMethod.POST)
  public void deleteEdges(final User user, final EdgeForm edgeForm)
      throws NotFoundException, UnauthorizedException {
    checkSignedIn(user);
    final Filter parentKeyFilter =
        new FilterPredicate("parentKey", FilterOperator.EQUAL, edgeForm.getParentKey());
    final Filter childKeyFilter =
        new FilterPredicate("childKey", FilterOperator.EQUAL, edgeForm.getChildKey());
    List<Edge> edges = ofy().load().type(Edge.class)
        .filter(parentKeyFilter)
        .filter(childKeyFilter)
        .list();
    ofy().delete().entities(edges).now();
  }

  /**
   * @param websafeKnowledgeNodeKey the web safe key for a knowledge node
   * @return a set of edge entities connecting the given node
   */
  private Set<Edge> getEdgesForNode(String websafeKnowledgeNodeKey) {
    Set<Edge> edges = new HashSet<>();
    final Filter parentKeyFilter =
        new FilterPredicate("parentKey", FilterOperator.EQUAL, websafeKnowledgeNodeKey);
    final Filter childKeyFilter =
        new FilterPredicate("childKey", FilterOperator.EQUAL, websafeKnowledgeNodeKey);
    edges.addAll(ofy().load().type(Edge.class).filter(parentKeyFilter).list());
    edges.addAll(ofy().load().type(Edge.class).filter(childKeyFilter).list());
    return edges;
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
    checkSignedIn(user);
    String userId = ApiUtils.getUserId(user);
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

  /**
   * Returns all edges.
   * In order to receive the websafeKnowledgeNodeKey via the JSON params, uses a POST method.
   *
   * @param user An user who invokes this method, null when the user is not signed in.
   * @return all edges.
   */
  @ApiMethod(
      name = "getAllEdges",
      path = "getAllEdges",
      httpMethod = HttpMethod.POST
  )
  public List<Edge> getAllEdges(
      final User user,
      @Named("limit") @DefaultValue(DEFAULT_QUERY_LIMIT) final int limit)
      throws UnauthorizedException {
    return ofy().load().type(Edge.class).limit(limit).list();
  }

  private void checkSignedIn(User user) throws UnauthorizedException {
    // If not signed in, throw a 401 error.
    if (user == null) {
      throw new UnauthorizedException("Authorization required");
    }
  }


  /** API For Quiz */


}