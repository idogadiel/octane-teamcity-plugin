package com.hp.nga.teamcity.coverage;

import com.hp.octane.plugins.jetbrains.teamcity.configuration.ConfigurationService;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created by lazara on 21/02/2016.
 */
public class HttpClientTest {

    @Test
    public void TestConnectionWithNGAServer(){

        String serverLocation="https://mqalb006nngx.saas.hpe.com";
        String username="MQM_QA2@hpe.com";
        String password="Welcome1";
        String sharedSpace="2002";

        String status = ConfigurationService.checkConfiguration(serverLocation, sharedSpace,
                username, password, ConfigurationService.CLIENT_TYPE);

        assertNotNull(status);
        assertEquals("Connection successful", status);
    }
}