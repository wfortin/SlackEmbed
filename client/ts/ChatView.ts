///<reference path='definitions/lib.d.ts' />

module SlackLine {

  export class ChatView {
    private chatBox:HTMLElement;
    private sendMessageButton:Element;
    private newMessageInput:HTMLTextAreaElement;
    private messagesContainer: Element;
    private chatBoxHeader: Element;

    public doInitializeChat: Function;
    public doSubmitMessage: Function;

    constructor() {
      this.chatBox = <HTMLElement> document.querySelector('#slackline');
      this.chatBox.classList.add('collapsed');
      this.createChatBox();

      this.sendMessageButton = document.querySelector('#send-message');
      this.sendMessageButton.addEventListener('click', (e:Event) => this.onSumbitMessage(e));

      this.newMessageInput = <HTMLTextAreaElement> document.querySelector('#new-message-input');
      this.newMessageInput.addEventListener('keyup', (e:KeyboardEvent) => this.checkSubmitMessage(e));

      this.messagesContainer = document.querySelector('.messages-container');

      this.chatBoxHeader = document.querySelector('#slackline .chat-header');
      this.chatBoxHeader.addEventListener('click', (e: Event) => { this.toggleChatBox(e) })
    }

    private createChatBox() {
      var chatBox = document.createElement('div');
      chatBox.innerHTML = Templates.ChatBox();
      this.chatBox.appendChild(chatBox);
    }

    protected onSumbitMessage(e:Event) {
      var message = this.newMessageInput.value.toString();
      var chatEvent : ChatEvent = {
        content: message,
        username: 'You',
        timestamp: new Date().getTime()/1000
      };
      this.addMessage(chatEvent);
      this.newMessageInput.value = '';

      if (this.doSubmitMessage) {
        this.doSubmitMessage(chatEvent);
      }
    }

    protected checkSubmitMessage(e:KeyboardEvent) {
      if (e.keyCode == 13 && !e.shiftKey) {
        this.onSumbitMessage(e);
      }
    }

    public addMessage(chatEvent: ChatEvent) {
      this.messagesContainer = document.querySelector('.messages-container');
      var messageEl = document.createElement('div');
      messageEl.classList.add('message');
      messageEl.innerHTML = Templates.ChatMessage(chatEvent);
      this.messagesContainer.appendChild(messageEl);
      this.messagesContainer.scrollTop = this.messagesContainer.scrollHeight;
    }

    public toggleChatBox(e: Event) {
      var isCollapsed = this.chatBox.classList.contains('collapsed');
      if (isCollapsed) {
        this.chatBox.classList.remove('collapsed');
        this.doInitializeChat();
      } else {
        this.chatBox.classList.add('collapsed');
      }
    }
  }
}
