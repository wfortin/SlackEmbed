///<reference path='definitions/lib.d.ts' />

module SlackLine {

  export class ChatView {
    private placeholder:HTMLElement;
    private sendMessageButton:Element;
    private newMessageInput:HTMLTextAreaElement;
    private messagesContainer: Element;

    public doSubmitMessage: Function;

    constructor(private username: string) {
      this.placeholder = <HTMLElement> document.querySelector('#slackline');
      this.placeholder.classList.add('collapsed');
      this.createChatBox();

      this.sendMessageButton = document.querySelector('#send-message');
      this.sendMessageButton.addEventListener('click', (e:Event) => this.onSumbitMessage(e));

      this.newMessageInput = <HTMLTextAreaElement> document.querySelector('#new-message-input');
      this.newMessageInput.addEventListener('keyup', (e:KeyboardEvent) => this.checkSubmitMessage(e));

      this.messagesContainer = document.querySelector('.messages-container');
    }

    private createChatBox() {
      var chatBox = document.createElement('div');
      chatBox.innerHTML = Templates.ChatBox();
      this.placeholder.appendChild(chatBox);
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
  }
}
