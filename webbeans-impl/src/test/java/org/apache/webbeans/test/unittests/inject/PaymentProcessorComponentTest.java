/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.webbeans.test.unittests.inject;

import junit.framework.Assert;

import org.apache.webbeans.test.AbstractUnitTest;
import org.apache.webbeans.test.component.CheckWithCheckPayment;
import org.apache.webbeans.test.component.CheckWithMoneyPayment;
import org.apache.webbeans.test.component.IPayment;
import org.apache.webbeans.test.component.PaymentProcessorComponent;
import org.junit.Test;

public class PaymentProcessorComponentTest extends AbstractUnitTest
{
    @Test
    public void testTypedComponent() throws Throwable
    {
        startContainer(CheckWithCheckPayment.class, CheckWithMoneyPayment.class, PaymentProcessorComponent.class);
        PaymentProcessorComponent uc = getInstance(PaymentProcessorComponent.class);
        IPayment p = uc.getPaymentCheck();

        Assert.assertTrue(p instanceof CheckWithCheckPayment);
        Assert.assertEquals("CHECK", p.pay());

        p = uc.getPaymentMoney();

        Assert.assertTrue(p instanceof CheckWithMoneyPayment);

        Assert.assertEquals("MONEY", p.pay());
    }

}
