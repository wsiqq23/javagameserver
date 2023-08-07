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
package pers.winter.test.http;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.Executors;

public class HttpRequestUtil {
    private static final Logger logger = LogManager.getLogger(HttpRequestUtil.class);
    private static final Duration CONNECT_TIME_OUT = Duration.ofSeconds(5);
    private static final Duration READ_TIME_OUT = Duration.ofSeconds(5);
    private static final HttpClient client;
    static {
        client = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).connectTimeout(CONNECT_TIME_OUT).executor(Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())).build();
    }
    public static byte[] get(String url){
        try{
            HttpRequest request = HttpRequest.newBuilder().version(HttpClient.Version.HTTP_1_1).uri(URI.create(url)).GET().timeout(READ_TIME_OUT).build();
            HttpResponse<byte[]> response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
            if(response.statusCode() == 200){
                return response.body();
            } else {
                logger.error("Http response code:{}, url:{}",response.statusCode(),url);
            }
        } catch (Exception e) {
            logger.error("Exception.",e);
        }
        return null;
    }
    public static byte[] post(String url,byte[] postData){
        try {
            HttpRequest request = HttpRequest.newBuilder().version(HttpClient.Version.HTTP_2).uri(URI.create(url)).POST(HttpRequest.BodyPublishers.ofByteArray(postData)).timeout(READ_TIME_OUT).build();
            HttpResponse<byte[]> response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
            if(response.statusCode() == 200){
                return response.body();
            } else {
                logger.error("Http response code:{}, url:{}",response.statusCode(),url);
            }
        } catch (Exception e) {
            logger.error("Exception.",e);
        }
        return null;
    }
}
