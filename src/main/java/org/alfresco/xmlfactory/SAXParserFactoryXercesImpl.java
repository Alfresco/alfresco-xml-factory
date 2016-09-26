/*
 * Copyright (C) 2005-2014 Alfresco Software Limited.
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
import java.util.List;

import org.apache.xerces.jaxp.SAXParserFactoryImpl;

public class SAXParserFactoryXercesImpl extends SAXParserFactoryImpl
{
    private static List<String> featuresToEnable = FactoryHelper.DEFAULT_FEATURES_TO_ENABLE;
    private static List<String> featuresToDisable = FactoryHelper.DEFAULT_FEATURES_TO_DISABLE;
    
    public SAXParserFactoryXercesImpl()
    {
        super();
        FactoryHelper.configureFactory(this, featuresToEnable, featuresToDisable);
    }
    
    public void setFeaturesToEnable(List<String> featuresToEnable)
    {
        SAXParserFactoryXercesImpl.featuresToEnable = new ArrayList<String>(featuresToEnable);
    }
    
    public void setFeaturesToDisable(List<String> featuresToDisable)
    {
        SAXParserFactoryXercesImpl.featuresToDisable = new ArrayList<String>(featuresToDisable);
    }
}
