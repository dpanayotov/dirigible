/*
 * Copyright (c) 2022 SAP SE or an SAP affiliate company and Eclipse Dirigible contributors
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-FileCopyrightText: 2022 SAP SE or an SAP affiliate company and Eclipse Dirigible contributors
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.dirigible.engine.odata2.sql;

import org.apache.olingo.odata2.annotation.processor.core.edm.AnnotationEdmProvider;
import org.apache.olingo.odata2.api.ODataService;
import org.apache.olingo.odata2.api.exception.ODataException;
import org.apache.olingo.odata2.api.processor.ODataContext;
import org.eclipse.dirigible.engine.odata2.sql.api.SQLInterceptor;
import org.eclipse.dirigible.engine.odata2.sql.mapping.DefaultEdmTableMappingProvider;
import org.eclipse.dirigible.engine.odata2.sql.processor.DefaultSQLProcessor;
import org.eclipse.dirigible.engine.odata2.sql.test.util.OData2TestUtils;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.eclipse.dirigible.engine.odata2.sql.processor.DefaultSQLProcessor.DEFAULT_DATA_SOURCE_CONTEXT_KEY;

/**
 * OData2TestServiceFactory
 *
 */
public class OData2TestServiceFactory extends org.apache.olingo.odata2.api.ODataServiceFactory {

    private final DataSource ds;
    private final Class<?>[] edmAnnotatedClasses;
    private final List<SQLInterceptor> interceptorList = new ArrayList<>();

    /**
     * @param ds the data source
     * @param edmAnnotatedClasses the classes
     * @throws ODataException in case of error
     */
    public OData2TestServiceFactory(DataSource ds, Class<?>... edmAnnotatedClasses) throws ODataException {
       this(ds, Collections.emptyList(), edmAnnotatedClasses);
    }

    /**
     * @param ds the data source
     * @param interceptorList the interceptor list
     * @param edmAnnotatedClasses the classes
     * @throws ODataException in case of error
     */
    public OData2TestServiceFactory(DataSource ds, List<SQLInterceptor> interceptorList, Class<?>... edmAnnotatedClasses) throws ODataException {
        this.ds = ds;
        this.edmAnnotatedClasses = edmAnnotatedClasses;
        addInterceptors(interceptorList);
    }

    /**
     * @return AnnotationEdmProvider
     * @throws ODataException in case of error
     */
    public AnnotationEdmProvider createAnnotationEdmProvider() throws ODataException {
        return new AnnotationEdmProvider(Collections.unmodifiableList(Arrays.asList(edmAnnotatedClasses)));

    }

    /**
     * @param interceptorList the interceptor list
     */
    public void addInterceptors(List<SQLInterceptor> interceptorList){
        this.interceptorList.addAll(interceptorList);
    }


    @Override
    public ODataService createService(ODataContext ctx) throws ODataException {
        setDefaultDataSource(ctx);
        try {
            DefaultEdmTableMappingProvider edmTableMappingProvider = new DefaultEdmTableMappingProvider(
                    OData2TestUtils.resources(edmAnnotatedClasses));
            DefaultSQLProcessor processor = new DefaultSQLProcessor(edmTableMappingProvider);
            processor.addInterceptors(interceptorList);
            return super.createODataSingleProcessorService(createAnnotationEdmProvider(), processor);
        } catch (org.eclipse.dirigible.engine.odata2.api.ODataException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @param ctx the context
     * @throws ODataException in case of error
     */
    public void setDefaultDataSource(ODataContext ctx) throws ODataException {
        ctx.setParameter(DEFAULT_DATA_SOURCE_CONTEXT_KEY, ds);
    }

}
