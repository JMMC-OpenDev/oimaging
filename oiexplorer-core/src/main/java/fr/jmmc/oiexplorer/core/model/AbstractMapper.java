/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oiexplorer.core.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author bourgesl
 */
public abstract class AbstractMapper<K> {

    /** Logger */
    protected static final Logger logger = LoggerFactory.getLogger(AbstractMapper.class);

    // members:    
    /** global item keyed by local item */
    protected final Map<K, K> globalPerLocal = new IdentityHashMap<K, K>();
    /** local items keyed by global item */
    protected final Map<K, List<K>> localsPerGlobal = new IdentityHashMap<K, List<K>>();

    protected AbstractMapper() {
        super();
    }

    /**
     * Clear the mappings
     * May be overriden
     */
    public void clear() {
        // clear insMode mappings:
        globalPerLocal.clear();
        localsPerGlobal.clear();
    }

    public final void dump() {
        if (logger.isDebugEnabled()) {
            logger.debug("Globals: {}", localsPerGlobal.keySet());
            logger.debug("Locals:  {}", globalPerLocal.keySet());
        }
    }

    public final void register(final K local) {
        if (local != null) {
            K match = null;
            for (K global : localsPerGlobal.keySet()) {
                if (match(global, local)) {
                    match = global;
                    break;
                }
            }

            final List<K> locals;
            if (match == null) {
                // Create global (clone):
                match = createGlobal(local);

                locals = new ArrayList<K>(2);
                localsPerGlobal.put(match, locals);
            } else {
                locals = localsPerGlobal.get(match);
            }

            // anyway
            globalPerLocal.put(local, match);
            locals.add(local);
        }
    }

    public final K getGlobal(final K local) {
        return globalPerLocal.get(local);
    }

    public final List<K> getLocals(final K global) {
        return localsPerGlobal.get(global);
    }

    public final List<String> getSortedUniqueAliases(final K global) {
        List<String> results = null;

        final List<K> locals = getLocals(global);
        final int len = locals.size();

        if (len > 1) {
            final Set<String> aliases = new HashSet<String>();
            final String main = getName(global);
            // always put global name:
            aliases.add(main);
            for (int j = 0; j < len; j++) {
                aliases.add(getName(locals.get(j)));
            }
            // remove global name:
            aliases.remove(main);

            if (aliases.size() > 1) {
                results = new ArrayList<String>(aliases);
                Collections.sort(results);
            }
        }
        return results;
    }

    protected abstract K createGlobal(final K local);

    protected abstract boolean match(final K src, final K other);

    protected abstract String getName(final K src);

}
