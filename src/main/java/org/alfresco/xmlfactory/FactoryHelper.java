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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;

/**
 * The configuration is taken from https://www.owasp.org/index.php/XML_External_Entity_(XXE)_Prevention_Cheat_Sheet#Java
 */
public class FactoryHelper
{
    private static final Log logger = LogFactory.getLog(FactoryHelper.class);

    public final static String FEATURE_EXTERNAL_GENERAL_ENTITIES = "http://xml.org/sax/features/external-general-entities";
    public final static String FEATURE_EXTERNAL_PARAMETER_ENTITIES = "http://xml.org/sax/features/external-parameter-entities";
    public final static String FEATURE_USE_ENTITY_RESOLVER2 = "http://xml.org/sax/features/use-entity-resolver2";
    public final static String FEATURE_LOAD_EXTERNAL_DTD = "http://apache.org/xml/features/nonvalidating/load-external-dtd";
    public final static String FEATURE_DISALLOW_DOCTYPE = "http://apache.org/xml/features/disallow-doctype-decl";
    
    public final static List<String> DEFAULT_FEATURES_TO_DISABLE = Collections.unmodifiableList(new ArrayList<String>(
            Arrays.asList(FEATURE_EXTERNAL_GENERAL_ENTITIES, 
                    FEATURE_EXTERNAL_PARAMETER_ENTITIES,
                    FEATURE_USE_ENTITY_RESOLVER2,
                    FEATURE_LOAD_EXTERNAL_DTD)));

    public final static List<String> DEFAULT_FEATURES_TO_ENABLE = Collections.unmodifiableList(new ArrayList<String>(
            Arrays.asList(XMLConstants.FEATURE_SECURE_PROCESSING,
                    FEATURE_DISALLOW_DOCTYPE)));

    /* white list of classes that can use the parsers with no security restrictions */
    public final static List<String> WHITE_LIST_CALLERS = Collections.unmodifiableList(new ArrayList<String>(
           Arrays.asList(
                    "com.sun.xml.ws.transport.http.servlet.WSServletContextListener",
                    "org.springframework.beans.factory.xml.XmlBeanDefinitionReader",
                    "org.apache.myfaces.config.FacesConfigurator",
                    "org.hibernate.cfg.Configuration",
                    "org.alfresco.ibatis.HierarchicalXMLConfigBuilder",
                    "org.alfresco.repo.security.permissions.impl.model.PermissionModel",
                    "org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl"
                    )));

    public static void configureFactory(DocumentBuilderFactory factory,
            List<String> featuresToEnable, List<String> featuresToDisable)
    {
        if (!isCallInWhiteList())
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
            applyAdditionalFeatures(factory);
        }
    }

    private static boolean isCallInWhiteList()
    {
        StackTraceElement[] currentStackTrace = (new Exception()).getStackTrace();
        for (int i = 0; i < currentStackTrace.length; i++)
        {
            for (String className : WHITE_LIST_CALLERS)
            {
                if (currentStackTrace[i].getClassName().equals(className))
                {
                    logger.debug("Found " + className + " in white list.");
                    return true;
                }
            }
        }
        return false;
    }

    private static void applyAdditionalFeatures(DocumentBuilderFactory factory)
    {
        try
        {
            factory.setXIncludeAware(false);
            factory.setExpandEntityReferences(false);
        }
        catch (Exception e)
        {
            logConfigurationFailure(factory.getClass().getName(), e);
        }
    }

    private static void setFeature(DocumentBuilderFactory factory, String feature, boolean enable)
    {
        try
        {
            factory.setFeature(feature, enable);
        }
        catch (ParserConfigurationException pce)
        {
            logConfigurationFailure(factory.getClass().getName(), feature, pce);
        }
    }

    private static void setFeature(SAXParserFactory factory, String feature, boolean enable)
    {
        try
        {
            factory.setFeature(feature, enable);
        }
        catch (ParserConfigurationException pce)
        {
            logConfigurationFailure(factory.getClass().getName(), feature, pce);
        }
        catch (SAXNotRecognizedException nre)
        {
            logConfigurationFailure(factory.getClass().getName(), feature, nre);
        }
        catch (SAXNotSupportedException nse)
        {
            logConfigurationFailure(factory.getClass().getName(), feature, nse);
        }
    }

    private static void logConfigurationFailure(String factoryName, String feature, Exception e)
    {
        if (logger.isWarnEnabled())
        {
            logger.warn("Failed to configure " + factoryName + " with feature: " + feature, e);
        }
    }

    private static void logConfigurationFailure(String factoryName, Exception e)
    {
        if (logger.isWarnEnabled())
        {
            logger.warn("Failed to configure " + factoryName, e);
        }
    }

    public static void configureFactory(SAXParserFactory factory,
            List<String> featuresToEnable, List<String> featuresToDisable) {
        if (!isCallInWhiteList()) {
            if (featuresToEnable != null) {
                for (String featureToEnable : featuresToEnable) {
                    setFeature(factory, featureToEnable, true);
                }
            }
            if (featuresToDisable != null) {
                for (String featureToDisable : featuresToDisable) {
                    setFeature(factory, featureToDisable, false);
                }
            }
        }
    }
}
