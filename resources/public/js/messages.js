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

  var ChatMessage = Backbone.Model.extend({
    getChatString: function() {
      return this.get('player') + ': ' + this.get('string');
    }
  });

  var Messages = Backbone.Collection.extend({
    model: function(attrs, options) {
      if (attrs.type == 'chat') {
        return new ChatMessage(attrs, options);
      } else {
        return new Message(attrs, options);
      }
    }
  });

  var MessageQueue = Backbone.Model.extend({
    defaults: {
      'isStopped': false,
      'roomId': 'room-a',
      'pollInterval': 2500,
      'handlerFunction': function() {},
      'tickFunction': function() {}
    },

    initialize: function() {
      this.set('messages', new Messages());
      this.get('messages').on('add', this.handleNewMessage, this);

      this.set('clock', new Clock({
        deadline: Date.now() + 5 * 60 * 1000
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
        console.log('Message queue is stopped. Restart it by calling the start() method');
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
