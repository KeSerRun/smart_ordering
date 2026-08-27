const { env } = require('../config/env');

let socketTask = null;
const listeners = new Set();

function connectSocket() {
  if (!env.enableSocket) return null;
  if (socketTask) return socketTask;
  socketTask = wx.connectSocket({ url: env.wsUrl });

  socketTask.onMessage((res) => {
    let payload = res.data;
    try {
      if (typeof payload === 'string') {
        payload = JSON.parse(payload);
      }
    } catch (e) {
      return;
    }
    listeners.forEach((cb) => cb(payload));
  });

  socketTask.onClose(() => {
    socketTask = null;
  });

  socketTask.onError(() => {
    socketTask = null;
  });

  return socketTask;
}

function addSocketListener(fn) {
  listeners.add(fn);
  return () => listeners.delete(fn);
}

module.exports = {
  connectSocket,
  addSocketListener
};
