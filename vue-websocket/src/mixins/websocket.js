export default {
  data() {
    return {
      websock: null, //websocket对象
      lockReconnect: false, //是否真正建立连接
      timeout: 58 * 1000, //58秒一次心跳
      timeoutObj: null, //心跳心跳倒计时
      serverTimeoutObj: null, //心跳倒计时
      timeoutnum: null, //断开 重连倒计时
    }
  },
  methods: {
    initWebSocket(wsuri) {
      this.websock = new WebSocket(wsuri);
      // 客户端接收服务端数据时触发
      this.websock.onmessage = this.websocketonmessage;
      // 连接建立时触发
      this.websock.onopen = this.websocketonopen;
      // 通信发生错误时触发
      this.websock.onerror = this.websocketonerror;
      // 连接关闭时触发
      this.websock.onclose = this.websocketclose;
    },
    // 连接建立时触发
    websocketonopen() {
      //开启心跳
      this.start();
    },
    // 通信发生错误时触发
    websocketonerror() {
      console.log("出现错误");
      this.reconnect();
    },
    websocketsend(Data) {
      //数据发送
      this.websock.send(Data);
    },
    // 连接关闭时触发
    websocketclose(e) {
      //关闭
      console.log("断开连接", e);
      //重连
      this.reconnect();
    },
    reconnect() {
      //重新连接
      var that = this;
      if (that.lockReconnect) {
        return;
      }
      that.lockReconnect = true;
      //没连接上会一直重连，设置延迟避免请求过多
      that.timeoutnum && clearTimeout(that.timeoutnum);
      that.timeoutnum = setTimeout(function () {
        //新连接
        that.initWebSocket();
        that.lockReconnect = false;
      }, 5000);
    },
    reset() {
      //重置心跳
      var that = this;
      //清除时间
      clearTimeout(that.timeoutObj);
      clearTimeout(that.serverTimeoutObj);
      //重启心跳
      that.start();
    },
    start() {
      //开启心跳
      console.log("开启心跳");
      var self = this;
      self.timeoutObj && clearTimeout(self.timeoutObj);
      self.serverTimeoutObj && clearTimeout(self.serverTimeoutObj);
      self.timeoutObj = setTimeout(function () {
        //这里发送一个心跳，后端收到后，返回一个心跳消息，
        if (self?.ws?.readyState == 1) {
          //如果连接正常
          // self.ws.send("heartCheck"); //这里可以自己跟后端约定
        } else {
          //否则重连
          self.reconnect();
        }
        self.serverTimeoutObj = setTimeout(function () {
          //超时关闭
          self?.ws?.close();
        }, self.timeout);
      }, self.timeout);
    },
  },
}