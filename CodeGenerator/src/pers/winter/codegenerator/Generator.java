/*
 * Copyright 2023 Winter Game Server
 *
 * The Winter Game Server licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package pers.winter.codegenerator;

import pers.winter.codegenerator.utils.FileUtil;

import java.io.File;
import java.util.List;

public class Generator {
    public static void main(String[] args) throws InterruptedException {
        File file = new File(Constants.EXPORT_DIR);
        FileUtil.deleteFileRecursively(file);
        file.mkdir();
        List<File> allXml = FileUtil.findAllFilesRecursively(Constants.CONFIG_DIR,"xml");
        Parser.INSTANCE.parse(allXml);
    }
}
