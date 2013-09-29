(function($) {
  var Clock = Backbone.Model.extend({
    millisecondsRemaining: function() {
      return this.get('deadline') - this.get('current');
    }
  });

  var Message = Backbone.Model.extend({
    getChatString: function() {
      return 'Message id ' + this.get('id') + ': Time: ' + this.get('server-time-unix-millis');
    }
  });

  var ChatMessage = Message.extend({
    getChatString: function() {
      return this.get('player') + ': ' + this.get('string');
    }
  });

  var ResolveMessage =  Message.extend({
    getChatString: function() {
      return this.get('player') + ': ' + this.get('testinput') + '-> ' + this.get('testoutput');
    }
  });

  var TestMessage =  Message.extend({
    getChatString: function() {
      return this.get('player') + ' s Submission: ' + this.get('testresult');
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
      } 
      else if (attrs.type == 'test') {
        return new TestMessage(attrs, options);
      }
      else if (attrs.type == 'resolve') {
        return new ResolveMessage(attrs, options);
      }
      else if (attrs.type == 'join') {
        return new JoinMessage(attrs, options);
      }
      else if (attrs.type == 'leave') {
        return new LeaveMessage(attrs, options);
      }
      else {
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
