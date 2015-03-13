///<reference path='definitions/lib.d.ts' />

module SlackLine {
  export class ChatRoom {
    private socket: SocketIOClient.Socket;

    constructor(private chatView: ChatView) {
      this.socket = io.connect('192.168.68.62:8080', {
        'reconnection delay' : 2000,
        'force new connection' : true
      });

      this.socket.on('connect', () => this.onConnect());

      this.socket.on('chat', (chatEvent: ChatEvent) => this.onChatMessage(chatEvent));
    }

    onConnect() {
      this.sendChatMessage({
        content: 'new user connected needing help asap'
      });
    }

    onChatMessage(chatEvent: ChatEvent) {
      this.chatView.addMessage(chatEvent.content);
    }

    sendChatMessage(chatEvent: ChatEvent) {
      this.socket.emit('chat', chatEvent);
    }
  }
}
