const laneDefault = {
  // strokeColor: "${getStrokeColor}",
  strokeColor: "#FF9900",
  // fillColor: "${getFillColor}",
  fillColor: "#FF9900",
  strokeOpacity: 1,
  strokeWidth: 4,
  fillOpacity: .9,
  pointRadius: 6,
  label: "${getLabel}",
  fontFamily: "Arial",
  fontSize: "8px",
  cursor: "pointer"
};

const measureDefault = {
  strokeColor: "#FF9900",
  fillColor: "#FF9900",
  strokeOpacity: 1,
  strokeWidth: 2,
  fillOpacity: .9,
  fontFamily: "Arial",
  fontSize: "8px",
  cursor: "pointer"
};

const barDefault = {
  strokeColor: "#FF0000",
  fillColor: "#FF0000",
  strokeOpacity: 1,
  strokeWidth: 3,
  fillOpacity: 0,
  pointRadius: 2
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
  strokeOpacity: .5,
  strokeWidth: 1,
  fillOpacity: .1,
  pointRadius: 1
};

const connectionsDefault = {
  strokeColor: "#0000FF",
  fillColor: "#0000FF",
  strokeOpacity: 1,
  strokeWidth: 1,
  fillOpacity:.5,
  pointRadius: 6,
  graphicName: "triangle",
  rotation: "${angle}"
};


const laneStyle = (feature) => {
  return new ol.style.Style({
    stroke: new ol.style.Stroke({
      color: feature.get('computed') ? '#FF5000' : (feature.get('source') ? '#00EDFF' : "#FF9900"),
      width: laneDefault.strokeWidth,
      opacity: laneDefault.strokeOpacity
    }),
    fill: new ol.style.Fill({
      color: feature.get('computed') ? '#FF5000' : (feature.get('source') ? '#00EDFF' : "#FF9900"),
      opacity: laneDefault.fillOpacity
    }),
    text: new ol.style.Text({
      text: feature.getGeometry().getType() === "LineString"? '': feature.get('laneNumber') ? feature.get('laneNumber').toString() : '',
      font: `${laneDefault.fontSize} ${laneDefault.fontFamily}`,
      fill: new ol.style.Fill({
        color: '#000'
      })
    }),
    image: new ol.style.Circle({
      radius: laneDefault.pointRadius,
      fill: new ol.style.Fill({
        color: feature.get('computed') ? '#FF5000' : (feature.get('source') ? '#00EDFF' : laneDefault.fillColor)
      })
    })
  });
};

const measureStyle = (feature) => {
  return new ol.style.Style({
    stroke: new ol.style.Stroke({
      color: feature.get('computed') ? '#FF5000' : (feature.get('source') ? '#00EDFF' : "#FF9900"),
      width: measureDefault.strokeWidth,
      opacity: measureDefault.strokeOpacity
    }),
    fill: new ol.style.Fill({
      color: feature.get('computed') ? '#FF5000' : (feature.get('source') ? '#00EDFF' : "#FF9900"),
      opacity: laneDefault.fillOpacity
    })
  });
};

const barStyle = (feature) => {
  return new ol.style.Style({
    fill: new ol.style.Fill({
      color: 'rgba(255, 255, 255, 0)', 
    }),
    stroke: new ol.style.Stroke({
      color: barDefault.strokeColor,
      width: barDefault.strokeWidth,
      opacity: barDefault.strokeOpacity
    })
  });
};

const barHighlightedStyle = ()=>{
  return new ol.style.Style({
    fill: new ol.style.Fill({
      color: 'rgba(61, 65, 176, 0.5)',
    }),
    stroke: new ol.style.Stroke({
      color: 'rgb(6, 14, 226)',
      width: 3,
    })
  });
}


const vectorStyle = (feature)=>{
  console.log(feature);
  return new ol.style.Style(vectorDefault)
};

const widthStyle = (feature) => {
  return new ol.style.Style({
    stroke: new ol.style.Stroke({
      color: `rgba(255, 255, 0, ${widthDefault.strokeOpacity})`, 
      width: widthDefault.strokeWidth
    }),
    fill: new ol.style.Fill({
      color: `rgba(255, 255, 0, ${widthDefault.fillOpacity})`
    }),
    image: new ol.style.Circle({
      radius: widthDefault.pointRadius,
      fill: new ol.style.Fill({
        color: `rgba(255, 255, 0, ${widthDefault.fillOpacity})`
      }),
      stroke: new ol.style.Stroke({
        color: `rgba(255, 255, 0, ${widthDefault.strokeOpacity})`, 
        width: widthDefault.strokeWidth
      })
    })
  });
};

const connectionsStyle = (feature) => {
  return new ol.style.Style({
    stroke: new ol.style.Stroke({
      color: connectionsDefault.strokeColor,
      width: connectionsDefault.strokeWidth,
      opacity: connectionsDefault.strokeOpacity
    }),
    fill: new ol.style.Fill({
      color: connectionsDefault.fillColor,
      opacity: connectionsDefault.fillOpacity
    }),
    image: new ol.style.RegularShape({
      points: 3,
      radius: connectionsDefault.pointRadius,
      angle: feature.get('angle') || 0,
      fill: new ol.style.Fill({
        color: connectionsDefault.fillColor
      })
    })
  });
};


const lineStyle = new ol.style.Style({
  stroke: new ol.style.Stroke({
    color: 'red',
    width: 2
  })
});

const pointStyle = new ol.style.Style({
  image: new ol.style.Circle({
    radius: 5,
    fill: new ol.style.Fill({
      color: 'red'
    })
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

// Style for midpoints (less opacity)
const midMarkerStyle = new ol.style.Style({
  image: new ol.style.Circle({
      radius: 5,
      fill: new ol.style.Fill({ color: 'rgba(240, 133, 45, 0.5)' }), // Semi-transparent
      stroke: new ol.style.Stroke({ color: 'rgba(247, 154, 14, 0.5)', width: 1 })
  })
});


export {
  laneStyle,
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