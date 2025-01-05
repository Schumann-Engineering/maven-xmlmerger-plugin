/*
 * Copyright © 2011  The original author or authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.commons.io.FileUtils

File file = new File(basedir, "target/classes/pom.xml");
File fileMerge = new File(basedir, "target/classes/MERGE.pom.xml");

String pomContent = FileUtils.readFileToString(file);

if (!pomContent.
        contains("<groupId>junit</groupId>") || !pomContent.contains("<artifactId>xdt4j</artifactId>"))
    throw new RuntimeException("The generated project do not containe all data needed");

if (fileMerge.exists())
    throw new RuntimeException("The MERGE.pom.xml must be deleted");
