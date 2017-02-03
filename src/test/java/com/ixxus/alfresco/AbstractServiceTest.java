/**
 * All rights reserved.
 * Copyright (c) Ixxus Ltd 2017
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