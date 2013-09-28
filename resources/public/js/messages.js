(function($) {

  var Message = Backbone.Model.extend({
    getChatString: function() {
      return 'Message id ' + this.get('id') + ': Time: ' + this.get('server-time-unix-millis');
    }
  });

  var Messages = Backbone.Collection.extend({
    model: Message
  });

  var MessageQueue = Backbone.Model.extend({
    defaults: {
      'isStopped': false,
      'roomId': 'room-a',
      'pollInterval': 2500,
      'handlerFunction': function() {}
    },

    initialize: function() {
      this.set('messages', new Messages());
      this.get('messages').on('add', this.handleNewMessage, this);
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

      $.getJSON(this.messagesPath(), params, _.bind(this.addMessages, this));

      setTimeout(_.bind(this.poll, this), this.get('pollInterval'));
    },

    latestMessageId: function() {
      return this.get('messages').last().get('id');
    },

    messagesPath: function() {
      return '/rooms/' + this.get('roomId') + '/messages';
    },

    addMessages: function(messages) {
      this.get('messages').add(messages);
    },

    handleNewMessage: function(message) {
      this.get('handlerFunction').call(null, message);
    }
  });

  window.MessageQueue = MessageQueue;
})(jQuery);
