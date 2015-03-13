///<reference path='definitions/lib.d.ts' />
var SlackLine;
(function (SlackLine) {
    var Templates = (function () {
        function Templates() {
        }
        Templates.ChatMessage = function (args) {
            return '<div class="user">' + args.username + '</div>' + '<div class="timestamp">' + this.formatTime(args.timestamp) + '</div>' + '<div class="message-text">' + args.content + '</div>';
        };
        Templates.ChatBox = function () {
            return '<div class="chat-header">' + 'Can I help you bro?' + '</div>' + '<div class="messages-container"></div>' + '<div class="new-message-container">' + '<textarea id="new-message-input" cols="30" rows="10"></textarea>' + '<button id="send-message" type="button">send</button>' + '</div>';
        };
        Templates.formatTime = function (timestamp) {
            timestamp = parseFloat(timestamp);
            var date = new Date(timestamp * 1000);
            var hours = date.getHours();
            hours = hours < 10 ? '0' + hours : hours;
            var minutes = date.getMinutes();
            minutes = minutes < 10 ? '0' + minutes : minutes;
            return hours + ':' + minutes;
        };
        return Templates;
    })();
    SlackLine.Templates = Templates;
})(SlackLine || (SlackLine = {}));
///<reference path='definitions/lib.d.ts' />
var SlackLine;
(function (SlackLine) {
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
            this.chatView.doSubmitMessage = function (event) { return _this.sendChatMessage(event); };
        }
        ChatRoom.prototype.onConnect = function () {
            this.sendWelcomeMessage({
                userId: 'wfortin1234'
            });
        };
        ChatRoom.prototype.onChatMessage = function (chatEvent) {
            this.chatView.addMessage(chatEvent);
        };
        ChatRoom.prototype.sendChatMessage = function (chatEvent) {
            this.socket.emit('chat', chatEvent);
        };
        ChatRoom.prototype.sendWelcomeMessage = function (welcomeEvent) {
            this.socket.emit('welcome', {
                userId: welcomeEvent.userId
            });
        };
        return ChatRoom;
    })();
    SlackLine.ChatRoom = ChatRoom;
})(SlackLine || (SlackLine = {}));
///<reference path='definitions/lib.d.ts' />
var SlackLine;
(function (SlackLine) {
    var ChatView = (function () {
        function ChatView(username) {
            var _this = this;
            this.username = username;
            this.placeholder = document.querySelector('#slackline');
            this.placeholder.classList.add('collapsed');
            this.createChatBox();
            this.sendMessageButton = document.querySelector('#send-message');
            this.sendMessageButton.addEventListener('click', function (e) { return _this.onSumbitMessage(e); });
            this.newMessageInput = document.querySelector('#new-message-input');
            this.newMessageInput.addEventListener('keyup', function (e) { return _this.checkSubmitMessage(e); });
            this.messagesContainer = document.querySelector('.messages-container');
        }
        ChatView.prototype.createChatBox = function () {
            var chatBox = document.createElement('div');
            chatBox.innerHTML = SlackLine.Templates.ChatBox();
            this.placeholder.appendChild(chatBox);
        };
        ChatView.prototype.onSumbitMessage = function (e) {
            var message = this.newMessageInput.value.toString();
            var chatEvent = {
                content: message,
                username: 'You',
                timestamp: new Date().getTime() / 1000
            };
            this.addMessage(chatEvent);
            this.newMessageInput.value = '';
            if (this.doSubmitMessage) {
                this.doSubmitMessage(chatEvent);
            }
        };
        ChatView.prototype.checkSubmitMessage = function (e) {
            if (e.keyCode == 13 && !e.shiftKey) {
                this.onSumbitMessage(e);
            }
        };
        ChatView.prototype.addMessage = function (chatEvent) {
            this.messagesContainer = document.querySelector('.messages-container');
            var messageEl = document.createElement('div');
            messageEl.classList.add('message');
            messageEl.innerHTML = SlackLine.Templates.ChatMessage(chatEvent);
            this.messagesContainer.appendChild(messageEl);
            this.messagesContainer.scrollTop = this.messagesContainer.scrollHeight;
        };
        return ChatView;
    })();
    SlackLine.ChatView = ChatView;
})(SlackLine || (SlackLine = {}));
///<reference path='definitions/lib.d.ts' />
var SlackLine;
(function (SlackLine) {
    var SlackLineChat = (function () {
        function SlackLineChat(username) {
            this.chatView = new SlackLine.ChatView(username);
            this.chatRoom = new SlackLine.ChatRoom(this.chatView);
        }
        return SlackLineChat;
    })();
    SlackLine.SlackLineChat = SlackLineChat;
})(SlackLine || (SlackLine = {}));
new SlackLine.SlackLineChat('wfortin1234');
