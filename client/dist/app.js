var Templates = (function () {
    function Templates() {
    }
    Templates.ChatMessage = function (args) {
        return '<div class="user">' + args.user + '</div>' + '<div class="timestamp">' + args.time + '</div>' + '<div class="message">' + args.message + '</div>';
    };
    Templates.ChatBox = function () {
        return '<div class="chat-header">' + 'Can I help you bro?' + '</div>' + '<div class="messages-container"></div>' + '<div class="new-message-container">' + '<textarea id="new-message-input" cols="30" rows="10"></textarea>' + '<button id="send-message" type="button">send</button>' + '</div>';
    };
    return Templates;
})();
var ChatRoom = (function () {
    function ChatRoom(chatView) {
        var _this = this;
        this.chatView = chatView;
        this.socket = io.connect('192.168.68.62:8080', {
            'reconnection delay': 2000,
            'force new connection': true
        });
        this.socket.on('connect', function () { return _this.onConnect(); });
        this.socket.on('chat', function (chatEvent) { return _this.onChatMessage(chatEvent); });
    }
    ChatRoom.prototype.onConnect = function () {
        this.sendChatMessage({
            content: 'new user connected needing help asap'
        });
    };
    ChatRoom.prototype.onChatMessage = function (chatEvent) {
        this.chatView.addMessage(chatEvent.content);
    };
    ChatRoom.prototype.sendChatMessage = function (chatEvent) {
        this.socket.emit('chat', chatEvent);
    };
    return ChatRoom;
})();
///<reference path='../definitions/lib.d.ts' />
var ChatView = (function () {
    function ChatView() {
        this.placeholder = document.querySelector('#chat-placeholder');
        this.createChatBox();
        this.sendMessageButton = document.querySelector('#send-message');
        this.sendMessageButton.addEventListener('click', this.onSumbitMessage);
        this.newMessageInput = document.querySelector('#new-message-input');
        this.newMessageInput.addEventListener('keyup', this.checkSubmitMessage);
        this.messagesContainer = document.querySelector('.messages-container');
    }
    ChatView.prototype.createChatBox = function () {
        var chatBox = document.createElement('div');
        chatBox.innerHTML = Templates.ChatBox();
        this.placeholder.appendChild(chatBox);
    };
    ChatView.prototype.onSumbitMessage = function (e) {
        var message = this.newMessageInput.value.toString();
        this.addMessage(message);
        chatRoom.sendChatMessage({
            content: message
        });
        this.newMessageInput.value = '';
    };
    ChatView.prototype.checkSubmitMessage = function (e) {
        if (e.keyCode == 13) {
            this.onSumbitMessage(e);
        }
    };
    ChatView.prototype.addMessage = function (message) {
        this.messagesContainer = document.querySelector('.messages-container');
        var messageEl = document.createElement('div');
        messageEl.innerHTML = Templates.ChatMessage({
            user: 'User Name Here',
            time: new Date().toISOString(),
            message: message
        });
        this.messagesContainer.appendChild(messageEl);
    };
    return ChatView;
})();
///<reference path='definitions/lib.d.ts' />
var chatView = new ChatView();
var chatRoom = new ChatRoom(chatView);
