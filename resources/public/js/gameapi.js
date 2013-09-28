(function($) {
  var GameApi = Backbone.Model.extend({
    defaults: {
      'roomId': 'room-a',
      'playerId': 'player-a'
    },

    resolveInput: function(input) {
      this.sendEvent({
        type: 'resolve-input',
        player: this.get('playerId'),
        arg: input
      });
    },

    testSolution: function(solution) {
      this.sendEvent({
        type: 'resolve-input',
        player: this.get('playerId'),
        'function': solution
      });
    },

    chat: function(string) {
      this.sendEvent({
        type: 'chat',
        player: this.get('playerId'),
        string: string
      });
    },

    sendEvent: function(params) {
      $.post(this.eventsPath(), params);
    },

    eventsPath: function() {
      return '/rooms/' + this.get('roomId') + '/events';
    }
  });

  window.GameApi = GameApi;
})(jQuery);
