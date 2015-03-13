
interface ChatMessageArguments {
  user: string;
  time: string;
  message: string;
}

class Templates {
  static ChatMessage(args: ChatMessageArguments): string {
    return '<div class="user">' + args.user  + '</div>' +
    '<div class="timestamp">' + args.time + '</div>' +
    '<div class="message">' +  args.message + '</div>';
  }

  static ChatBox() {
    return '<div class="chat-header">' +
      'Can I help you bro?' +
      '</div>' +
      '<div class="messages-container"></div>' +
      '<div class="new-message-container">' +
      '<textarea id="new-message-input" cols="30" rows="10"></textarea>' +
      '<button id="send-message" type="button">send</button>' +
      '</div>'
  }
}