package jmri.jmrit.logixng;

import java.util.List;
import jmri.Manager;

/**
 * Manager for ConditionalNG
 * 
 * @author Daniel Bergqvist Copyright 2018
 */
public interface ConditionalNG_Manager extends Manager<ConditionalNG> {

    /**
     * Create a new ConditionalNG if the ConditionalNG does not exist.
     *
     * @param systemName the system name
     * @param userName   the user name
     * @return a new ConditionalNG or null if unable to create
     */
    public ConditionalNG createConditionalNG(String systemName, String userName)
            throws IllegalArgumentException;
    
    /**
     * For use with User GUI, to allow the auto generation of systemNames, where
     * the user can optionally supply a username.
     *
     * @param userName the user name
     * @return a new ConditionalNG or null if unable to create
     */
    public ConditionalNG createConditionalNG(String userName)
            throws IllegalArgumentException;
    
    /**
     * Creates the initial items in the ConditionalNG tree.
     * 
     * By default, this is as following:
     * + ActionMany
     *   + ActionHoldAnything
     *   + ActionDoIf
     * 
     * @param conditionalNG the ConditionalNG to be initialized with a tree
     */
    public void setupInitialConditionalNGTree(ConditionalNG conditionalNG);

    /**
     * Locate via user name, then system name if needed. Does not create a new
     * one if nothing found
     *
     * @param name User name or system name to match
     * @return null if no match found
     */
    public ConditionalNG getConditionalNG(String name);

    public ConditionalNG getByUserName(String name);

    public ConditionalNG getBySystemName(String name);
    
    /**
     * Create a new system name for a ConditionalNG.
     * @return a new system name
     */
    public String getNewSystemName();
    
    /**
     * Resolve all the ConditionalNG trees.
     * <P>
     * This method ensures that everything in the ConditionalNG tree has a pointer
     * to its parent.
     */
    public void resolveAllTrees();

    /**
     * Setup all ConditionalNGs. This method is called after a configuration file is
     * loaded.
     */
    public void setupAllConditionalNGs();

    /**
     * Delete ConditionalNG by removing it from the manager. The ConditionalNG must first
     * be deactivated so it stops processing.
     *
     * @param x the ConditionalNG to delete
     */
    void deleteConditionalNG(ConditionalNG x);

    /**
     * Support for loading ConditionalNGs in a disabled state
     * 
     * @param s true if ConditionalNG should be disabled when loaded
     */
    public void setLoadDisabled(boolean s);
    
    /**
     * Register a FemaleSocketFactory.
     */
    public void registerFemaleSocketFactory(FemaleSocketFactory factory);
    
    /**
     * Register a FemaleSocketFactory.
     */
    public List<FemaleSocketFactory> getFemaleSocketFactories();
    
}
