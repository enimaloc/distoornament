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

import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.rest.util.ApplicationCommandOptionType;
import reactor.util.function.Tuple2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public interface CommandGroup extends Command {
    
    Tuple2<ApplicationCommandOptionType, Command>[] getCommands();
    
    @Override
    default List<ApplicationCommandOptionData> getOptions() {
        final List<ApplicationCommandOptionData> options = new ArrayList<>();
        for (Tuple2<ApplicationCommandOptionType, Command> tuple : this.getCommands()) {
            options.add(ApplicationCommandOptionData.builder()
                                                    .name(tuple.getT2().getName())
                                                    .description(tuple.getT2().getDescription())
                                                    .type(tuple.getT1().getValue())
                                                    .options(tuple.getT2().getOptions())
                                                    .build());
        }
        return Collections.unmodifiableList(options);
    }
}
