// ========== Style Defaults ==========
const laneDefault = {
  strokeColor: "#FF9900",
  fillColor: "#FF9900",
  strokeOpacity: 1,
  strokeWidth: 5,
  fillOpacity: 0.9,
  pointRadius: 8,
  fontFamily: "Arial",
  fontSize: "10px",
  cursor: "pointer"
};

const measureDefault = {
  strokeColor: "#FF9900",
  fillColor: "#FF9900",
  strokeOpacity: 1,
  strokeWidth: 4,
  fillOpacity: 0.9,
  fontFamily: "Arial",
  fontSize: "10px",
  cursor: "pointer"
};

const barDefault = {
  strokeColor: "#FF0000",
  fillColor: "#FF0000",
  strokeOpacity: 1,
  strokeWidth: 4,
  fillOpacity: 0,
  pointRadius: 3
};

const vectorDefault = {
  strokeColor: "#FF9900",
  fillColor: "#FF9900",
  strokeOpacity: 1,
  strokeWidth: 1,
  fillOpacity: 0,
  pointRadius: 1
};

const widthDefault = {
  strokeColor: "#FFFF00",
  fillColor: "#FFFF00",
  strokeOpacity: 0.5,
  strokeWidth: 1,
  fillOpacity: 0.1,
  pointRadius: 1
};

const connectionsDefault = {
  strokeColor: "#0000FF",
  fillColor: "#0000FF",
  strokeOpacity: 1,
  strokeWidth: 1,
  fillOpacity: 0.5,
  pointRadius: 6
};

const areaDefault = {
  strokeColor: "#00FF33",
  fillColor: "#00FF33",
  strokeOpacity: 1,
  strokeWidth: 3,
  fillOpacity: 0,
  pointRadius: 2
};

const polyDefault = {
  strokeColor: "#FF9900",
  fillColor: "#FF9900",
  strokeOpacity: 1,
  strokeWidth: 4,
  fillOpacity: 0.2,
  pointRadius: 6
};

const polyDefault2 = {
  strokeColor: "#FF9900",
  fillColor: "#FF9900",
  strokeOpacity: 1,
  strokeWidth: 4,
  fillOpacity: 0.9,
  pointRadius: 6
};

// ========== Style Functions ==========

const laneStyle = (feature) => new ol.style.Style({
  stroke: new ol.style.Stroke({
    color: feature.get('computed') ? '#FF5000' : (feature.get('source') ? '#00EDFF' : laneDefault.strokeColor),
    width: laneDefault.strokeWidth
  }),
  fill: new ol.style.Fill({
    color: feature.get('computed') ? '#FF5000' : (feature.get('source') ? '#00EDFF' : laneDefault.fillColor)
  }),
  text: new ol.style.Text({
    text: feature.getGeometry().getType() === "LineString" ? '' : (
      feature.get('laneNumber')
        ? String(parseInt(feature.get('laneNumber'), 10))
        : ''
    ),
    font: `${laneDefault.fontSize} ${laneDefault.fontFamily}`,
    fill: new ol.style.Fill({ color: '#000' })
  }),
  image: new ol.style.Circle({
    radius: laneDefault.pointRadius,
    fill: new ol.style.Fill({
      color: feature.get('computed') ? '#FF5000' : (feature.get('source') ? '#00EDFF' : laneDefault.fillColor)
    })
  })
});

const measureStyle = (feature) => new ol.style.Style({
  stroke: new ol.style.Stroke({
    color: feature.get('computed') ? '#FF5000' : (feature.get('source') ? '#00EDFF' : measureDefault.strokeColor),
    width: measureDefault.strokeWidth
  }),
  fill: new ol.style.Fill({
    color: feature.get('computed') ? '#FF5000' : (feature.get('source') ? '#00EDFF' : measureDefault.fillColor)
  })
});

const barStyle = () => new ol.style.Style({
  fill: new ol.style.Fill({ color: 'rgba(255,255,255,0)' }),
  stroke: new ol.style.Stroke({
    color: barDefault.strokeColor,
    width: barDefault.strokeWidth
  })
});

const barHighlightedStyle = () => new ol.style.Style({
  fill: new ol.style.Fill({ color: 'rgba(61, 65, 176, 0.5)' }),
  stroke: new ol.style.Stroke({ color: 'rgb(6, 14, 226)', width: 3 })
});

const vectorStyle = () => new ol.style.Style({
  stroke: new ol.style.Stroke({
    color: vectorDefault.strokeColor,
    width: vectorDefault.strokeWidth
  }),
  fill: new ol.style.Fill({ color: `rgba(255, 153, 0, ${vectorDefault.fillOpacity})` }),
  image: new ol.style.Circle({
    radius: vectorDefault.pointRadius,
    fill: new ol.style.Fill({ color: `rgba(255, 153, 0, ${vectorDefault.fillOpacity})` })
  })
});

const widthStyle = () => new ol.style.Style({
  stroke: new ol.style.Stroke({
    color: `rgba(255,255,0,${widthDefault.strokeOpacity})`,
    width: widthDefault.strokeWidth
  }),
  fill: new ol.style.Fill({
    color: `rgba(255,255,0,${widthDefault.fillOpacity})`
  }),
  image: new ol.style.Circle({
    radius: widthDefault.pointRadius,
    fill: new ol.style.Fill({ color: `rgba(255,255,0,${widthDefault.fillOpacity})` }),
    stroke: new ol.style.Stroke({ color: `rgba(255,255,0,${widthDefault.strokeOpacity})`, width: widthDefault.strokeWidth })
  })
});

const connectionsStyle = (feature) => new ol.style.Style({
  stroke: new ol.style.Stroke({
    color: connectionsDefault.strokeColor,
    width: connectionsDefault.strokeWidth
  }),
  fill: new ol.style.Fill({ color: connectionsDefault.fillColor }),
  image: new ol.style.RegularShape({
    points: 3,
    radius: connectionsDefault.pointRadius,
    angle: feature.get('angle') || 0,
    fill: new ol.style.Fill({ color: connectionsDefault.fillColor })
  })
});

const areaStyle = () => new ol.style.Style({
  stroke: new ol.style.Stroke({
    color: `rgba(0,255,51,${areaDefault.strokeOpacity})`,
    width: areaDefault.strokeWidth
  }),
  fill: new ol.style.Fill({
    color: `rgba(0,255,51,${areaDefault.fillOpacity})`
  }),
  image: new ol.style.Circle({
    radius: areaDefault.pointRadius,
    fill: new ol.style.Fill({ color: `rgba(0,255,51,${areaDefault.fillOpacity})` })
  })
});

const polyStyle = () => new ol.style.Style({
  stroke: new ol.style.Stroke({
    color: `rgba(255,153,0,${polyDefault.strokeOpacity})`,
    width: polyDefault.strokeWidth
  }),
  fill: new ol.style.Fill({
    color: `rgba(255,153,0,${polyDefault.fillOpacity})`
  }),
  image: new ol.style.Circle({
    radius: polyDefault.pointRadius,
    fill: new ol.style.Fill({ color: `rgba(255,153,0,${polyDefault.fillOpacity})` })
  })
});

const polyStyle2 = () => new ol.style.Style({
  stroke: new ol.style.Stroke({
    color: `rgba(255,153,0,${polyDefault2.strokeOpacity})`,
    width: polyDefault2.strokeWidth
  }),
  fill: new ol.style.Fill({
    color: `rgba(255,153,0,${polyDefault2.fillOpacity})`
  }),
  image: new ol.style.Circle({
    radius: polyDefault2.pointRadius,
    fill: new ol.style.Fill({ color: `rgba(255,153,0,${polyDefault2.fillOpacity})` })
  })
});

const lineStyle = new ol.style.Style({
  stroke: new ol.style.Stroke({ color: 'red', width: 2 })
});

const pointStyle = new ol.style.Style({
  image: new ol.style.Circle({
    radius: 5,
    fill: new ol.style.Fill({ color: 'red' })
  })
});

const errorMarkerStyle = new ol.style.Style({
  image: new ol.style.Icon({
    src: 'img/error.png',
    size: [21, 25],
    anchor: [0.5, 1],
    anchorXUnits: 'fraction',
    anchorYUnits: 'fraction'
  })
});

const midMarkerStyle = new ol.style.Style({
  image: new ol.style.Circle({
    radius: 5,
    fill: new ol.style.Fill({ color: 'rgba(240, 133, 45, 0.5)' }),
    stroke: new ol.style.Stroke({ color: 'rgba(247, 154, 14, 0.5)', width: 1 })
  })
});

// ========== Export All ==========
export {
  laneStyle,
  areaStyle,
  polyStyle,
  polyStyle2,
  vectorStyle,
  lineStyle,
  widthStyle,
  connectionsStyle,
  errorMarkerStyle,
  barStyle,
  pointStyle,
  barHighlightedStyle,
  measureStyle,
  midMarkerStyle
};
