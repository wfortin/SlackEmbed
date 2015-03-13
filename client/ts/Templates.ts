///<reference path='definitions/lib.d.ts' />

module SlackLine {
  export class Templates {
    static ChatMessage(args: ChatEvent): string {
      return '<div class="user">' + args.username  + '</div>' +
        '<div class="timestamp">' + this.formatTime(args.timestamp) + '</div>' +
        '<div class="message-text">' +  args.content + '</div>';
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

    private static formatTime(timestamp: any):string {
      timestamp = parseFloat(timestamp);
      var date = new Date(timestamp*1000);
      var hours: any = date.getHours();
      hours = hours < 10 ? '0' + hours : hours;
      var minutes: any = date.getMinutes();
      minutes = minutes < 10 ? '0' + minutes : minutes;
      return hours + ':' + minutes;
    }
  }
}
