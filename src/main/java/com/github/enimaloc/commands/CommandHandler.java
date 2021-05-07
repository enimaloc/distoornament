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
import discord4j.core.object.entity.Message;
import reactor.core.publisher.Flux;
import reactor.util.function.Tuples;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommandHandler {
    
    private final String[]             prefix;
    private final Map<String, Command> commands;
    
    public CommandHandler(GatewayDiscordClient gateway, String prefix, Command... commands) {
        this(gateway, new String[]{prefix}, commands);
    }
    
    public CommandHandler(GatewayDiscordClient gateway, String[] prefix, Command... commands) {
        this.prefix = prefix;
        this.commands = new HashMap<>();
        for (Command command : commands) {
            this.commands.put(command.getName(), command);
            for (String alias : command.getAliases()) {
                this.commands.put(alias, command);
            }
        }
        Flux.fromStream(this.commands.values().stream())
            .filter(Command::canBeRegisterAsApplicationCommand)
            .map(Command::asRequest)
            .collectList()
            .flatMapMany(request ->
                    gateway.getRestClient()
                           .getApplicationService()
                           .bulkOverwriteGlobalApplicationCommand(
                                   gateway.getSelfId().asLong(),
                                   request
                           )
            ).subscribe();
        
        gateway.on(MessageCreateEvent.class)
               .map(event -> Tuples.of(event, event.getMessage()))
               .map(tuple -> tuple.mapT2(Message::getContent))
               .filter(tuple -> getRegexPrefix().matcher(tuple.getT2()).find())
               .map(tuple -> tuple.mapT2(content -> getRegexPrefix().matcher(content).replaceFirst("")))
               .map(tuple -> tuple.mapT2(content -> content.split(" ", 2)))
               .map(tuple -> Tuples
                       .of(tuple.getT1(), tuple.getT2()[0], tuple.getT2().length != 1 ? tuple.getT2()[1] : ""))
               .filter(tuple -> this.commands.containsKey(tuple.getT2()))
               .map(tuple -> Tuples.of(tuple.getT1(), tuple.getT2(), tuple.getT3(), this.commands.get(tuple.getT2())))
               .map(tuple -> Tuples.of(new Command.TextContext(tuple), tuple.getT4()))
               .flatMap(tuple -> tuple.getT2().executeText(tuple.getT1()))
               .subscribe();
        
        gateway.on(InteractionCreateEvent.class)
               .map(event -> Tuples.of(event, event.getCommandName()))
               .filter(tuple -> this.commands.containsKey(tuple.getT2()))
               .map(tuple -> tuple.mapT2(this.commands::get))
               .map(tuple -> tuple.mapT1(event -> new Command.InteractionContext(tuple)))
               .flatMap(tuple -> tuple.getT2().executeInteraction(tuple.getT1()))
               .subscribe();
    }
    
    private Pattern getRegexPrefix(String... additionalPrefix) {
        StringBuilder regexBuilder = new StringBuilder();
        for (String s : prefix) {
            regexBuilder.append("|").append(Matcher.quoteReplacement(s));
        }
        for (String s : additionalPrefix) {
            regexBuilder.append("|").append(Matcher.quoteReplacement(s));
        }
        return Pattern.compile("^(" + regexBuilder.subSequence(1, regexBuilder.length()) + ")");
    }
}
