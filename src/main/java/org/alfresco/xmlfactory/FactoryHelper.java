/*
 * Copyright (C) 2005-2015 Alfresco Software Limited.
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

public class FactoryHelper
{
    private static final Log logger = LogFactory.getLog(FactoryHelper.class);

    public final static String FEATURE_EXTERNAL_GENERAL_ENTITIES = "http://xml.org/sax/features/external-general-entities";
    public final static String FEATURE_EXTERNAL_PARAMETER_ENTITIES = "http://xml.org/sax/features/external-parameter-entities";
    public final static String FEATURE_USE_ENTITY_RESOLVER2 = "http://xml.org/sax/features/use-entity-resolver2";
    public final static String FEATURE_LOAD_EXTERNAL_DTD = "http://apache.org/xml/features/nonvalidating/load-external-dtd";
    
    public final static List<String> DEFAULT_FEATURES_TO_DISABLE = Collections.unmodifiableList(new ArrayList<String>(
            Arrays.asList(FEATURE_EXTERNAL_GENERAL_ENTITIES, 
                    FEATURE_EXTERNAL_PARAMETER_ENTITIES,
                    FEATURE_USE_ENTITY_RESOLVER2,
                    FEATURE_LOAD_EXTERNAL_DTD)));

    public final static List<String> DEFAULT_FEATURES_TO_ENABLE = Collections.unmodifiableList(new ArrayList<String>(
            Arrays.asList(XMLConstants.FEATURE_SECURE_PROCESSING)));

    public static void configureFactory(DocumentBuilderFactory factory,
            List<String> featuresToEnable, List<String> featuresToDisable)
    {
        try
        {
            if (featuresToEnable != null)
            {
                for (String featureToEnable : featuresToEnable)
                {
                    factory.setFeature(featureToEnable, true);
                }
            }
            if (featuresToDisable != null)
            {
                for (String featureToDisable : featuresToDisable)
                {
                    factory.setFeature(featureToDisable, false);
                }
            }
        }
        catch (ParserConfigurationException e)
        {
            //If we get any other exception then we've failed to configure the parser factory as required.
            //Return the factory as is.
            if (logger.isWarnEnabled())
            {
                logger.warn("Failed to configure DocumentBuilderFactory securely.", e);
            }
        }
    }

    public static void configureFactory(SAXParserFactory factory,
            List<String> featuresToEnable, List<String> featuresToDisable)
    {
        try
        {
            if (featuresToEnable != null)
            {
                for (String featureToEnable : featuresToEnable)
                {
                    factory.setFeature(featureToEnable, true);
                }
            }
            if (featuresToDisable != null)
            {
                for (String featureToDisable : featuresToDisable)
                {
                    factory.setFeature(featureToDisable, false);
                }
            }
        }
        catch (RuntimeException rte)
        {
            //If any runtime exception occurs then simply rethrow it
            throw rte;
        } 
        catch (Exception e)
        {
            //If we get any other exception then we've failed to configure the parser factory as required.
            //Return the factory as is.
            if (logger.isWarnEnabled())
            {
                logger.warn("Failed to configure SAXParserFactory securely.", e);
            }
        } 
    }

}
