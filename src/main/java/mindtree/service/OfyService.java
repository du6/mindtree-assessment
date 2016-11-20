package main.java.mindtree.service;

import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyFactory;
import com.googlecode.objectify.ObjectifyService;

import main.java.mindtree.domain.Edge;
import main.java.mindtree.domain.KnowledgeNode;
import main.java.mindtree.domain.Profile;
import main.java.mindtree.domain.Quiz;
import main.java.mindtree.domain.QuizTag;

/**
 * Custom Objectify Service that this application should use.
 */
public class OfyService {
    /**
     * This static block ensure the entity registration.
     */
    static {
        factory().register(Edge.class);
        factory().register(KnowledgeNode.class);
        factory().register(Profile.class);
        factory().register(Quiz.class);
        factory().register(QuizTag.class);
    }

    /**
     * Use this static method for getting the Objectify service object in order to make sure the
     * above static block is executed before using Objectify.
     * @return Objectify service object.
     */
    public static Objectify ofy() {
        return ObjectifyService.ofy();
    }

    /**
     * Use this static method for getting the Objectify service factory.
     * @return ObjectifyFactory.
     */
    public static ObjectifyFactory factory() {
        return ObjectifyService.factory();
    }
}
