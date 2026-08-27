const { restoreSession, isLoggedIn } = require('./utils/auth');
const { connectSocket } = require('./utils/socket');

App({
  globalData: {
    user: null,
    table: null,
    currentOrderId: null
  },

  onLaunch() {
    restoreSession();
    // 已登录时自动连接 WebSocket
    if (isLoggedIn()) {
      const socketTask = connectSocket();
      if (socketTask && typeof socketTask.onError === 'function') {
        // 小程序 SocketTask 不是 Promise，这里只做容错监听，避免空对象调用 catch
        socketTask.onError(() => {});
      }
    }
  }
});
