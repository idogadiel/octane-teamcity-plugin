/*
 *     Copyright 2017 Hewlett-Packard Development Company, L.P.
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 */

package com.hp.octane.plugins.jetbrains.teamcity.actions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hp.octane.integrations.OctaneSDK;
import com.hp.octane.integrations.dto.DTOFactory;
import com.hp.octane.integrations.dto.configuration.OctaneConfiguration;
import com.hp.octane.plugins.jetbrains.teamcity.configuration.OctaneConfigStructure;
import com.hp.octane.plugins.jetbrains.teamcity.OctaneTeamCityPlugin;
import com.hp.octane.plugins.jetbrains.teamcity.configuration.TCConfigurationService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Created by lazara on 14/02/2016.
 */

public class ConfigurationActionsController implements Controller {
	private static final Logger logger = LogManager.getLogger(ConfigurationActionsController.class);
	private static final DTOFactory dtoFactory = DTOFactory.getInstance();

	@Autowired
	private OctaneTeamCityPlugin octaneTeamCityPlugin;
	@Autowired
	private TCConfigurationService configurationService;

	@Override
	public ModelAndView handleRequest(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
		String returnStr = "";
		String action = httpServletRequest.getParameter("action");

		if (action == null || action.equals("")) {
			returnStr = reloadConfiguration();
		} else {
			try {
				String url = httpServletRequest.getParameter("server");
				String apiKey = httpServletRequest.getParameter("username1");
				String secret = httpServletRequest.getParameter("password1");
				OctaneConfiguration octaneConfiguration;

				if (action.equals("test")) {
					octaneConfiguration = OctaneSDK.getInstance().getConfigurationService().buildConfiguration(url, apiKey, secret);
					returnStr = configurationService.checkConfiguration(octaneConfiguration);
				} else if (action.equals("save")) {
					if (url != null && !url.isEmpty()) {
						octaneConfiguration = OctaneSDK.getInstance().getConfigurationService().buildConfiguration(url, apiKey, secret);
					} else {
						octaneConfiguration = dtoFactory.newDTO(OctaneConfiguration.class)
								.setUrl("")
								.setSharedSpace("")
								.setApiKey(apiKey)
								.setSecret(secret);
					}
					returnStr = updateConfiguration(octaneConfiguration, url);
				}
			} catch (Exception e) {
				logger.error("failed to process configuration request (" + action + ")", e);
				returnStr = e.getMessage();
			}
		}

		PrintWriter writer;
		try {
			writer = httpServletResponse.getWriter();
			writer.write(returnStr);
		} catch (IOException ioe) {
			logger.error("failed to write response", ioe);
		}
		return null;
	}

	public String updateConfiguration(OctaneConfiguration octaneConfiguration, String originalUrl) {
		OctaneConfigStructure cfg = octaneTeamCityPlugin.getConfig();
		cfg.setUiLocation(originalUrl);
		cfg.setLocation(octaneConfiguration.getUrl());
		cfg.setSharedSpace(octaneConfiguration.getSharedSpace());
		cfg.setUsername(octaneConfiguration.getApiKey());
		cfg.setSecretPassword(octaneConfiguration.getSecret());
		configurationService.saveConfig(cfg);
		OctaneSDK.getInstance().getConfigurationService().notifyChange();
		return "Configuration updated successfully";
	}

	public String reloadConfiguration() {
		try {
			ObjectMapper mapper = new ObjectMapper();
			OctaneConfigStructure cfg = octaneTeamCityPlugin.getConfig();
			return mapper.writeValueAsString(cfg);
		} catch (JsonProcessingException jpe) {
			logger.error("failed to reload configuration", jpe);
		}
		return null;
	}
}
