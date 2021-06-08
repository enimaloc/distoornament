/*
 * Copyright 2021 Antoine(enimaloc) SAINTY
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.enimaloc.distoornament;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import com.github.enimaloc.commands.CommandHandler;
import discord4j.core.DiscordClient;
import io.sentry.Sentry;
import org.apache.commons.lang3.SystemUtils;
import org.slf4j.LoggerFactory;

import java.util.Locale;

public class Distoornament {
    
    private final Logger logger = (Logger) LoggerFactory.getLogger(Distoornament.class);
    
    public Distoornament() {
        ((Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME))
                .setLevel(Level.toLevel(
                        System.getenv("logLevel") != null ? System.getenv("logLevel").toUpperCase(Locale.ROOT) : null,
                        Level.INFO
                ));
        
        logger.info("Starting Distoornament (v. {})", Constant.VERSION);
        
        Sentry.init(options -> {
            options.setDsn(System.getenv("sentry"));
            options.setDist(SystemUtils.OS_ARCH);
            options.setEnvironment(System.getenv("dev") != null ? "Development" : "Production");
        });
        
        DiscordClient client = DiscordClient.create(System.getenv("token"));
        
        client.withGateway(gateway -> {
            new CommandHandler(gateway, "!"
            );
            
            return gateway.onDisconnect();
        }).block();
    }
}
