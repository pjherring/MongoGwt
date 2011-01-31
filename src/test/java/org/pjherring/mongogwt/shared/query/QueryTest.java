/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pjherring.mongogwt.shared.query;

import java.util.List;
import java.util.Map;
import org.easymock.EasyMockSupport;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author pjherring
 */
public class QueryTest extends EasyMockSupport {

    public QueryTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    public static class QueryTestHelper extends Query {

        public void setMap(Map<String, Object> queryMap) {
            this.queryMap = queryMap;
        }
    }

    @Test
    public void test_is() {
        Query query = new Query();
        query.start("1").is("one");
        assertEquals("one", query.getQueryMap().get("1"));
    }

    @Test
    public void test_And() {
        Query query = new Query();
        query.start("one").is("one")
            .and("two").is("two");
        assertEquals("two", query.getQueryMap().get("two"));
        assertEquals("one", query.getQueryMap().get("one"));
    }

    @Test
    public void test_greaterThan_lessThan_equals() {
        Query query = new Query()
            .start("one").greaterThen(1);
        assertTrue(Map.class.isAssignableFrom(query.getQueryMap().get("one").getClass()));
        assertEquals("$gt", ((Map<String, Object>) query.getQueryMap().get("one")).keySet().iterator().next());
        assertEquals(1, ((Map<String, Object>) query.getQueryMap().get("one")).get("$gt"));

        query = new Query()
            .start("one").greaterThenEquals(1);
        assertTrue(Map.class.isAssignableFrom(query.getQueryMap().get("one").getClass()));
        assertEquals("$gte", ((Map<String, Object>) query.getQueryMap().get("one")).keySet().iterator().next());
        assertEquals(1, ((Map<String, Object>) query.getQueryMap().get("one")).get("$gte"));

        query = new Query()
            .start("one").lessThen(1);
        assertTrue(Map.class.isAssignableFrom(query.getQueryMap().get("one").getClass()));
        assertEquals("$lt", ((Map<String, Object>) query.getQueryMap().get("one")).keySet().iterator().next());
        assertEquals(1, ((Map<String, Object>) query.getQueryMap().get("one")).get("$lt"));

        query = new Query()
            .start("one").lessThenEquals(1);
        assertTrue(Map.class.isAssignableFrom(query.getQueryMap().get("one").getClass()));
        assertEquals("$lte", ((Map<String, Object>) query.getQueryMap().get("one")).keySet().iterator().next());
        assertEquals(1, ((Map<String, Object>) query.getQueryMap().get("one")).get("$lte"));

    }

    @Test
    public void test_range() {
        Query query = new Query()
            .start("key").range(1, 5);

        assertTrue(Map.class.isAssignableFrom(query.getQueryMap().get("key").getClass()));
        Map<String, Object> rangeMap = (Map<String, Object>) query.getQueryMap().get("key");

        for (String key : rangeMap.keySet()) {
            assertTrue(key, key.equals("$gte") || key.equals("$lte"));
            Integer value = (Integer) rangeMap.get(key);

            assertTrue(
                (key.equals("$gte") && value.equals(1))
                || (value.equals(5) && key.equals("$lte"))
            );
        }

        query = new Query()
            .start("key").range(1, 5, false, false);

        assertTrue(Map.class.isAssignableFrom(query.getQueryMap().get("key").getClass()));
        rangeMap = (Map<String, Object>) query.getQueryMap().get("key");

        for (String key : rangeMap.keySet()) {
            assertTrue(key, key.equals("$gt") || key.equals("$lt"));
            Integer value = (Integer) rangeMap.get(key);

            assertTrue(
                (key.equals("$gt") && value.equals(1))
                || (value.equals(5) && key.equals("$lt"))
            );
        }
    }

    @Test
    public void test_all() {
        Query query = new Query()
            .start("key").all("one", "two", "three");
        Map<String, Object> queryMap = query.getQueryMap();
        assertTrue(Map.class.isAssignableFrom(queryMap.get("key").getClass()));
        Map<String, Object> keyValue = (Map<String, Object>) queryMap.get("key");
        assertEquals("$all", keyValue.keySet().iterator().next());
        assertEquals(Object[].class, keyValue.get("$all").getClass());
        Object[] stored = (Object[]) keyValue.get("$all");
        assertArrayEquals(stored, new String[]{"one", "two", "three"});
    }

    @Test
    public void test_in() {
        Query query = new Query()
            .start("key").in("one", "two", "three");
        Map<String, Object> queryMap = query.getQueryMap();
        assertTrue(Map.class.isAssignableFrom(queryMap.get("key").getClass()));
        Map<String, Object> keyValue = (Map<String, Object>) queryMap.get("key");
        assertEquals("$in", keyValue.keySet().iterator().next());
        assertEquals(Object[].class, keyValue.get("$in").getClass());
        Object[] stored = (Object[]) keyValue.get("$in");
        assertArrayEquals(stored, new String[]{"one", "two", "three"});
    }

    @Test
    public void test_notIn() {
        Query query = new Query()
            .start("key").notIn("one", "two", "three");
        Map<String, Object> queryMap = query.getQueryMap();
        assertTrue(Map.class.isAssignableFrom(queryMap.get("key").getClass()));
        Map<String, Object> keyValue = (Map<String, Object>) queryMap.get("key");
        assertEquals("$nin", keyValue.keySet().iterator().next());
        assertEquals(Object[].class, keyValue.get("$nin").getClass());
        Object[] stored = (Object[]) keyValue.get("$nin");
        assertArrayEquals(stored, new String[]{"one", "two", "three"});
    }

    @Test
    public void test_or() {
        Query query = new Query()
            .start("key").is("something")
            .or(new Query().start("key").is("somethingelse"));
        Map<String, Object> queryMap = query.getQueryMap();
        assertEquals("something", queryMap.get("key"));
        assertTrue(Map[].class.isAssignableFrom(queryMap.get("$or").getClass()));
        Map[] orQueryMaps = (Map[]) queryMap.get("$or");
        assertEquals(1, orQueryMaps.length);
        assertEquals("somethingelse", orQueryMaps[0].get("key"));
    }

    @Test
    public void test_regex() {
        Query query = new Query()
            .start("key").regexp("regex");
        List<String> regexKeys = query.getRegularExpressionKeys();
        assertEquals("key", regexKeys.get(0));
        Map<String, Object> queryMap = query.getQueryMap();
        assertEquals("regex", queryMap.get("key"));
    }

    @Test
    public void testSort() {
        Query query = new Query()
            .start("key").is("value")
            .addSort("key", Query.Sort.DESC);
        Map<String, Integer> sortMap = query.getSortMap();
        assertTrue(-1 == sortMap.get("key"));
    }
}