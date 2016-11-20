package main.java.mindtree.spi;

import com.google.api.server.spi.response.ConflictException;
import com.google.api.server.spi.response.ForbiddenException;
import com.google.api.server.spi.response.NotFoundException;
import com.google.api.server.spi.response.UnauthorizedException;
import com.google.appengine.api.users.User;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.Work;

import main.java.mindtree.domain.AppEngineUser;
import main.java.mindtree.domain.MindTreeEntity;
import main.java.mindtree.form.MindTreeForm;

import static main.java.mindtree.service.OfyService.factory;
import static main.java.mindtree.service.OfyService.ofy;

/**
 * Util class to help building APIs.
 */
class ApiUtils {
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
  public static class TxResult<ResultType> {

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
   * This is an ugly workaround for null userId for Android clients.
   *
   * @param user A User object injected by the cloud endpoints.
   * @return the App Engine userId for the user.
   */
  public static String getUserId(User user) {
    String userId = user.getUserId();
    if (userId == null) {
      AppEngineUser appEngineUser = new AppEngineUser(user);
      ofy().save().entity(appEngineUser).now();
      // Begin new session for not using session cache.
      Objectify objectify = ofy().factory().begin();
      AppEngineUser savedUser = objectify.load().key(appEngineUser.getKey()).now();
      userId = savedUser.getUser().getUserId();
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

  public static MindTreeEntity createEntity (
      User user,
      final MindTreeForm form,
      final Class<? extends MindTreeEntity> entityClass)
      throws NotFoundException, ForbiddenException, ConflictException {
    final String userId = getUserId(user);
    // Start a transaction.
    TxResult<MindTreeEntity> entity = ofy().transact(new Work<TxResult<MindTreeEntity>>() {
      @Override
      public TxResult<MindTreeEntity> run() {
        try {
          Key<MindTreeEntity> key = factory().allocateId(MindTreeEntity.class);
          Class[] cArg = new Class[3];
          cArg[0] = Long.class;
          cArg[1] = String.class;
          cArg[2] = form.getClass();
          MindTreeEntity entity = entityClass.getDeclaredConstructor(cArg).newInstance(
              key.getId(),
              userId,
              form);
          ofy().save().entity(entity).now();
          return new TxResult<>(entity);
        } catch (Exception e) {
          return new TxResult<>(e);
        }
      }
    });
    return entityClass.cast(entity.getResult());
  }

  public static MindTreeEntity updateEntity (
      User user,
      final MindTreeForm form,
      final String websafeKey,
      final Class<? extends MindTreeEntity> entityClass)
      throws NotFoundException, ForbiddenException, ConflictException {
    final String userId = getUserId(user);
    // Update the knowledgeNode with the knowledgeNodeForm sent from the client.
    TxResult<MindTreeEntity> result = ofy().transact(new Work<TxResult<MindTreeEntity>>() {
      @Override
      public TxResult<MindTreeEntity> run() {
        // If there is no knowledge node with the id, throw a 404 error.
        Key<MindTreeEntity> entityKey = Key.create(websafeKey);
        MindTreeEntity entity = ofy().load().key(entityKey).now();
        if (entity == null) {
          return new TxResult<>(
              new NotFoundException("No knowledge node found with the key: "
                  + websafeKey));
        }
        entity.updateWithForm(form);
        ofy().save().entity(entity).now();
        return new TxResult<>(entity);
      }
    });
    // NotFoundException or ForbiddenException is actually thrown here.
    return entityClass.cast(result.getResult());
  }
}
