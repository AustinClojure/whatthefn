(function($) {

  var Message = Backbone.Model.extend();

  var Messages = Backbone.Collection.extend({
    model: Message
  });

  var MessageQueue = Backbone.Model.extend({
    defaults: {
      'roomId': 'room-a',
      'pollInterval': 2500,
      'handlerFunction': function() {}
    },

    initialize: function() {
      this.set('messages', new Messages());
    },

    start: function() {
      this.poll();
    },

    poll: function() {
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
      _.each(messages, this.get('handlerFunction'))
    }
  });

  window.MessageQueue = MessageQueue;
})(jQuery);
