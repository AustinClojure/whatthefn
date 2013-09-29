(function($) {
  var GameApi = Backbone.Model.extend({
    defaults: {
      'roomId': 'the-room',
      'playerId': 'newbie'
    },

    resolveInput: function(input) {
      this.sendEvent({
        type: 'resolve-input',
        player: this.get('playerId'),
        room: this.get('roomId'),
        arg: input
      });
    },

    testSolution: function(solution) {
      this.sendEvent({
        type: 'test-solution',
        player: this.get('playerId'),
        'function': solution
      });
    },
    
    leave: function(input) {
      this.sendEvent({
	type: 'player-left',
	player: this.get('playerId'),
        room: this.get('roomId')
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
