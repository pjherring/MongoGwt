/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pjherring.mongogwt.shared.query;

import com.google.gwt.user.client.rpc.IsSerializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author pjherring
 */
public class Query implements IsSerializable {

    public static class Reference implements IsSerializable {
        private String id;
        private Class entityReference;

        public Reference() {}

        public Reference(String id, Class entityReference) {
            this.id = id;
            this.entityReference = entityReference;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public Class getEntityReference() {
            return entityReference;
        }

        public void setEntityReference(Class entityReference) {
            this.entityReference = entityReference;
        }
    }

    /*
     * This contains a string value of the column that will be searched on.
     */
    protected String nextKey = "";
    /*
     * The map that holds the actual query. This is used by Database to
     * actually search MongoDB
     */
    protected Map<String, Object> queryMap = new HashMap<String, Object>();
    /*
     * The sort map will hold a column name and -1 or 1 depending on the
     * sort desired.
     */
    protected Map<String, Integer> sortMap = new HashMap<String, Integer>();
    /*
     * Holds the keys of the regexp columns.
     * @link org.pjherring.mongogwt.server.domain.operation.Read will use these
     * to compile the Strings into @link java.util.regex.Pattern
     */
    protected List<String> regexpKeys = new ArrayList<String>();

    protected List<String> columns = new ArrayList<String>();

    /*
     * Holds the keys of reference ids.
     */

    /*
     * The limit to our query.
     */
    int limit = -1;

    public static class KeyNotSetException extends RuntimeException {

        public KeyNotSetException() {
            super("Key is not set. Set key before specifying equivalancy.");
        }
    }

    /*
     * Constants used for specifying the direction of sort.
     */
    public enum Sort {
        DESC, ASC;
    }

    public Query() {}

    public Query select(String... columnsToAdd) {
        columns.addAll(Arrays.asList(columnsToAdd));
        return this;
    }

    public List<String> getColumns() {
        return columns;
    }

    /*
     * Get the limit set on the query.
     */
    public int getLimit() {
        return limit;
    }

    public Query setLimit(int limit) {
        this.limit = limit;
        return this;
    }

    /*
     * Being the query with specifying the first key to specify a filter with.
     */
    public Query start(String key) {
        nextKey = key;
        return this;
    }

    /*
     * Add an and clause to the query.
     */
    public Query and(String key) {
        nextKey = key;
        return this;
    }

    /*
     * Specify equivalance of the previous key stored.
     * Usage: new Query().start("someKey").is("someValue");
     */
    public Query is(Object value) {
        checkKeyExists();
        queryMap.put(nextKey, value);
        nextKey = "";
        return this;
    }

    /*
     * Specify non-equivalance of the previous key stored.
     * Usage: new Query().start("someKey").notEquals("someValue");
     */
    public Query notEquals(Object value) {
        checkKeyExists();
        queryMap.put(nextKey, makeMap("$ne", value));
        nextKey = "";
        return this;
    }

    /*
     * Specify greater than filter of the previous key stored.
     * Usage: new Query().start("someKey").greaterThen(5);
     * The value stored in "someKey" must be greater than 5.
     *
     */
    public Query greaterThen(Object value) {
        checkKeyExists();
        queryMap.put(nextKey, makeMap("$gt", value));
        nextKey = "";
        return this;
    }

    /*
     * Specify greater than or equal to filter of the previous key stored.
     * Usage: new Query().start("someKey").greaterThenEquals(5);
     * The value stored in "someKey" must be greater than  or equal to 5.
     *
     */
    public Query greaterThenEquals(Object value) {
        checkKeyExists();
        queryMap.put(nextKey, makeMap("$gte", value));
        nextKey = "";
        return this;
    }

    /*
     * Specify less than filter of the previous key stored.
     * Usage: new Query().start("someKey").lessThen(5);
     * The value stored in "someKey" must be less then 5.
     *
     */
    public Query lessThen(Object value) {
        checkKeyExists();
        queryMap.put(nextKey, makeMap("$lt", value));
        nextKey = "";
        return this;
    }

    /*
     * Specify less than or equal to filter of the previous key stored.
     * Usage: new Query().start("someKey").lessThenEquals(5);
     * The value stored in "someKey" must be less then or equal to 5.
     *
     */
    public Query lessThenEquals(Object value) {
        checkKeyExists();
        queryMap.put(nextKey, makeMap("$lte", value));
        nextKey = "";
        return this;
    }

    /*
     * Give a range for a value. Calling this method will include the floor
     * and ceiling. Usage: new Query().start("someKey").range(5, 10). This will
     * find values stored in "someKey" that are between 5 and 10 inclusively.
     */
    public Query range(Object floor, Object ceiling) {
        return range(floor, ceiling, true, true);
    }

    /*
     * Give a range for a value and specify if you wish to include the floor
     * and ceiling in the range.
     * Usage: new Query().start("someKey").range(5, 10, true, false). This will
     * find values stored in "someKey" that are between 5 and 10, including 5
     * but excluding 10.
     */
    public Query range(Object floor, Object ceiling, boolean includeFloor, boolean includeCeiling) {
        checkKeyExists();

        String lessThan = includeCeiling ? "$lte" : "$lt";
        String greaterThan = includeFloor ? "$gte" : "$gt";
        Map rangeMap = makeMap(lessThan, ceiling);
        rangeMap.putAll(makeMap(greaterThan, floor));

        queryMap.put(nextKey, rangeMap);
        nextKey = "";
        return this;
    }

    /*
     * Checks to see if an array stored in the database has all of the values
     * passed in @param values.
     * Usage: new Query().start("someKey").all(new Object[]{1,3,5}). This will
     * find arrays stored in "someKey" that contain 1, 3, and 5. "someKey" may
     * contain more than these three integers, but can not be missing one of them.
     */
    public Query all(Object... values) {
        checkKeyExists();
        queryMap.put(nextKey, makeMap("$all", values));
        nextKey = "";
        return this;
    }

    /*
     * Checks to see if an array stored in the database has at least one of the
     * values passed in @param values.
     * Usage: new Query().start("someKey").in(new Object[]{1,3,5}). This will
     * find arrays stored in "someKey" that contain 1, 3, OR 5.
     */
    public Query in(Object... values) {
        checkKeyExists();
        queryMap.put(nextKey, makeMap("$in", values));
        nextKey = "";
        return this;
    }

    /*
     * Checks to see if an array stored in the database contains none of the
     * values passed in @param values.
     * Usage: new Query().start("someKey").notIn(new Object[]{1, 3, 5}). This
     * will find arrays stored in "someKey" that DO NOT CONTAIN 1, 3, and 5.
     */
    public Query notIn(Object... values) {
        checkKeyExists();
        queryMap.put(nextKey, makeMap("$nin", values));
        nextKey =  "";
        return this;
    }

    /*
     * Add an OR clause to the query.
     */
    public Query or(Query... queries) {
        Map[] queryMaps = new Map[queries.length];
        int index = 0;

        for (Query query : queries) {
            queryMaps[index++] = query.getQueryMap();
        }

        queryMap.put("$or", queryMaps);

        return this;
    }

    /*
     * Filter fields based on a regular expression.
     * Usage: new Query().start("someKey").regexp("test"). This will match
     * values stored in "someKey" that match the regular expression "test". So
     * "testFood" would match but "random" would not.
     */
    public Query regexp(String regex) {
        checkKeyExists();
        queryMap.put(nextKey, regex);
        regexpKeys.add(nextKey);
        nextKey = "";

        return this;
    }

    /*
     * This is used by the database to transform String's into Pattern's. This
     * enables the database to do this quicker than flipping through each
     * key and looking for a regular expression.
     */
    public List<String> getRegularExpressionKeys() {
        return regexpKeys;
    }

    /*
     * Add a sort to the query.
     * @param key : Key on which to sort on.
     * @param sort : DESC OR ASC
     */
    public Query addSort(String key, Sort sort) {
        sortMap.put(key, sort.equals(Sort.DESC) ? -1 : 1);
        return this;
    }

    /*
     * This is mostly for debugging, but should return a string that represents
     * the query as a JS Object.
     */
    @Override
    public String toString() {
        String output = "{";

        for (String key : queryMap.keySet()) {
            output += " " + key + " : " + queryMap.get(key) + ",";
        }

        //remove last comma
        output = output.substring(0, output.lastIndexOf(","));

        output += "}";
        return output;
    }

    /*
     * A conveince method ot make a map containing one key and value.
     */
    private Map<String, Object> makeMap(String key, Object value) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put(key, value);
        return map;
    }

    /*
     * Convenience method to make sure we have a key stored before trying
     * to specify a filter. For instance, if we do the following
     * new Query().is("someValue") a KeyNotSetException will be thrown because
     * no key was set.
     */
    private void checkKeyExists() {
        if (nextKey.equals("")) {
            throw new KeyNotSetException();
        }
    }

    /*
     * Get the query as a map of key value pairs. Note that the Object stored
     * in the map may be multipe things: Strings, Integers, Arrays, Queries,
     * etc...
     */
    public Map<String, Object> getQueryMap() {
        return queryMap;
    }

    /*
     * Get a sort map.
     */
    public Map<String, Integer> getSortMap() {
        return sortMap;
    }
}
