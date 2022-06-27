// ORNAMENTS ///////////////////////////////////////////////////////

// Added as part of the Ingress #Helios in 2014, ornaments
// are additional image overlays for portals.
// currently there are 6 known types of ornaments: ap$x$suffix
// - cluster portals (without suffix)
// - volatile portals (_v)
// - meeting points (_start)
// - finish points (_end)
//
// Beacons and Frackers were introduced at the launch of the Ingress
// ingame store on November 1st, 2015
// - Beacons (pe$TAG - $NAME) ie: 'peNIA - NIANTIC'
// - Frackers ('peFRACK')
// (there are 7 different colors for each of them)


window.ornaments = {

  OVERLAY_SIZE: 60,
  OVERLAY_OPACITY: 0.6,
  iconUrls: [],
  excludedOrnaments: [],

  setup: function () {
    this._portals = {};
    var layerGroup = L.layerGroup;
    if (window.map.options.preferCanvas && L.Browser.canvas && !window.DISABLE_CANVASICONLAYER) {
      layerGroup = L.canvasIconLayer;
      L.CanvasIconLayer.mergeOptions({ padding: L.Canvas.prototype.options.padding });
    }
    this.load();
    this._layer = layerGroup();
    this._beacons = layerGroup();
    this._frackers = layerGroup();
    this._scout = layerGroup();
    this._battle = layerGroup();
    this._excluded = layerGroup(); //to keep excluded ornaments in an own layer
    
    window.layerChooser.addOverlay(this._layer, 'Ornaments');
    window.layerChooser.addOverlay(this._beacons, 'Beacons');
    window.layerChooser.addOverlay(this._frackers, 'Frackers');
    window.layerChooser.addOverlay(this._scout, 'Scouting');
    window.layerChooser.addOverlay(this._battle, 'Battle');
    window.layerChooser.addOverlay(this._excluded, 'Excluded'); //just for testing

    $('#toolbox').append('<a onclick="window.ornaments.ornamentsOpt();return false;" accesskey="o" title="Edit ornament exclusions [o]">Ornaments Opt</a>');

  },

  addPortal: function (portal) {
    this.removePortal(portal);
    var ornaments = portal.options.data.ornaments;
    if (ornaments && ornaments.length) {
      this._portals[portal.options.guid] = ornaments.map(function (ornament) {
        var layer = this._layer; 
        var opacity = this.OVERLAY_OPACITY;
        var size = this.OVERLAY_SIZE;
        var anchor = [size / 2, size / 2];
        var iconUrl = '//commondatastorage.googleapis.com/ingress.com/img/map_icons/marker_images/' + ornament + '.png';

        if (ornament.startsWith('pe')) {
          layer = ornament === 'peFRACK'
            ? this._frackers
            : this._beacons;
        }

        if (ornament.startsWith('sc')) {
          layer = this._scout;
        }

        if (ornament.startsWith('peB')) {
          layer = this._battle;
        }

        if (typeof (window.ornaments.iconUrls[ornament]) !== 'undefined') {
          opacity = 1;
          iconUrl = window.ornaments.iconUrls[ornament];
          anchor = [size / 2, size];
        }

        var exclude = false;
        exclude = this.excludedOrnaments.some( function(pattern) {
              return ornament.startsWith(pattern)
        });
        if (exclude){ 
//          opacity = 0;
          layer = this._excluded;
        }

        return L.marker(portal.getLatLng(), {
          icon: L.icon({  
            iconUrl: iconUrl,
            iconSize: [size, size],
            iconAnchor: anchor, // https://github.com/IITC-CE/Leaflet.Canvas-Markers/issues/4
            className: 'no-pointer-events'
          }),
          interactive: false,
          keyboard: false,
          opacity: opacity,
          layer: layer,
        }).addTo(layer);

      }, this);
    }
  },

  removePortal: function (portal) {
    var guid = portal.options.guid;
    if (this._portals[guid]) {
//      console.log(window.ornaments._portals[guid], guid);
      this._portals[guid].forEach(function (marker) {
        marker.options.layer.removeLayer(marker);
      });
      delete this._portals[guid];
    }
  },

  load: function () {
    try {
      var dataStr = localStorage['excludedOrnaments'];
      if (dataStr === undefined) return;
      this.excludedOrnaments = JSON.parse(dataStr);
    } catch(e) {
    console.warn('ornaments: failed to load data from localStorage: '+e);
    }
  },

  save: function () {
    localStorage['excludedOrnaments'] = JSON.stringify(this.excludedOrnaments);
  },

  ornamentsOpt: function () {
    var eO = window.ornaments.excludedOrnaments.toString();
    var html = '<div class="ornamentsOpts">'
             + 'Hide Ornaments from IITC that start with:<br>'
             + '<input type="text" value="'+eO +'" id="ornaments_E"></input><br>'
             + '(separator: space or komma allowed)<hr>'
             + '<b>known Ornaments:</b><br>'
             + 'ap1-ap9<br>'
             + 'sc5_p<br>'
             + 'bb_s<br>'
             + 'peFRACK<br>'
             + 'peNIA<br>'
             + 'peBN_BLM<br>'
             + 'peBN_ENL_WINNER-60<br>'
             + 'peBN_RES_WINNER-60<br>'
             + 'peBN_TIED_WINNER-60<br>'
             + 'peBR_REWARD-10_125_38<br>'
             + 'peBR_REWARD-10_150_75<br>'
             + 'peBR_REWARD-10_175_113<br>'
             + 'peFW_ENL<br>'
             + 'peFW_RES<br>'
             + 'peLOOK<br>'
             + 'peNEMESIS<br>'
             + 'peTOASTY<br>'
             + 'peBB_BATTLE_RARE<br>'
             + 'peBB_BATTLE<br>'
             + '<br>'
             + '</div>';

    dialog({
      html:html,
      id:'ornamentsOpt',
      dialogClass:'ui-dialog-content',
      title:'Ornament excludes',
      buttons: {
        'OK': function() {
          // stuff to do
          // remove markers
//          debugger;
          window.ornaments.excludedOrnaments = $("#ornaments_E").val().split(/[\s,]+/);
          console.log(window.ornaments.excludedOrnaments);
          window.ornaments.save();
          // reload markers
          
          $(this).dialog('close');
        }
      }
    });
  }
};
