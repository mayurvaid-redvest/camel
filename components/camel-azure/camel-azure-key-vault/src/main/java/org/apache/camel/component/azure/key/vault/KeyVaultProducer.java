/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.camel.component.azure.key.vault;

import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.SecretClientBuilder;
import com.azure.security.keyvault.secrets.models.KeyVaultSecret;
import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.support.DefaultProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KeyVaultProducer extends DefaultProducer {

    private static final Logger LOG = LoggerFactory.getLogger(KeyVaultProducer.class);

    protected SecretClient secretClient;

    public KeyVaultProducer(final Endpoint endpoint) {
        super(endpoint);
    }

    @Override
    protected void doInit() throws Exception {
        super.doInit();
    }

    @Override
    protected void doStart() throws Exception {
        super.doStart();

        // Build key vault URI
        String keyVaultUri = "https://" + getConfiguration().getVaultName() + ".vault.azure.net";

        // Credential
        ClientSecretCredential credential = new ClientSecretCredentialBuilder()
                .tenantId(getConfiguration().getTenantId())
                .clientId(getConfiguration().getClientId())
                .clientSecret(getConfiguration().getClientSecret())
                .build();

        // Build Client
        secretClient = new SecretClientBuilder()
                .vaultUrl(keyVaultUri)
                .credential(credential)
                .buildClient();
    }

    @Override
    public void process(Exchange exchange) throws Exception {
        KeyVaultOperation operation = determineOperation(exchange);
        switch (operation) {
            case createSecret:
                createSecret(exchange);
                break;
            default:
                throw new IllegalArgumentException("Unsupported operation");
        }
    }

    private void createSecret(Exchange exchange) {
        KeyVaultSecret p = secretClient.setSecret(new KeyVaultSecret("pippo", "peppe"));
        Message message = getMessageForResponse(exchange);
        message.setBody(p);
    }

    @Override
    public KeyVaultEndpoint getEndpoint() {
        return (KeyVaultEndpoint) super.getEndpoint();
    }

    public KeyVaultConfiguration getConfiguration() {
        return getEndpoint().getConfiguration();
    }

    public static Message getMessageForResponse(final Exchange exchange) {
        return exchange.getMessage();
    }

    private KeyVaultOperation determineOperation(Exchange exchange) {
        KeyVaultOperation operation = exchange.getIn().getHeader(KeyVaultConstants.OPERATION, KeyVaultOperation.class);
        if (operation == null) {
            operation = getConfiguration().getOperation();
        }
        return operation;
    }
}