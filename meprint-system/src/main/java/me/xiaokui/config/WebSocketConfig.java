/*
 *  Copyright 2019-2020 Zheng Jie
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package me.xiaokui.config;

import me.xiaokui.modules.mapper.TestCaseMapper;
import me.xiaokui.modules.system.handler.Room;
import me.xiaokui.modules.system.service.RecordService;
import org.apache.catalina.Context;
import org.apache.catalina.connector.Connector;
import org.apache.tomcat.websocket.server.WsSci;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.embedded.tomcat.TomcatContextCustomizer;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.server.ServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

/**
 * @author ZhangHouYing
 * @date 2019-08-24 15:44
 */
@Configuration
public class WebSocketConfig {

	/**
	 * http的端口
	 */
	@Value("${server.port}")
	private Integer port;

	/**
	 * https的端口
	 */
	@Value("${http.port}")
	private Integer httpsPort;

	@Bean
	public ServerEndpointExporter serverEndpointExporter() {
		return new ServerEndpointExporter();
	}

	/**
	 * 创建wss协议接口
	 */
	@Bean
	public TomcatContextCustomizer tomcatContextCustomizer() {
		return context -> context.addServletContainerInitializer(new WsSci(), null);
	}

	/**
	 * 给{@code WebSocket}的注入依赖
	 * 你可能会发现WebSocket已经有了{@code Component}，为什么不使用{@code Resource}或者{@code Autowired}
	 * 原因如下：
	 * 因为@EndPointServer注解劫持了WebSocket的实例，这里就把Bean的管理权交给了tomcat，tomcat利用反射给每个线程生成每一个websocket实例
	 * 通过这样的方式进行线程隔离，所以{@code WebSocket}下所有的this.xxx看起来应该是会有线程问题，其实本质上并不会因为多个请求而互相干扰
	 * 所以两个依赖加上了static，然后通过这样的方式注入，表示两个依赖跟着{@code WebSocket}这个.class类型进入了方法区，而不是跟着实例进堆
	 *
	 * 感兴趣可以在websocket类中的方法打断点，进来一个请求去追寻方法栈
	 * @see org.apache.tomcat.websocket.pojo.PojoEndpointServer#onOpen(javax.websocket.Session, javax.websocket.EndpointConfig)
	 * 这个函数当中的
	 * pojo = sec.getConfigurator().getEndpointInstance(sec.getEndpointClass());
	 * 会生成一个默认初始化处理器org.apache.tomcat.websocket.server.DefaultServerEndpointConfigurator
	 * 然后在函数public <T> T getEndpointInstance(Class<T> clazz)下调用return clazz.getConstructor().newInstance();
	 * 完成基于反射的实例构造
	 */
	@Autowired
	public void setWebsocketService(RecordService recordService, TestCaseMapper caseMapper) {
//        WebSocket.recordService = recordService;
//        WebSocket.caseMapper = caseMapper;
		Room.caseMapper = caseMapper;
		Room.recordService = recordService;
	}
}
