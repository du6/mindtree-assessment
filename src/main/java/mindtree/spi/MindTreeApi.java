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
import main.java.mindtree.domain.Quiz;
import main.java.mindtree.domain.QuestionTag;
import main.java.mindtree.form.EdgeForm;
import main.java.mindtree.form.KnowledgeNodeForm;
import main.java.mindtree.domain.Profile;
import main.java.mindtree.form.ProfileForm;
import main.java.mindtree.form.QuestionTagForm;
import main.java.mindtree.form.QuizForm;

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
    ApiUtils.checkSignedIn(user);
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
    ApiUtils.checkSignedIn(user);
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
    ApiUtils.checkSignedIn(user);
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
      httpMethod = HttpMethod.DELETE)
  public void deleteEdges(final User user, final EdgeForm edgeForm)
      throws NotFoundException, UnauthorizedException {
    ApiUtils.checkSignedIn(user);
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
   * In order to receive the web safe key via the JSON params, uses a POST method.
   *
   * @param user An user who invokes this method, null when the user is not signed in.
   * @param limit The number of entities to return
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
    ApiUtils.checkSignedIn(user);
    String userId = ApiUtils.getUserId(user);
    return queryByOwner(userId).limit(limit).list();
  }
  
  private static Query<KnowledgeNode> queryByOwner(final String userId) {
    final Filter ownerFilter = new FilterPredicate("createdBy", FilterOperator.EQUAL, userId);
    return ofy().load().type(KnowledgeNode.class).filter(ownerFilter);
  }

  /**
   * Returns all knowledge nodes.
   * In order to receive the web safe key via the JSON params, uses a POST method.
   *
   * @param user An user who invokes this method, null when the user is not signed in.
   * @param limit The number of entities to return
   * @return all knowledge nodes.
   */
  @ApiMethod(
      name = "getAllKnowledgeNodes",
      path = "getAllKnowledgeNodes",
      httpMethod = HttpMethod.POST
  )
  public List<KnowledgeNode> getAllKnowledgeNodes(
      final User user,
      @Named("limit") @DefaultValue(DEFAULT_QUERY_LIMIT) final int limit) {
    return ofy().load().type(KnowledgeNode.class).limit(limit).list();
  }

  /**
   * Returns all edges.
   * In order to receive the web safe key via the JSON params, uses a POST method.
   *
   * @param user An user who invokes this method, null when the user is not signed in.
   * @param limit The number of entities to return
   * @return all edges.
   */
  @ApiMethod(
      name = "getAllEdges",
      path = "getAllEdges",
      httpMethod = HttpMethod.POST
  )
  public List<Edge> getAllEdges(
      final User user,
      @Named("limit") @DefaultValue(DEFAULT_QUERY_LIMIT) final int limit) {
    return ofy().load().type(Edge.class).limit(limit).list();
  }


  /** API For Quiz */


  /**
   * Creates a quiz
   *
   * @param user A user who invokes this method, null when the user is not signed in.
   * @param quizForm An QuizForm object representing user's inputs.
   * @throws UnauthorizedException when the user is not signed in.
   */
  @ApiMethod(
      name = "createQuiz",
      path = "createQuiz",
      httpMethod = HttpMethod.POST)
  public Quiz createQuiz(final User user, final QuizForm quizForm)
      throws UnauthorizedException, NotFoundException, ForbiddenException, ConflictException {
    return (Quiz) ApiUtils.createEntity(user, quizForm, Quiz.class);
  }

  /**
   * Creates a question tag
   *
   * @param user A user who invokes this method, null when the user is not signed in.
   * @param tagForm An QuestionTagForm object representing user's inputs.
   * @throws UnauthorizedException when the user is not signed in.
   */
  @ApiMethod(
      name = "createQuestionTag",
      path = "createQuestionTag",
      httpMethod = HttpMethod.POST)
  public QuestionTag createQuestionTag(final User user, final QuestionTagForm tagForm)
      throws UnauthorizedException, NotFoundException, ForbiddenException, ConflictException {
    return (QuestionTag) ApiUtils.createEntity(user, tagForm, QuestionTag.class);
  }

  /**
   * Updates the existing quiz with the given web safe key.
   *
   * @param user A user who invokes this method, null when the user is not signed in.
   * @param quizForm A QuizForm object representing user's inputs.
   * @param websafeQuizKey The String representation of the Quiz key.
   * @return Updated Quiz object.
   * @throws UnauthorizedException when the user is not signed in.
   * @throws NotFoundException when there is no quiz with the given key.
   */
  @ApiMethod(
      name = "updateQuiz",
      path = "updateQuiz/{websafeQuizKey}",
      httpMethod = HttpMethod.PUT
  )
  public Quiz updateQuiz(
      final User user,
      final QuizForm quizForm,
      @Named("websafeQuizKey")
      final String websafeQuizKey)
      throws UnauthorizedException, NotFoundException, ForbiddenException, ConflictException {
    return (Quiz) ApiUtils.updateEntity(user, quizForm, websafeQuizKey, Quiz.class);
  }

  /**
   * Deletes an question tag
   *
   * @param user A user who invokes this method, null when the user is not signed in.
   * @param websafeQuestionTagKey An EdgeForm object representing user's inputs.
   * @throws NotFoundException when there is no question tag with the given web safe key.
   * @throws UnauthorizedException when user is not logged in.
   */
  @ApiMethod(
      name = "deleteQuestionTag",
      path = "deleteQuestionTag/{websafeQuestionTagKey}",
      httpMethod = HttpMethod.DELETE)
  public void deleteQuestionTag(
      final User user,
      @Named("websafeQuestionTagKey") final String websafeQuestionTagKey)
      throws NotFoundException, UnauthorizedException {
    ApiUtils.checkSignedIn(user);
    Key<QuestionTag> questionTagKey = Key.create(websafeQuestionTagKey);
    QuestionTag questionTag = ofy().load().key(questionTagKey).now();
    if (questionTag == null) {
      throw new NotFoundException("No tag found with key: " + websafeQuestionTagKey);
    } else {
      ofy().delete().keys(questionTagKey).now();
    }
  }

  /**
   * Deletes a quiz by marking it expired.
   * @param user A user who invokes this method, null when the user is not signed in.
   * @param websafeQuizKey The String representation of the key.
   * @throws NotFoundException when there is no quiz with the given key.
   * @throws UnauthorizedException when user is not logged in.
   */
  @ApiMethod(
      name = "deleteQuiz",
      path = "deleteQuiz/{websafeQuizKey}",
      httpMethod = HttpMethod.DELETE
  )
  public void deleteQuiz(
      final User user,
      @Named("websafeQuizKey") final String websafeQuizKey)
      throws NotFoundException, UnauthorizedException {
    ApiUtils.checkSignedIn(user);
    Key<Quiz> quizKey = Key.create(websafeQuizKey);
    Quiz quiz = ofy().load().key(quizKey).now();
    if (quiz == null) {
      throw new NotFoundException("No quiz found with key: " + websafeQuizKey);
    } else {
      quiz.delete();
      ofy().save().entity(quiz).now();
    }
  }

  /**
   * Returns all active quizzes.
   * In order to receive the web safe key via the JSON params, uses a POST method.
   *
   * @param user An user who invokes this method, null when the user is not signed in.
   * @param limit The number of entities to return
   * @return all active quizzes.
   */
  @ApiMethod(
      name = "getAllActiveQuizzes",
      path = "getAllActiveQuizzes",
      httpMethod = HttpMethod.POST
  )
  public List<Quiz> getAllActiveQuizzes(
      final User user,
      @Named("limit") @DefaultValue(DEFAULT_QUERY_LIMIT) final int limit) {
    return ofy().load().type(Quiz.class).filter(Quiz.activeQuizFilter()).limit(limit).list();
  }

  /**
   * Returns all tags for a give question key.
   * In order to receive the web safe key via the JSON params, uses a POST method.
   *
   * @param user An user who invokes this method, null when the user is not signed in.
   * @param websafeQuestionKey The question key.
   * @param limit The number of entities to return
   * @return all question tags for a give question key.
   */
  @ApiMethod(
      name = "getQuestionTags",
      path = "getQuestionTags",
      httpMethod = HttpMethod.POST
  )
  public List<QuestionTag> getQuestionTags(
      final User user,
      @Named("websafeQuestionKey") final String websafeQuestionKey,
      @Named("limit") @DefaultValue(DEFAULT_QUERY_LIMIT) final int limit) {
    final Filter questionFilter =
        new FilterPredicate("questionKey", FilterOperator.EQUAL, websafeQuestionKey);
    return ofy().load().type(QuestionTag.class).filter(questionFilter).limit(limit).list();
  }
}