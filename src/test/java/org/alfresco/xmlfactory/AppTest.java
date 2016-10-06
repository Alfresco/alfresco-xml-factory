/*
 * Copyright (C) 2005-2016 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package org.alfresco.xmlfactory;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.apache.xerces.jaxp.DocumentBuilderFactoryImpl;
import org.apache.xerces.jaxp.SAXParserFactoryImpl;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

/**
 * Unit test for simple App.
 */
public class AppTest 
    extends TestCase
{
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public AppTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( AppTest.class );
    }

    /**
     * Rigourous Test :-)
     */
    public void testApp()
    {
        assertTrue( true );
    }

    /**
     * Test we have set features the way we expect as defaults.
     */
    public void testDocumentBuilderFactory() throws Throwable {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

        assertTrue(dbf.getFeature(XMLConstants.FEATURE_SECURE_PROCESSING));
// TODO uncomment: Having this commented out takes XmlFactory back to how it was before the latest changes
//        assertTrue(dbf.getFeature(FactoryHelper.FEATURE_DISALLOW_DOCTYPE));

        assertFalse(dbf.getFeature(FactoryHelper.FEATURE_EXTERNAL_GENERAL_ENTITIES));
        assertFalse(dbf.getFeature(FactoryHelper.FEATURE_EXTERNAL_PARAMETER_ENTITIES));
        assertFalse(dbf.getFeature(FactoryHelper.FEATURE_USE_ENTITY_RESOLVER2));
        assertFalse(dbf.getFeature(FactoryHelper.FEATURE_LOAD_EXTERNAL_DTD));

// TODO uncomment: Having this commented out takes XmlFactory back to how it was before the latest changes
//        assertFalse(dbf.isExpandEntityReferences());
        assertFalse(dbf.isXIncludeAware());
    }

    /**
     * Test we have set features the way we expect as defaults.
     */
    public void testSAXParserFactory() throws Throwable
    {
        SAXParserFactory spf = SAXParserFactory.newInstance();

        assertTrue(spf.getFeature(XMLConstants.FEATURE_SECURE_PROCESSING));
// TODO uncomment: Having this commented out takes XmlFactory back to how it was before the latest changes
//        assertTrue(spf.getFeature(FactoryHelper.FEATURE_DISALLOW_DOCTYPE));

        assertFalse(spf.getFeature(FactoryHelper.FEATURE_EXTERNAL_GENERAL_ENTITIES));
        assertFalse(spf.getFeature(FactoryHelper.FEATURE_EXTERNAL_PARAMETER_ENTITIES));
        assertFalse(spf.getFeature(FactoryHelper.FEATURE_USE_ENTITY_RESOLVER2));
        assertFalse(spf.getFeature(FactoryHelper.FEATURE_LOAD_EXTERNAL_DTD));

        assertFalse(spf.isXIncludeAware());
    }

    /**
     * Test we have set features the way we expect as defaults.
     */
    public void testDocumentBuilderFactoryInWhiteList() throws Throwable
    {
        // Using constructor rather than the service locator and then using the helper to configure it.
        DocumentBuilderFactory dbf = new DocumentBuilderFactoryImpl();
        FactoryHelper factoryHelper = new FactoryHelper();
        List<String> whiteListClasses = Collections.singletonList(getClass().getName());
        factoryHelper.configureFactory(dbf, FactoryHelper.DEFAULT_FEATURES_TO_ENABLE,
                FactoryHelper.DEFAULT_FEATURES_TO_DISABLE,
                whiteListClasses);

        assertFalse(dbf.getFeature(XMLConstants.FEATURE_SECURE_PROCESSING));
        assertFalse(dbf.getFeature(FactoryHelper.FEATURE_DISALLOW_DOCTYPE));

        assertTrue(dbf.getFeature(FactoryHelper.FEATURE_EXTERNAL_GENERAL_ENTITIES));
        assertTrue(dbf.getFeature(FactoryHelper.FEATURE_EXTERNAL_PARAMETER_ENTITIES));
        assertTrue(dbf.getFeature(FactoryHelper.FEATURE_USE_ENTITY_RESOLVER2));
        assertTrue(dbf.getFeature(FactoryHelper.FEATURE_LOAD_EXTERNAL_DTD));

        assertTrue(dbf.isExpandEntityReferences());
        assertFalse(dbf.isXIncludeAware()); // false is the default so is same as the non whitelist test
    }

    /**
     * Test we have set features the way we expect as defaults.
     */
    public void testSAXParserFactoryInWhiteList() throws Throwable
    {
        // Using constructor rather than the service locator and then using the helper to configure it.
        SAXParserFactory spf = new SAXParserFactoryImpl();
        FactoryHelper factoryHelper = new FactoryHelper();
        List<String> whiteListClasses = Collections.singletonList(getClass().getName());
        factoryHelper.configureFactory(spf, FactoryHelper.DEFAULT_FEATURES_TO_ENABLE,
                FactoryHelper.DEFAULT_FEATURES_TO_DISABLE,
                whiteListClasses);

        assertFalse(spf.getFeature(XMLConstants.FEATURE_SECURE_PROCESSING));
        assertFalse(spf.getFeature(FactoryHelper.FEATURE_DISALLOW_DOCTYPE));

        assertTrue(spf.getFeature(FactoryHelper.FEATURE_EXTERNAL_GENERAL_ENTITIES));
        assertTrue(spf.getFeature(FactoryHelper.FEATURE_EXTERNAL_PARAMETER_ENTITIES));
        assertTrue(spf.getFeature(FactoryHelper.FEATURE_USE_ENTITY_RESOLVER2));
        assertTrue(spf.getFeature(FactoryHelper.FEATURE_LOAD_EXTERNAL_DTD));

        assertFalse(spf.isXIncludeAware()); // false is the default so is same as the non whitelist test
    }

    private class TestFactoryHelper extends FactoryHelper
    {
        final Map<String, Properties> testValues = new HashMap<>();

        /**
         * @param propertyArgs groups of 3: &lt;url>, &lt;propertyName>, &lt;value>. If &lt;url> is null the property
         *        is taken to be a system property.
         */
        TestFactoryHelper(String... propertyArgs)
        {
            for (int i=0; i< propertyArgs.length; i+=3)
            {
                String urlString = propertyArgs[i] == null ? "" : getResource(null, propertyArgs[i]).toString();
                String propertyName = propertyArgs[i+1];
                String value = propertyArgs[i+2];

                Properties properties = testValues.get(urlString);
                if (properties == null)
                {
                    properties = new Properties();
                    testValues.put(urlString, properties);
                }
                properties.put(propertyName, value);
            }
        }

        private String getTestValue(String urlString, String propertyName)
        {
            Properties properties = testValues.get(urlString);
            return properties == null ? null : (String)properties.get(propertyName);
        }

        @Override
        String getJavaHome()
        {
            return "$JAVA_HOME"; // just has to be a non null value.
        }

        @Override
        String getSystemProperty(String propertyName)
        {
            return getTestValue("", propertyName);
        }

        @Override
        URL getResource(ClassLoader loader, String resourceName)
        {
            try
            {
                // This gets the wrong URI (it just prefixes the current directory), but that is okay for testing
                // as we are not really reading files, but just using String values.
                File file = new File(resourceName);
                return file.toURI().toURL();
            }
            catch (MalformedURLException e)
            {
                fail("Error in test code creating URL");
                return null;
            }
        }

        @Override
        String getProperty(URL url, String propertyName)
        {
            return url == null ? null : getTestValue(url.toString(), propertyName);
        }
    }

    /**
     * Test the pick up of configuration properties. We just overload the methods in the helper as putting files in
     * the jre structure or META_INF is not a good idea in an automated test. Has been manually tested. This tests
     * the code around the actual reading of property files and system properties.
     */
    public void testConfigOrder() throws Throwable
    {
        List<String> emptyList = Collections.emptyList();
        List<String> abcDefList = Arrays.asList("abc", "def");

        FactoryHelper factoryHelper = new TestFactoryHelper(
                // Test System Properties: <factory>.<propertyName>
                null, "java.lang.String.name1", "sysProp1",
                null, "java.lang.Object.name1", "sysProp2",
                null, "java.lang.String.name2", "  sysProp3a  ;  sysProp3b;sysProp3c   ",

                // JRE Property file: jre/lib/<factory>.properties
                "$JAVA_HOME/lib/java.lang.String.properties", "name1", "jre1",
                "$JAVA_HOME/lib/java.lang.Object.properties", "name1", "jre2",
                "$JAVA_HOME/lib/java.lang.String.properties", "name3", "jre3",

                // META_INF/services Property file: jre/lib/<factory>.properties
                "META-INF/services/java.lang.String.properties", "name1", "metaInf1",
                "META-INF/services/java.lang.Object.properties", "name1", "metaInf2",
                "META-INF/services/java.lang.String.properties", "name3", "metaInf3",
                "META-INF/services/java.lang.Object.properties", "name3", "metaInf4",
                "META-INF/services/java.lang.String.properties", "name5", "metaInf5",
                "META-INF/services/java.lang.String.properties", "name6", ""
                );

        // Test System Properties
        assertGetConfiguration(factoryHelper, emptyList, String.class, "name1", "sysProp1");
        assertGetConfiguration(factoryHelper, emptyList, String.class, "name0");

        // Test JRE Property file
        assertGetConfiguration(factoryHelper, emptyList, String.class, "name3", "jre3");

        // META_INF/services Property file
        assertGetConfiguration(factoryHelper, emptyList, String.class, "name5", "metaInf5");

        // Test the default list
        assertGetConfiguration(factoryHelper, abcDefList, String.class, "name7", "abc", "def");

        // Test zero length values - they should override the default
        assertGetConfiguration(factoryHelper, emptyList,  String.class, "name6");
        assertGetConfiguration(factoryHelper, abcDefList, String.class, "name6");

        // Test multiple values in property value and trim of spaces
        assertGetConfiguration(factoryHelper, emptyList, String.class, "name2", "sysProp3a", "sysProp3b", "sysProp3c");
    }

    private void assertGetConfiguration(FactoryHelper fh, List<String> defaultValues, Class clazz, String propertyName, String... expected)
    {
        List<String> list = fh.getConfiguration(clazz, propertyName, defaultValues);

        for (int i=0; i < expected.length && i < list.size(); i++)
        {
            assertEquals(expected[i], list.get(i));
        }
        assertEquals(list.toString(), expected.length, list.size());
    }
}
