package thredds.server.sos.util;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import org.joda.time.DateTime;
import org.joda.time.DateTimeUtils;
import ucar.nc2.units.DateFormatter;

public class TimeUtils
{
    /**
     * <p>A {@link Comparator} that compares {@link DateTime} objects based only
     * on their millisecond instant values.  This can be used for
     * {@link Collections#sort(java.util.List, java.util.Comparator) sorting} or
     * {@link Collections#binarySearch(java.util.List, java.lang.Object,
     * java.util.Comparator) searching} {@link List}s of {@link DateTime} objects.</p>
     * <p>The ordering defined by this Comparator is <i>inconsistent with equals</i>
     * because it ignores the Chronology of the DateTime instants.</p>
     * <p><i>(Note: The DateTime object inherits from Comparable, not
     * Comparable&lt;DateTime&gt;, so we can't use the methods in Collections
     * directly.  However we can reuse the {@link DateTime#compareTo(java.lang.Object)}
     * method.)</i></p>
     */
    public static final Comparator<DateTime> DATE_TIME_COMPARATOR =
        new Comparator<DateTime>()
    {
        @Override
        public int compare(DateTime dt1, DateTime dt2) {
            return dt1.compareTo(dt2);
        }
    };

     /**
     * Searches the given list of timesteps for the nearest date-time of the
     * specified date-time using the binary search algorithm.
     * Matches are found based only upon the millisecond instant of the target
     * DateTime, not its Chronology.
     * @param  target The timestep to search for.
     * @return the index of the search key, if it is contained in the list;
     *	       otherwise, <tt>(-(<i>insertion point</i>) - 1)</tt>.  The
     *	       <i>insertion point</i> is defined as the point at which the
     *	       key would be inserted into the list: the index of the first
     *	       element greater than the key, or <tt>list.size()</tt> if all
     *	       elements in the list are less than the specified key.  Note
     *	       that this guarantees that the return value will be &gt;= 0 if
     *	       and only if the key is found.  If this Layer does not have a time
     *         axis this method will return -1.
     */
    public static int findNearestTimeIndex(List<DateTime> dtList, DateTime target)
    {
        int index = Collections.binarySearch(dtList, target, DATE_TIME_COMPARATOR);
        if (index < 0)
        {
            // No exact match, but we have the insertion point, i.e. the index of
            // the first element that is greater than the target value
            int insertionPoint = -(index + 1);

            // Deal with the extremes
            if (insertionPoint == 0) {
                index = 0;
            } else if (insertionPoint == dtList.size()) {
                index = dtList.size() - 1;
            } else {
                // We need to work out which index is closer: insertionPoint or
                // (insertionPoint - 1)
                long mils = DateTimeUtils.getInstantMillis(target);
                long d1 = Math.abs(mils - DateTimeUtils.getInstantMillis(dtList.get(insertionPoint)));
                long d2 = Math.abs(mils - DateTimeUtils.getInstantMillis(dtList.get(insertionPoint - 1)));
                if (d1 < d2) index = insertionPoint;
                else index = insertionPoint - 1;
            }
        }
        return index;
    }
}
