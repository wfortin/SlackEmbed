///<reference path='../definitions/lib.d.ts' />

module SlackLine {

  export interface ChatEvent {
    content: string;
  }

  export class ChatView {
    private placeholder:Element;
    private sendMessageButton:Element;
    private newMessageInput:HTMLTextAreaElement;
    private messagesContainer: Element;

    constructor() {
      this.placeholder = document.querySelector('#chat-placeholder');
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
      this.addMessage(message);
      chatRoom.sendChatMessage({
        content: message
      });
      this.newMessageInput.value = '';
    }

    protected checkSubmitMessage(e:KeyboardEvent) {
      if (e.keyCode == 13) {
        this.onSumbitMessage(e);
      }
    }

    public addMessage(message: string) {
      this.messagesContainer = document.querySelector('.messages-container');
      var messageEl = document.createElement('div');
      messageEl.innerHTML = Templates.ChatMessage({
        user: 'User Name Here',
        time: new Date().toISOString(),
        message: message
      });
      this.messagesContainer.appendChild(messageEl);
    }
  }
}
