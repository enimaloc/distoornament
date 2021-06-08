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

public class Constant {
    public static final int MAJOR = 0;
    public static final int MINOR = 5;
    public static final int PATCH = 2;
    
    public static final int ALPHA             = 0;
    public static final int BETA              = 0;
    public static final int RELEASE_CANDIDATE = 0;
    
    public static final String VERSION =
            String.format("%s.%s.%s", MAJOR, MINOR, PATCH) +
                    (RELEASE_CANDIDATE != 0 ? "-rc." + RELEASE_CANDIDATE :
                            BETA != 0 ? "-b." + BETA :
                                    ALPHA != 0 ? "-a." + ALPHA : "");
}
