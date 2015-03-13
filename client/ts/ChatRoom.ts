///<reference path='definitions/lib.d.ts' />

module SlackLine {

  export interface ChatEvent {
    content: string;
    username?: string;
    timestamp?: number;
  }

  export interface WelcomeEvent {
    userId: string;
  }

  export class ChatRoom {
    private socket: SocketIOClient.Socket;

    constructor(private chatView: ChatView) {
      this.socket = io.connect('192.168.68.62:8080', {
        'reconnection delay' : 2000,
        'force new connection' : true
      });

      this.socket.on('connect', () => this.onConnect());

      this.socket.on('chat', (chatEvent: ChatEvent) => this.onChatMessage(chatEvent));

      this.chatView.doSubmitMessage = (event: ChatEvent) => this.sendChatMessage(event);
    }

    onConnect() {
      this.sendWelcomeMessage({
        userId: 'wfortin1234'
      });
    }

    onChatMessage(chatEvent: ChatEvent) {
      this.chatView.addMessage(chatEvent);
    }

    sendChatMessage(chatEvent: ChatEvent) {
      this.socket.emit('chat', chatEvent);
    }

    sendWelcomeMessage(welcomeEvent: WelcomeEvent) {
      this.socket.emit('welcome', {
        userId: welcomeEvent.userId
      });
    }
  }
}
