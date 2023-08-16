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

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import pers.winter.codegenerator.bean.BeanParser;
import pers.winter.codegenerator.entity.EntityParser;
import pers.winter.codegenerator.message.MessageParser;
import pers.winter.codegenerator.utils.FailureInfo;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class Parser {
    private static final String ELEMENT_BEAN = "bean";
    private static final String ELEMENT_MESSAGE = "message";
    private static final String ELEMENT_ENTITY = "entity";

    private final ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    private final DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newDefaultInstance();
    private final Queue<File> failedFiles = new LinkedBlockingQueue<>();
    private final AtomicInteger elementCount = new AtomicInteger(0);
    private final Queue<FailureInfo> failedElements = new LinkedBlockingQueue<>();
    public static Parser INSTANCE = new Parser();
    private Parser(){}
    public void parse(List<File> xmlFiles) throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(xmlFiles.size());
        for(File xmlFile:xmlFiles){
            executor.execute(()->{
                try {
                    parse(xmlFile);
                } catch (Exception e) {
                    failedFiles.add(xmlFile);
                    e.printStackTrace();
                } finally {
                    countDownLatch.countDown();
                }
            });
        }
        countDownLatch.await();
        System.out.println("Generate over!");
        System.out.printf("%d files to generate, %d failed.\n",xmlFiles.size(),failedFiles.size());
        if(failedFiles.size()>0){
            System.out.println("Failed files:");
            failedFiles.forEach(file -> System.out.println(file.getPath()));
        }
        System.out.printf("%d elements to generate, %d failed.\n",elementCount.get(),failedElements.size());
        if(failedElements.size()>0){
            System.out.println("Failed elements:");
            failedElements.forEach(failureInfo -> System.out.println(failureInfo.getTarget()));
        }
        executor.shutdown();
    }

    private void parse(File xml) throws Exception {
        DocumentBuilder builder = builderFactory.newDocumentBuilder();
        Document document = builder.parse(xml);
        Element root = document.getDocumentElement();
        NodeList nodes = root.getChildNodes();
        for(int i = 0;i < nodes.getLength();i++){
            Node node = nodes.item(i);
            if(node.getNodeType() == Node.ELEMENT_NODE){
                elementCount.incrementAndGet();
                Element element = (Element) node;
                String nodeName = node.getNodeName();
                try{
                    if(nodeName.equalsIgnoreCase(ELEMENT_BEAN)){
                        BeanParser.INSTANCE.parse(element);
                    } else if(nodeName.equalsIgnoreCase(ELEMENT_MESSAGE)){
                        MessageParser.INSTANCE.parse(element);
                    } else if(nodeName.equalsIgnoreCase(ELEMENT_ENTITY)){
                        EntityParser.INSTANCE.parse(element);
                    }
                } catch (Exception e){
                    failedElements.add(new FailureInfo(element.getAttribute("class"),e));
                    e.printStackTrace();
                }
            }
        }
    }
}
