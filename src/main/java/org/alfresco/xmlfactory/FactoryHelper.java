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

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;

/**
 * The configuration is taken from https://www.owasp.org/index.php/XML_External_Entity_(XXE)_Prevention_Cheat_Sheet#Java
 */
class FactoryHelper
{
    private static final Log logger = LogFactory.getLog(FactoryHelper.class);

    final static String FEATURE_EXTERNAL_GENERAL_ENTITIES = "http://xml.org/sax/features/external-general-entities";
    final static String FEATURE_EXTERNAL_PARAMETER_ENTITIES = "http://xml.org/sax/features/external-parameter-entities";
    final static String FEATURE_USE_ENTITY_RESOLVER2 = "http://xml.org/sax/features/use-entity-resolver2";
    final static String FEATURE_LOAD_EXTERNAL_DTD = "http://apache.org/xml/features/nonvalidating/load-external-dtd";

    // There is no standards for these values. Note they don't start with http:
    private final static String ADDITIONAL_FEATURE_X_INCLUDE_AWARE = "xIncludeAware";
    private final static String ADDITIONAL_FEATURE_EXPAND_ENTITY_REFERENCES = "expandEntityReferences";

    final static String FEATURE_DISALLOW_DOCTYPE = "http://apache.org/xml/features/disallow-doctype-decl";
    
    final static List<String> DEFAULT_FEATURES_TO_DISABLE = Collections.unmodifiableList(new ArrayList<>(
            Arrays.asList(
                    FEATURE_EXTERNAL_GENERAL_ENTITIES,
                    FEATURE_EXTERNAL_PARAMETER_ENTITIES,
                    FEATURE_USE_ENTITY_RESOLVER2,
                    FEATURE_LOAD_EXTERNAL_DTD,

                    ADDITIONAL_FEATURE_X_INCLUDE_AWARE,
                    ADDITIONAL_FEATURE_EXPAND_ENTITY_REFERENCES
                    )));

    final static List<String> DEFAULT_FEATURES_TO_ENABLE = Collections.unmodifiableList(new ArrayList<>(
            Arrays.asList(
                    XMLConstants.FEATURE_SECURE_PROCESSING
                    // Disllowing DOCTYPE disables too much in terms of transformations
                    // , FEATURE_DISALLOW_DOCTYPE
                    )));

    /* white list of classes that can use the parsers with no security restrictions */
    final static List<String> DEFAULT_WHITE_LIST_CALLERS = Collections.unmodifiableList(new ArrayList<>(
           Arrays.asList(
                    "com.sun.xml.ws.transport.http.servlet.WSServletContextListener",
                    "org.springframework.beans.factory.xml.XmlBeanDefinitionReader",
                    "org.springframework.beans.factory.support.AbstractBeanFactory",
                    "org.apache.myfaces.config.FacesConfigurator",
                    "org.hibernate.cfg.Configuration",
                    "org.alfresco.ibatis.HierarchicalXMLConfigBuilder",
                    "org.alfresco.repo.security.permissions.impl.model.PermissionModel",
                    "org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl"
                    )));

    // Property names used to configure the factories
    static final String FEATURES_TO_ENABLE  = "features.to.enable";
    static final String FEATURES_TO_DISABLE = "features.to.disable";
    static final String WHITE_LIST_CALLERS =  "white.list.callers";

    private static volatile int counter = 1;
    private int debugCounter = counter++;

    private final int STACK_DEPTH = 30;

    void configureFactory(DocumentBuilderFactory factory, List<String> featuresToEnable,
                                 List<String> featuresToDisable, List<String> whiteListCallers)
    {
        debugStack("DocumentBuilderFactory newInstance", 3, STACK_DEPTH);
        if (!isCallInWhiteList(whiteListCallers))
        {
            if (featuresToEnable != null)
            {
                for (String featureToEnable : featuresToEnable)
                {
                    setFeature(factory, featureToEnable, true);
                }
            }
            if (featuresToDisable != null)
            {
                for (String featureToDisable : featuresToDisable)
                {
                    setFeature(factory, featureToDisable, false);
                }
            }
        }
    }

    void configureFactory(SAXParserFactory factory, List<String> featuresToEnable,
                                 List<String> featuresToDisable, List<String> whiteListCallers)
    {
        debugStack("SAXParserFactory newInstance", 3, STACK_DEPTH);
        if (!isCallInWhiteList(whiteListCallers))
        {
            if (featuresToEnable != null)
            {
                for (String featureToEnable : featuresToEnable)
                {
                    setFeature(factory, featureToEnable, true);
                }
            }
            if (featuresToDisable != null)
            {
                for (String featureToDisable : featuresToDisable)
                {
                    setFeature(factory, featureToDisable, false);
                }
            }
        }
    }

    private boolean isCallInWhiteList(List<String> whiteListCallers)
    {
        StackTraceElement[] currentStackTrace = (new Exception()).getStackTrace();
        for (StackTraceElement clazz: currentStackTrace)
        {
            String currentClassName = clazz.getClassName();
            for (String className : whiteListCallers)
            {
                if (currentClassName.equals(className))
                {
                    debug(debugCounter+" Found " + className + " in white list.");
                    return true;
                }
            }
        }
        return false;
    }

    private void debugStack(String message, int fromStackDepth, int maxStackDepth) {
        if (logger.isDebugEnabled())
        {
            debug(debugCounter+" "+message);

            StackTraceElement[] currentStackTrace = (new Exception()).getStackTrace();
            StringJoiner stackDebug = new StringJoiner("\n");
            for (int i=fromStackDepth; i<currentStackTrace.length && i<maxStackDepth+fromStackDepth; i++)
            {
                StackTraceElement frame = currentStackTrace[i];
                stackDebug.add(debugCounter+"   "+frame);
            }
            debug(stackDebug.toString());
        }
    }

    private void setFeature(DocumentBuilderFactory factory, String feature, boolean enable)
    {
        try
        {
            if (ADDITIONAL_FEATURE_X_INCLUDE_AWARE.equals(feature))
            {
                factory.setXIncludeAware(enable);
            }
            else if (ADDITIONAL_FEATURE_EXPAND_ENTITY_REFERENCES.equals(feature))
            {
                factory.setExpandEntityReferences(enable);
            }
            else
            {
                factory.setFeature(feature, enable);
            }
            debug(debugCounter+" DocumentBuilderFactory "+feature+" "+enable);
        }
        catch (ParserConfigurationException pce)
        {
            logConfigurationFailure(factory.getClass().getName(), feature, pce);
        }
    }

    private void setFeature(SAXParserFactory factory, String feature, boolean enable)
    {
        try
        {
            if (ADDITIONAL_FEATURE_X_INCLUDE_AWARE.equals(feature))
            {
                factory.setXIncludeAware(enable);
            }
            else if (!ADDITIONAL_FEATURE_EXPAND_ENTITY_REFERENCES.equals(feature)) // Does not exist on SAXParserFactory
            {
                factory.setFeature(feature, enable);
            }
            debug(debugCounter+" SAXParserFactory "+feature+" "+enable);
        }
        catch (ParserConfigurationException | SAXNotSupportedException | SAXNotRecognizedException e)
        {
            logConfigurationFailure(factory.getClass().getName(), feature, e);
        }
    }

    public void debugNewParser(DocumentBuilder parser)
    {
        debugStack("New DocumentBuilder", 3, STACK_DEPTH);
    }

    public void debugNewParser(SAXParser parser)
    {
        debugStack("New SAXParser", 3, STACK_DEPTH);
    }

    private void logConfigurationFailure(String factoryName, String feature, Exception e)
    {
        if (logger.isWarnEnabled())
        {
            logger.warn("Failed to configure " + factoryName + " with feature: " + feature, e);
        }
    }

    /**
     * Returns a List of features (to be enabled or disabled) or class names (to be included in a caller white list) for
     * a factory. A similar approach to the one used to select the JAXP factories in the first place is used to find a
     * property value for each configurable value. The property names are: {@code}features.to.enable{@code},
     * {@code}features.to.disable{@code} and {@code}white.list.callers{@code}. The following order is used to find a
     * semicolon separated list of values for each property:
     * <li>A system property {@code}&lt;factoryName>.<propertyName>{@code} if it exists and is accessible.</li>
     * <li>A property in {@code}$JAVA_HOME/lib/&lt;factoryName>.properties{@code} if it exists.</li>
     * <li>A property in {@code}META-INF/services/&lt;factoryName>.properties{@code} if it exists.</li>
     * <li>The {@code}deafultFeatures{@code} parameter passed to this method.</li>
     *
     * @param factoryClass used to look up the &lt;factoryName>.
     * @param propertyName used as the property name in files or as the suffix in a sysme property.
     * @param defaultFeatures to be returned if other values are not found.
     * @return the list of features or class names.
     */
    List<String> getConfiguration(Class<?> factoryClass, String propertyName, List<String> defaultFeatures)
    {
        List<String> features = defaultFeatures;

        String factoryName = factoryClass.getName();
        String extendedPropertyName = factoryName+'.'+propertyName;

        // Look for values in <factoryName>.enable or <factoryName>.disable
        String value = null;
        try
        {
            value = getSystemProperty(extendedPropertyName);
            debugPropertyFrom(propertyName, value, "-D"+extendedPropertyName);
        }
        catch (SecurityException e)
        {
            debug(debugCounter+" Error reading system property:"+extendedPropertyName, e);
        }

        // Look for values in $JAVA_HOME/jre/lib/<factoryName>.properties.
        if (value == null)
        {
            String javaHome = getJavaHome();
            if (javaHome != null)
            {
                File file = new File(new File(new File(javaHome), "lib"), factoryName+".properties");
                try
                {
                    URL url = file.toURI().toURL();
                    value = getProperty(url, propertyName);
                    debugPropertyFrom(propertyName, value, file);
                }
                catch (MalformedURLException e)
                {
                    debug(debugCounter+" Error creating URL for:"+file, e);
                }
            }
        }

        // Look for values in META-INF/services/<factoryName>.properties.
        if (value == null)
        {
            String resourceName = "META-INF/services/" + factoryName+".properties";
            URL url = getResource(null, resourceName);
            value = getProperty(url, propertyName);
            debugPropertyFrom(propertyName, value, resourceName);
        }

        // Add features to a new List
        if (value != null)
        {
            features = new ArrayList<>();
            value = value.trim();
            if (!value.isEmpty())
            {
                for (String feature: value.split(";"))
                {
                    features.add(feature.trim());
                }
            }
        }

        return features;
    }

    private void debugPropertyFrom(String propertyName, String value, Object source) {
        if (logger.isDebugEnabled())
        {
            if (value != null)
            {
                debug(debugCounter+" "+propertyName+" "+value+" loaded from "+ source);
            }
        }
    }

    String getJavaHome()
    {
        return System.getProperty("java.home");
    }

    String getSystemProperty(String propertyName)
    {
        return System.getProperty(propertyName);
    }

    URL getResource(ClassLoader loader, String resourceName)
    {
        return loader == null
                ? ClassLoader.getSystemResource(resourceName)
                : loader.getResource(resourceName);
    }

    String getProperty(URL url, String propertyName)
    {
        String value = null;
        if (url != null)
        {
            Properties properties = getProperties(url);
            value = properties.getProperty(propertyName);
        }
        return value;
    }

    private Properties getProperties(URL url)
    {
        Properties properties = new Properties();
        InputStream in = null;
        Reader reader = null;
        try
        {
            in = url.openStream();
            reader = new InputStreamReader(in, "UTF-8");
            properties.load(reader);
        }
        catch (IOException e)
        {
            debug(debugCounter+" Error reading :"+url, e);
        }
        finally
        {
            if (reader != null)
            {
                try
                {
                    reader.close();
                }
                catch (IOException e)
                {
                    // ignore
                }
            }
            if (in != null)
            {
                try
                {
                    in.close();
                }
                catch (IOException e)
                {
                    // ignore
                }
            }
        }
        return properties;
    }

    private void debug(String message)
    {
        logger.debug(message);
        // System.out.println(message);
    }

    private void debug(String message, Exception e)
    {
        logger.debug(message, e);
        // System.out.println(message);
    }
}
