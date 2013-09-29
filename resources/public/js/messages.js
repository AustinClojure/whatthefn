(function($) {
  var Clock = Backbone.Model.extend({
    millisecondsRemaining: function() {
      return this.get('deadline') - this.get('current');
    }
  });

  var Message = Backbone.Model.extend({
    getChatString: function() {
      return 'Message id ' + this.get('id') + ': Type: ' + this.get('type');
    }
  });

  var EndRoundMessage = Backbone.Model.extend({
    getChatString: function() {
      return 'The round has ended.';
    }
  });

  var NewRoundMessage = Backbone.Model.extend({
    getChatString: function() {
      return 'A new round has started!';
    },

    getSampleValue: function() {
      if (this.get('round-data').type === 'numeric') {
        return '77';
      } else if (this.get('round-data').type === 'string') {
        return '"my string"';
      } else if (this.get('round-data').type === 'numeric-collection') {
        return '[1 2 3]';
      }
    },

    getEditorString: function() {
      return ";====================================================\n" +
             ";    Function Name: " + this.get('round-data').name + "\n" +
             ";====================================================\n" +
             ";    Function Type: " + this.get('round-data').type + "\n" +
             ";====================================================\n" +
             ";    Run (the-fn " + this.getSampleValue() + ") from the chat to get hints!\n" +
             ";====================================================\n" +
             "(defn the-fn\n" +
             "  [x]\n" +
             "  ; Put your solution here\n" +
             ")";
    }
  });

  var ChatMessage = Message.extend({
    getChatString: function() {
      return this.get('player') + ': ' + this.get('string');
    }
  });

  var ResolveMessage =  Message.extend({
    getChatString: function() {
      return this.get('player') + ' hinted : ' + this.get('input') + '-> ' + this.get('output');
    }
  });

  var TestMessage =  Message.extend({
    getChatString: function() {
      return this.get('player') + ' guessed ' + (this.get('result') ? "CORRECTLY" : "WRONG");
    },

    isRoundOver: function() {
      return this.get('result');
    }
  });

  var JoinMessage =  Message.extend({
    getChatString: function() {
      return "*" + this.get('player') + ' has joined the game';
    }
  });

  var LeaveMessage =  Message.extend({
    getChatString: function() {
      return "*" + this.get('player') + ' has left the game';
    }
  });

  var Messages = Backbone.Collection.extend({
    model: function(attrs, options) {
      if (attrs.type == 'chat') {
        return new ChatMessage(attrs, options);
      } else if (attrs.type == 'round-begins') {
        return new NewRoundMessage(attrs, options);
      } else if (attrs.type == 'round-ends') {
        return new EndRoundMessage(attrs, options);
      } else if (attrs.type == 'answer-solution') {
        return new TestMessage(attrs, options);
      } else if (attrs.type == 'resolve-input') {
        return new ResolveMessage(attrs, options);
      } else if (attrs.type == 'player-in-room') {
        return new JoinMessage(attrs, options);
      } else if (attrs.type == 'leave') {
        return new LeaveMessage(attrs, options);
      } else {
        return new Message(attrs, options);
      }
    }
  });

  var MessageQueue = Backbone.Model.extend({
    defaults: {
      'isStopped': false,
      'roomId': 'room-a',
      'pollInterval': 1000,
      'handlerFunction': function() {},
      'tickFunction': function() {}
    },

    mock: function(attrs) {
      this.get('messages').add(attrs);
    },

    initialize: function() {
      this.set('messages', new Messages());
      this.get('messages').on('add', this.handleNewMessage, this);

      this.set('clock', new Clock({
        deadline: Date.now() + .25 * 60 * 1000
      }));
      this.get('clock').on('change', this.callTickFunction, this);
    },

    start: function() {
      this.set('isStopped', false);
      this.poll();
    },

    stop: function() {
      this.set('isStopped', true);
    },

    poll: function() {
      if (this.get('isStopped')) {
       // console.log('Message queue is stopped. Restart it by calling the start() method');
        return;
      }
      var params = this.get('messages').isEmpty() ? {} : {since: this.latestMessageId()};

      $.getJSON(this.messagesPath(), params, _.bind(this.handleResponse, this));

      setTimeout(_.bind(this.poll, this), this.get('pollInterval'));
    },

    latestMessageId: function() {
      return this.get('messages').last().get('id');
    },

    messagesPath: function() {
      return '/rooms/' + this.get('roomId') + '/messages';
    },

    handleResponse: function(response) {
      this.get('clock').set('current', response['server-time-unix-millis']);
      this.get('messages').add(response.messages);
    },

    handleNewMessage: function(message) {
      this.get('handlerFunction').call(null, message);
    },

    callTickFunction: function() {
      this.get('tickFunction').call(null, this.get('clock').millisecondsRemaining());
    },
  });

  window.MessageQueue = MessageQueue;
})(jQuery);
