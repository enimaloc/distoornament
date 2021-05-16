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

package com.github.enimaloc.commands;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.InteractionCreateEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.discordjson.json.ApplicationCommandRequest;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuple4;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public interface Command {
    String getName();
    
    default String[] getAliases() {
        return new String[0];
    }
    
    default String getDescription() {
        return "";
    }
    
    Mono<Void> executeText(TextContext context);
    
    Mono<Void> executeInteraction(InteractionContext context);
    
    default boolean canBeRegisterAsApplicationCommand() {
        return true;
    }
    
    default ApplicationCommandRequest asRequest() {
        return ApplicationCommandRequest.builder()
                                        .name(getName())
                                        .addAllOptions(getOptions())
                                        .description(getDescription())
                                        .build();
    }
    
    default List<ApplicationCommandOptionData> getOptions() {
        return Collections.emptyList();
    }
    
    interface Context {
        Command getCommand();
    
        String getCommandName();
    
        Mono<Guild> getGuild();
    
        Optional<User> getAuthor();
    
        Optional<Member> getMember();
    
        GatewayDiscordClient getClient();
    }
    
    class TextContext implements Context {
        private final MessageCreateEvent event;
        private final String             commandName;
        private final String             arguments;
        private final Command            command;
        
        public TextContext(Tuple4<MessageCreateEvent, String, String, Command> tuple) {
            event = tuple.getT1();
            commandName = tuple.getT2();
            arguments = tuple.getT3();
            command = tuple.getT4();
        }
        
        @Override
        public Command getCommand() {
            return command;
        }
        
        @Override
        public String getCommandName() {
            return commandName;
        }
        
        @Override
        public Mono<Guild> getGuild() {
            return event.getGuild();
        }
    
        @Override
        public Optional<User> getAuthor() {
            return event.getMessage().getAuthor();
        }
    
        @Override
        public Optional<Member> getMember() {
            return event.getMember();
        }
    
        @Override
        public GatewayDiscordClient getClient() {
            return event.getClient();
        }
    
        public String getArguments() {
            return arguments;
        }
    
        public Message getMessage() {
            return event.getMessage();
        }
    }
    
    class InteractionContext implements Context {
        private final InteractionCreateEvent event;
        private final Command                command;
        
        public InteractionContext(Tuple2<InteractionCreateEvent, Command> tuple) {
            event = tuple.getT1();
            command = tuple.getT2();
        }
        
        @Override
        public Command getCommand() {
            return command;
        }
        
        @Override
        public String getCommandName() {
            return event.getCommandName();
        }
        
        @Override
        public Mono<Guild> getGuild() {
            return event.getInteraction().getGuild();
        }
    
        @Override
        public Optional<User> getAuthor() {
            return Optional.of(event.getInteraction().getUser());
        }
    
        @Override
        public Optional<Member> getMember() {
            return event.getInteraction().getMember();
        }
    
        @Override
        public GatewayDiscordClient getClient() {
            return event.getClient();
        }
    
        public InteractionCreateEvent getEvent() {
            return event;
        }
    }
}
