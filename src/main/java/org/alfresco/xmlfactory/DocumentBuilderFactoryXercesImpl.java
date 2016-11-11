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

import java.util.List;

import org.apache.xerces.jaxp.DocumentBuilderFactoryImpl;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class DocumentBuilderFactoryXercesImpl extends DocumentBuilderFactoryImpl
{
    // Static so we only do this configuration lookup once
    private static FactoryHelper factoryHelper = new FactoryHelper();
    private static List<String> FEATURES_TO_ENABLE = factoryHelper.getConfiguration(DocumentBuilderFactory.class,
                  FactoryHelper.FEATURES_TO_ENABLE,  FactoryHelper.DEFAULT_FEATURES_TO_ENABLE);
    private static List<String> FEATURES_TO_DISABLE = factoryHelper.getConfiguration(DocumentBuilderFactory.class,
                  FactoryHelper.FEATURES_TO_DISABLE, FactoryHelper.DEFAULT_FEATURES_TO_DISABLE);
    private static List<String> WHITE_LIST_CALLERS = factoryHelper.getConfiguration(DocumentBuilderFactory.class,
                  FactoryHelper.WHITE_LIST_CALLERS,  FactoryHelper.DEFAULT_WHITE_LIST_CALLERS);

    public DocumentBuilderFactoryXercesImpl()
    {
        factoryHelper.configureFactory(this, FEATURES_TO_ENABLE, FEATURES_TO_DISABLE, WHITE_LIST_CALLERS);
    }

    @Override
    public DocumentBuilder newDocumentBuilder() throws ParserConfigurationException {
        DocumentBuilder documentBuilder = super.newDocumentBuilder();
        factoryHelper.debugNewParser(documentBuilder);
        return documentBuilder;
    }
}