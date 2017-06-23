/**
 * Copyright 2017 Ixxus Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
 */
package com.ixxus.alfresco;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.transaction.TransactionService;
import org.junit.After;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;

import javax.transaction.UserTransaction;

/**
 *
 * @author: Bert Bloomer
 */
public abstract class AbstractServiceTest {

    @Autowired
    protected TransactionService trxService;
    private UserTransaction trx;

    @Before
    public void setup() throws Exception {
        AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();
        trx = trxService.getUserTransaction(false);
        trx.begin();
    }

    @After
    public void breakdown() throws Exception {
        try {
            trx.commit();
        } catch (Exception e) {
        }
    }
}
