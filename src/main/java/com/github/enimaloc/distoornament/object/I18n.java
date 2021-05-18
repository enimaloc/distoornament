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

package com.github.enimaloc.distoornament.object;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.io.*;
import java.util.*;

public class I18n {
    private static final Map<String, Map<String, I18n>> LOCALES = new HashMap<>();
    
    static {
        File i18n = new File("i18n");
        if (i18n.exists() || i18n.mkdirs()) {
            
            File frLang = new File(i18n, "LANGUAGE-country.lang");
            if (!frLang.exists()) {
                try (FileWriter writer = new FileWriter(frLang)) {
                    writer.append("key=value");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            
            for (File file : Objects.requireNonNull(i18n.listFiles())) {
                if (file.getName().contains("-")) {
                    String[] split = file.getName().toLowerCase(Locale.ROOT)
                                         .substring(0, file.getName().lastIndexOf('.')).split("-");
                    try {
                        new I18n(split[0], split[1], new BufferedReader(new FileReader(file)));
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
    
    private final String              language;
    private final String              country;
    private final Map<String, String> values;
    
    I18n(String language, String country, BufferedReader input) {
        this(language, country, new HashMap<>());
        Flux.fromStream(input.lines())
            .filter(line -> line.contains("="))
            .map(line -> line.split("=", 2))
            .map(split -> Tuples.of(split[0], split[1]))
            .map(tuple -> values.put(tuple.getT1(), tuple.getT2()) != null)
            .subscribe();
    }
    
    @SafeVarargs
    I18n(String language, String country, Tuple2<String, String>... values) {
        this(language, country, new HashMap<>());
        Arrays.stream(values)
              .forEach(tuple -> this.values.put(tuple.getT1(), tuple.getT2()));
    }
    
    I18n(String language, String country, Map<String, String> values) {
        this.language = language;
        this.country = country;
        this.values = values;
        LOCALES.merge(language, Map.of(country, this), (m1, m2) -> {
            m1.putAll(m2);
            return m1;
        });
    }
    
    public static Optional<I18n> getI18n(String locale) {
        return Mono.just(locale)
                .map(s -> s.split("-"))
                .filter(split -> split.length <= 2)
                .map(identifier -> Tuples.of(identifier[0], identifier[1]))
                .map(I18n::getI18n)
                .block();
    }

    public static Optional<I18n> getI18n(Tuple2<String, String> identifier) {
        return getI18n(identifier.getT1(), identifier.getT2());
    }

    public static Optional<I18n> getI18n(String language, String country) {
        return Mono.zip(Mono.just(language).map(String::toLowerCase), Mono.just(country).map(String::toLowerCase))
                .map(identifier -> Tuples.of(identifier.getT1(), identifier.getT2()))
                .map(tuple -> tuple.mapT1(LOCALES::get))
                .map(tuple -> tuple.mapT2(tuple.getT1()::get))
                .map(Tuple2::getT2)
                .blockOptional();
    }

    public static Map<String, String> getAllLanguages() {
        Map<String, String> r = new HashMap<>();
        for (String language : LOCALES.keySet()) {
            for (String country : LOCALES.get(language).keySet()) {
                r.put(language, country);
            }
        }
        return Collections.unmodifiableMap(r);
    }

    @SafeVarargs
    public final String get(String key, Tuple2<String, String>... values) {
        if (!this.values.containsKey(key)) {
            return key;
        }
        String s = this.values.get(key);
        for (Tuple2<String, String> value : values) {
            s = s.replaceAll(":" + value.getT1(), value.getT2());
        }
        return s;
    }
    
    @Override
    public String toString() {
        return language + "-" + country.toUpperCase(Locale.ROOT);
    }
}
