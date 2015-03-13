///<reference path='definitions/lib.d.ts' />

module SlackLine {
  export class SlackLineChat {
    private chatView: SlackLine.ChatView;
    private chatRoom: SlackLine.ChatRoom;

    constructor(username: string) {
      this.chatView = new SlackLine.ChatView();
      this.chatRoom = new SlackLine.ChatRoom(this.chatView, username);
    }
  }
}

new SlackLine.SlackLineChat('wfortin1234');