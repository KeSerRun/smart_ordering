import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client'

/**
 * 创建 STOMP WebSocket 客户端
 *
 * <p>传输层用 SockJS（后端 /api/ws 端点，代理带 ws:true），连接后订阅
 * 后厨大屏主题 {@code /topic/kitchen}，把服务端推送的 WsMessage
 * （{ eventType, data, topic, timestamp }）解析后交给 onMessage。</p>
 *
 * @param {object}   [options]
 * @param {Function} [options.onMessage]  收到 /topic/kitchen 消息的回调，参数为解析后的 body
 * @param {Function} [options.onConnected] 连接建立后的回调，参数为 client 实例
 * @returns {Client} @stomp/stompjs 客户端，调用方负责 activate / deactivate
 */
export function createStompClient({ onMessage, onConnected } = {}) {
  const client = new Client({
    // 生产用同源相对路径 /api/ws（经反代或直接同域）；开发由 Vite 代理并升级
    webSocketFactory: () => new SockJS('/api/ws'),
    reconnectDelay: 5000,
    heartbeatIncoming: 10000,
    heartbeatOutgoing: 10000,
    logRawCommunication: false,
    debug: import.meta.env.DEV ? (str) => console.debug('[ws]', str) : () => {}
  })

  client.onConnect = () => {
    console.info('[ws] connected, subscribing /topic/kitchen')
    client.subscribe('/topic/kitchen', (frame) => {
      try {
        const body = JSON.parse(frame.body)
        onMessage && onMessage(body)
      } catch (e) {
        console.error('[ws] invalid message payload', frame.body, e)
      }
    })
    onConnected && onConnected(client)
  }

  client.onStompError = (frame) => {
    console.error('[ws] STOMP error:', frame.headers && frame.headers.message, frame.body)
  }

  client.onWebSocketClose = () => {
    console.info('[ws] socket closed, will auto-reconnect')
  }

  return client
}