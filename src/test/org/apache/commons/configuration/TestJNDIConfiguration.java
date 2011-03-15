/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.commons.configuration;

import java.util.Hashtable;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;

import junit.framework.TestCase;

import org.apache.commons.configuration.event.ConfigurationErrorListener;

/**
 * Test to see if the JNDIConfiguration works properly.
 *
 * @version $Id$
 */
public class TestJNDIConfiguration extends TestCase {

    public static final String CONTEXT_FACTORY = MockInitialContextFactory.class.getName();

    private PotentialErrorJNDIConfiguration conf;
    private NonStringTestHolder nonStringTestHolder;

    /** A test error listener for counting internal errors.*/
    private ConfigurationErrorListenerImpl listener;

    public void setUp() throws Exception {

        System.setProperty("java.naming.factory.initial", CONTEXT_FACTORY);

        Properties props = new Properties();
        props.put("java.naming.factory.initial", CONTEXT_FACTORY);
        Context ctx = new InitialContext(props);
        conf = new PotentialErrorJNDIConfiguration(ctx);

        nonStringTestHolder = new NonStringTestHolder();
        nonStringTestHolder.setConfiguration(conf);

        listener = new ConfigurationErrorListenerImpl();
        conf.addErrorListener(listener);
    }

    /**
     * Clears the test environment. If an error listener is defined, checks
     * whether no error event was received.
     */
    protected void tearDown() throws Exception
    {
        if (listener != null)
        {
            listener.verify();
        }
        super.tearDown();
    }

    public void testBoolean() throws Exception {
        nonStringTestHolder.testBoolean();
    }

    public void testBooleanDefaultValue() throws Exception {
        nonStringTestHolder.testBooleanDefaultValue();
    }

    public void testByte() throws Exception {
        nonStringTestHolder.testByte();
    }

    public void testDouble() throws Exception {
        nonStringTestHolder.testDouble();
    }

    public void testDoubleDefaultValue() throws Exception {
        nonStringTestHolder.testDoubleDefaultValue();
    }

    public void testFloat() throws Exception {
        nonStringTestHolder.testFloat();
    }

    public void testFloatDefaultValue() throws Exception {
        nonStringTestHolder.testFloatDefaultValue();
    }

    public void testInteger() throws Exception {
        nonStringTestHolder.testInteger();
    }

    public void testIntegerDefaultValue() throws Exception {
        nonStringTestHolder.testIntegerDefaultValue();
    }

    public void testLong() throws Exception {
        nonStringTestHolder.testLong();
    }

    public void testLongDefaultValue() throws Exception {
        nonStringTestHolder.testLongDefaultValue();
    }

    public void testShort() throws Exception {
        nonStringTestHolder.testShort();
    }

    public void testShortDefaultValue() throws Exception {
        nonStringTestHolder.testShortDefaultValue();
    }

    public void testListMissing() throws Exception {
        nonStringTestHolder.testListMissing();
    }

    public void testSubset() throws Exception {
        nonStringTestHolder.testSubset();
    }

    public void testProperties() throws Exception {
        Object o = conf.getProperty("test.boolean");
        assertNotNull(o);
        assertEquals("true", o.toString());
    }

    public void testContainsKey()
    {
        String key = "test.boolean";
        assertTrue("'" + key + "' not found", conf.containsKey(key));

        conf.clearProperty(key);
        assertFalse("'" + key + "' still found", conf.containsKey(key));
    }

    public void testChangePrefix()
    {
        assertEquals("'test.boolean' property", "true", conf.getString("test.boolean"));
        assertEquals("'boolean' property", null, conf.getString("boolean"));

        // change the prefix
        conf.setPrefix("test");
        assertEquals("'test.boolean' property", null, conf.getString("test.boolean"));
        assertEquals("'boolean' property", "true", conf.getString("boolean"));
    }

    public void testResetRemovedProperties() throws Exception
    {
        assertEquals("'test.boolean' property", "true", conf.getString("test.boolean"));

        // remove the property
        conf.clearProperty("test.boolean");
        assertEquals("'test.boolean' property", null, conf.getString("test.boolean"));

        // change the context
        conf.setContext(new InitialContext());

        // get the property
        assertEquals("'test.boolean' property", "true", conf.getString("test.boolean"));
    }

    public void testConstructor() throws Exception
    {
        // test the constructor accepting a context
        JNDIConfiguration c = new JNDIConfiguration(new InitialContext());

        assertEquals("'test.boolean' property", "true", c.getString("test.boolean"));

        // test the constructor accepting a context and a prefix
        c = new JNDIConfiguration(new InitialContext(), "test");

        assertEquals("'boolean' property", "true", c.getString("boolean"));
    }

    /**
     * Configures the test config to throw an exception.
     */
    private PotentialErrorJNDIConfiguration setUpErrorConfig()
    {
        conf.installException();
        conf.removeErrorListener((ConfigurationErrorListener) conf
                .getErrorListeners().iterator().next());
        return (PotentialErrorJNDIConfiguration) conf;
    }

    /**
     * Tests whether the expected error events have been received.
     *
     * @param type the expected event type
     * @param propName the name of the property
     * @param propValue the property value
     */
    private void checkErrorListener(int type, String propName, Object propValue)
    {
        listener.verify(type, propName, propValue);
        assertTrue("Wrong exception class",
                listener.getLastEvent().getCause() instanceof NamingException);
        listener = null;
    }

    /**
     * Tests whether a JNDI configuration registers an error log listener.
     */
    public void testLogListener() throws NamingException
    {
        JNDIConfiguration c = new JNDIConfiguration();
        assertEquals("No error log listener registered", 1, c
                .getErrorListeners().size());
    }

    /**
     * Tests handling of errors in getKeys().
     */
    public void testGetKeysError()
    {
        assertFalse("Iteration not empty", setUpErrorConfig().getKeys()
                .hasNext());
        checkErrorListener(AbstractConfiguration.EVENT_READ_PROPERTY, null,
                null);
    }

    /**
     * Tests handling of errors in isEmpty().
     */
    public void testIsEmptyError() throws NamingException
    {
        assertTrue("Error config not empty", setUpErrorConfig().isEmpty());
        checkErrorListener(AbstractConfiguration.EVENT_READ_PROPERTY, null,
                null);
    }

    /**
     * Tests handling of errors in the containsKey() method.
     */
    public void testContainsKeyError()
    {
        assertFalse("Key contained after error", setUpErrorConfig()
                .containsKey("key"));
        checkErrorListener(AbstractConfiguration.EVENT_READ_PROPERTY, "key",
                null);
    }

    /**
     * Tests handling of errors in getProperty().
     */
    public void testGetPropertyError()
    {
        assertNull("Wrong property value after error", setUpErrorConfig()
                .getProperty("key"));
        checkErrorListener(AbstractConfiguration.EVENT_READ_PROPERTY, "key",
                null);
    }

    /**
     * Tests the getKeys() method when there are cycles in the tree.
     */
    public void testGetKeysWithCycles() throws NamingException
    {
        Hashtable env = new Hashtable();
        env.put(MockInitialContextFactory.PROP_CYCLES, Boolean.TRUE);
        InitialContext initCtx = new InitialContext(env);
        JNDIConfiguration c = new JNDIConfiguration(initCtx);
        c.getKeys("cycle");
    }

    /**
     * Tests getKeys() if no data is found. This should not cause a problem and
     * not notify the error listeners.
     */
    public void testGetKeysNoData()
    {
        conf.installException(new NameNotFoundException("Test exception"));
        assertFalse("Got keys", conf.getKeys().hasNext());
        listener.verify();
    }

    /**
     * A special JNDI configuration implementation that can be configured to
     * throw an exception when accessing the base context. Used for testing the
     * exception handling.
     */
    public static class PotentialErrorJNDIConfiguration extends
            JNDIConfiguration
    {
        /** An exception to be thrown by getBaseContext(). */
        private NamingException exception;

        public PotentialErrorJNDIConfiguration(Context ctx)
                throws NamingException
        {
            super(ctx);
        }

        /**
         * Prepares this object to throw an exception when the JNDI context is
         * queried.
         *
         * @param nex the exception to be thrown
         */
        public void installException(NamingException nex)
        {
            exception = nex;
        }

        /**
         * Prepares this object to throw a standard exception when the JNDI
         * context is queried.
         */
        public void installException()
        {
            installException(new NamingException("Simulated JNDI exception!"));
        }

        /**
         * Returns the JNDI context. Optionally throws an exception.
         */
        public Context getBaseContext() throws NamingException
        {
            if (exception != null)
            {
                throw exception;
            }
            return super.getBaseContext();
        }
    }
}