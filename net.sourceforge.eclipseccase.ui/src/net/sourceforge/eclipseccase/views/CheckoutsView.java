
package net.sourceforge.eclipseccase.views;

import net.sourceforge.eclipseccase.ClearcaseProvider;
import net.sourceforge.eclipseccase.StateCache;

/**
 * The Checkouts view
 */
public class CheckoutsView extends ClearcaseViewPart
{
    /* (non-Javadoc)
     * @see net.sourceforge.eclipseccase.views.ClearcaseViewPart#shouldAdd(net.sourceforge.eclipseccase.StateCache)
     */
    protected boolean shouldAdd(StateCache stateCache)
    {
        return stateCache.isCheckedOut() || (!stateCache.hasRemote() && !ClearcaseProvider.getClearcaseProvider(stateCache.getResource()).isIgnored(stateCache.getResource()));
    }
}