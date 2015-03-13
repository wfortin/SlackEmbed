///<reference path='definitions/lib.d.ts' />
var SlackLineChat = (function () {
    function SlackLineChat(username) {
        this.chatView = new SlackLine.ChatView(username);
        this.chatRoom = new SlackLine.ChatRoom(this.chatView);
    }
    return SlackLineChat;
})();
exports.SlackLineChat = SlackLineChat;
new SlackLineChat('wfortin1234');
