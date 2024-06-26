<template>
  <div class="chat">
    <el-button type="success" @click="sendBroadcast">测试广播消息</el-button>
    <h1>Demo</h1>
    <div>当前用户ID：{{ form.fromUid }}</div>
    <el-form ref="form" :model="form" label-width="100px">
      <el-form-item label="目标方ID">
        <el-input v-model="form.toUid"></el-input>
      </el-form-item>
      <el-form-item label="发送消息">
        <el-input v-model="form.message"></el-input>
      </el-form-item>
      <el-button type="primary" @click="sendMsg">发送</el-button>
    </el-form>
    <div>
      <h1>消息记录</h1>
      <template v-if="messageData.length > 0">
        <div v-for="(item,index) in messageData" :key="index">
          <el-tag v-if="!item.fromUid">广播消息{{ item }}</el-tag>
          <el-tag v-else type="success">用户{{ item.fromUid }}向你发来消息：{{ item.message }}</el-tag>
        </div>
      </template>
      <template v-else>
        <el-empty :image-size="200"></el-empty>
      </template>
    </div>
  </div>
</template>

<script>
import axios from 'axios';
import websocket from '@/mixins/websocket';
export default {
  name: "ChatView",
  mixins: [websocket],
  data() {
    return {
      form: {
        fromUid: "",
        toUid: "",
        message: "",
      },
      messageData: []
    };
  },
  created() {
    this.form.fromUid = Math.floor(Math.random() * 10+1);
    //初始化weosocket
    const wsuri = "ws://localhost:9999/websocket/" + this.form.fromUid;
    this.initWebSocket(wsuri);
  },
  destroyed() {
    this.websock.close(); //离开路由之后断开websocket连接
  },
  methods: {
    // 发送广播消息
    sendBroadcast() {
      axios.get("/api/broadcast").catch(() => {
        this.$message.error('广播消息发送失败')
      })
    },
    sendMsg() {
      if (this.form.toUid == "" || this.form.message == "") {
        this.$message.warning("请填写目标方ID和发送内容");
        return;
      }
      //发送数据
      this.websocketsend(JSON.stringify(this.form));
    },
    // 客户端接收服务端数据时触发
    websocketonmessage(e) {
      this.messageData.push(JSON.parse(e.data));
      //收到服务器信息，心跳重置
      this.reset();
    },
  }
};
</script>
