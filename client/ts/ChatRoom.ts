///<reference path='definitions/lib.d.ts' />

module SlackLine {

  export interface ChatEvent {
    content: string;
    username?: string;
    timestamp?: number;
  }

  export interface WelcomeEvent {
    userId: string;
    userAgent: string;
    currentPage: string;
    timezone: string;
    language: string;
  }

  export class ChatRoom {
    private socket: SocketIOClient.Socket;
    private connectionOpen: boolean = false;

    constructor(private chatView: ChatView, private username) {
      this.chatView.doInitializeChat = () => this.connect();
      this.chatView.doSubmitMessage = (event: ChatEvent) => this.sendChatMessage(event);
    }

    connect() {
      if (!this.connectionOpen) {
        this.socket = io.connect('192.168.68.62:8080', {
          'reconnection delay' : 2000,
          'force new connection' : true
        });

        this.socket.on('connect', () => this.onConnect());
        this.socket.on('chat', (chatEvent: ChatEvent) => this.onChatMessage(chatEvent));
        this.connectionOpen = true;
      }
    }

    onConnect() {
      this.sendWelcomeMessage({
        userId: this.username,
        userAgent: navigator.userAgent,
        currentPage: 'some-page',
        timezone: -(new Date().getTimezoneOffset()/60)+"",
        language: navigator.language
      });
    }

    onChatMessage(chatEvent: ChatEvent) {
      this.chatView.addMessage(chatEvent);
    }

    sendChatMessage(chatEvent: ChatEvent) {
      this.socket.emit('chat', chatEvent);
    }

    sendWelcomeMessage(welcomeEvent: WelcomeEvent) {
      this.socket.emit('welcome', welcomeEvent);
    }
  }
}
