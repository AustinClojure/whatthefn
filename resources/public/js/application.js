function handleMessage(msg) {
  jQuery('#statusbox').append("\n" + msg.getChatString());
  if (msg.get('type') === 'round-begins') {
    editor.setValue(msg.getEditorString(), 0);
    editor.moveCursorTo(0, 0);
  } else if (msg.get('type') === 'answer-solution' && msg.isRoundOver()) {
    //window.mq.stop();
    //jQuery('#Victor').text(msg.get('player') + " won the round!").appendTo('#RoundModal');
    //jQuery('#RoundModal').modal('show');
  }
  jQuery('#statusbox').scrollTop(jQuery('#statusbox').height());
}

// Much of this is from the Ace editor's scrollable demo

  var $ = document.getElementById.bind(document);
  var dom = require("ace/lib/dom");
  //add command to all new editor instaces
  require("ace/commands/default_commands").commands.push({
  name: "Toggle Fullscreen",
  bindKey: "F11",
  exec: function(editor) {
  dom.toggleCssClass(document.body, "fullScreen")
  dom.toggleCssClass(editor.container, "fullScreen")
  editor.resize()
  }
  }, {
  name: "add",
  bindKey: "Shift-Return",
  exec: add
  })

  // create first editor
  var editor = ace.edit("editor");
  editor.setTheme("ace/theme/cobalt");
  editor.session.setMode("ace/mode/clojure");
  editor.setFontSize(16);
  
  editor.setReadOnly(false);


  var count = 1;
  function add() {
  var oldEl = editor.container
  var pad = document.createElement("div")
  pad.style.padding = "40px"
  oldEl.parentNode.insertBefore(pad, oldEl.nextSibling)

  var el = document.createElement("div")
  oldEl.parentNode.insertBefore(el, pad.nextSibling)

  count++
  var theme = "ace/theme/" + themes[Math.floor(themes.length * Math.random() - 1e-5)]
  editor = ace.edit(el)
  editor.setTheme(theme)
  editor.session.setMode("ace/mode/clojure")

  editor.setValue([
  "this is editor number: ", count, "\n",
  "using theme \"", theme, "\"\n",
  ":)"
  ].join(""), -1)

  scroll()
  }


  function scroll(speed) {
  var top = editor.container.getBoundingClientRect().top
  speed = speed || 10
  if (top > 60 && speed < 500) {
                          if (speed > top - speed - 50)
    speed = top - speed - 50
    else
    setTimeout(scroll, 10, speed + 10)
    window.scrollBy(0, speed)
    }
    }

    var themes = {
    bright: [ "chrome", "clouds", "crimson_editor", "dawn", "dreamweaver", "eclipse", "github",
    "solarized_light", "textmate", "tomorrow"],
    dark: [ "clouds_midnight", "cobalt", "idle_fingers", "kr_theme", "merbivore", "merbivore_soft",
    "mono_industrial", "monokai", "pastel_on_dark", "solarized_dark",  "tomorrow_night",
    "tomorrow_night_blue", "tomorrow_night_bright", "tomorrow_night_eighties", "twilight", "vibrant_ink"]
    }
    themes = [].concat(themes.bright, themes.dark)
    setTimeout(function(){ window.scrollTo(0,0) }, 10)

    whatthefn.core.init()

    function myTimer(mymilliseconds) {


    var minutes = Math.max(0,Math.floor(mymilliseconds / 60000));
    var seconds = Math.max(0,Math.floor((mymilliseconds - (60000 * Math.floor(minutes))) / 1000));

    //console.log("myMillis: " + mymilliseconds + " Mins: " + minutes + " Seconds: " + seconds)

    var pminutes = "00";
    var pseconds = "00";

    if (minutes > 9) { pminutes = String(minutes) }
    else if (minutes > 0) { pminutes = "0" + String(minutes) }
    else { pminutes = "00" }

    if (seconds > 9) { pseconds = String(seconds) }
    else if (seconds > 0) { pseconds = "0" + String(seconds) }
    else { pseconds = "00" }



    if (minutes > 1) {
    $('clock').className="label pull-right btn-info";
    $('clock').innerHTML="Time Remaining: " + pminutes + ":" + pseconds;
    }
    else if (minutes > 0) {
    if (seconds == 58 || seconds == 56) { $('clock').className="label pull-right btn-info"; }
    else { $('clock').className="label pull-right btn-warning";}
    $('clock').innerHTML="Time Remaining: " + pminutes + ":" + pseconds;
    }
    else if (seconds > 0) {

    if (seconds == 58 || seconds == 56) { $('clock').className="label pull-right btn-warning"; }
    else if ((10 >= seconds ) && ((seconds % 2) == 0) ) { $('clock').className="label pull-right btn-inverse";  }
    else { $('clock').className="label pull-right btn-danger"; }
    $('clock').innerHTML="Time Remaining: " + pminutes + ":" + pseconds;
    }
    else
    {
    $('clock').className="label pull-right btn-inverse";
    $('clock').innerHTML="Time's Up! " + pminutes + ":" + pseconds;
    }
    } //myTimer



    function updateClock(millisecondsRemaining) {
    }

    window.mq = new MessageQueue({
    roomId: 'room-a',
    tickFunction: myTimer,
    handlerFunction: handleMessage
    });

    window.mq.start();

    this.api = new GameApi({roomId: 'room-a', playerId: "app.html"});
    this.api.join();
